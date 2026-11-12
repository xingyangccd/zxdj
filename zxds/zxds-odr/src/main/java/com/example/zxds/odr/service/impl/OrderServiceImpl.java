package com.example.zxds.odr.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.wxpay.WxPayService;
import com.example.zxds.odr.controller.form.TransferForm;
import com.example.zxds.odr.db.dao.OrderBillDao;
import com.example.zxds.odr.db.dao.OrderDao;
import com.example.zxds.odr.db.pojo.OrderBillEntity;
import com.example.zxds.odr.db.pojo.OrderEntity;
import com.example.zxds.odr.feign.DrServiceApi;
import com.example.zxds.odr.service.OrderService;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final OrderBillDao orderBillDao;
    private final StringRedisTemplate stringRedisTemplate;
    private final DrServiceApi drServiceApi;
    private final RabbitTemplate rabbitTemplate;
    private final WxPayService wxPayService;

    @Override
    public Map<String, Object> searchDriverTodayBusinessData(long driverId) {
        Map<String, Object> result = orderDao.searchDriverTodayBusinessData(driverId);
        // 代驾时长
        result.put("duration", MapUtil.getStr(result, "duration", "0"));
        // 今日收入
        result.put("income", MapUtil.getStr(result, "income", "0.00"));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 建议添加本地事务，这样效率高
    public String insertOrder(OrderEntity orderEntity, OrderBillEntity orderBillEntity) {
        // 1. 插入订单记录
        int rows = orderDao.insert(orderEntity);
        if(rows != 1){
            throw new ZxdsException("保存新订单失败");
        }

        // 2.获取订单id
        String id = orderDao.searchOrderIdByUUID(orderEntity.getUuid());

        // 3. 插入账单记录
        orderBillEntity.setOrderId(Long.parseLong(id));
        rows = orderBillDao.insert(orderBillEntity);
        if(rows != 1){
            throw new ZxdsException("保存该订单对应的账单失败");
        }

        // 4. 写Redis缓存当中，配置Redis Lua脚本防止司机重复抢单
        stringRedisTemplate.opsForValue().set("order#" + id, "none");

        // 订单 15分钟有效。（乘客端看到的倒计时也是15分钟：15分钟的等待时间，等待司机接单。）
        //stringRedisTemplate.expire("order#" + id, 15, TimeUnit.MINUTES);

        // 升级为16分钟
        stringRedisTemplate.expire("order#" + id, KEY_TTL_SECONDS, TimeUnit.SECONDS);

        // 返回的是订单ID。
        return id;
    }


    // 司机抢单的Redis Lua脚本
    private static final String GRAB_ORDER_SCRIPT = """
        local key = KEYS[1]
        local driver = ARGV[1]
        
        local current = redis.call('GET', key)
        if current == false then
            return 'KEY_NOT_EXIST'
        end
        
        if current ~= 'none' then
            return 'ALREADY_TAKEN:' .. current
        end
        
        redis.call('SET', key, driver)
        redis.call('EXPIRE', key, 300)
        
        return 'SUCCESS'
        """;

    private static final String RESULT_SUCCESS = "SUCCESS";
    private static final String RESULT_KEY_NOT_EXIST = "KEY_NOT_EXIST";
    private static final String RESULT_ALREADY_TAKEN_PREFIX = "ALREADY_TAKEN:";
    private static final String KEY_VALUE_NONE = "none";
    private static final long KEY_TTL_SECONDS = 960; // 16分钟（重点：比正常订单有效期15分钟延长1分钟）

    // Lua脚本对象（只初始化一次即可。）
    private static final DefaultRedisScript<String> GRAB_SCRIPT;

    static {
        GRAB_SCRIPT = new DefaultRedisScript<>();
        GRAB_SCRIPT.setScriptText(GRAB_ORDER_SCRIPT);
        GRAB_SCRIPT.setResultType(String.class);
    }

    @Override
    //@Transactional(rollbackFor = Exception.class) // 不要添加，添加上反而有问题。
    public String acceptNewOrder(long driverId, long orderId) {
        // 订单缓存的key
        String key = "order#" + orderId;
        // 司机ID。
        String driverIdStr = String.valueOf(driverId);

        // 1. 执行Redis Lua脚本进行抢单
        String result = stringRedisTemplate.execute(GRAB_SCRIPT, Collections.singletonList(key), driverIdStr);

        // 1.1 订单在 Redis 中不存在：去MySQL兜底查一次。
        if(RESULT_KEY_NOT_EXIST.equals(result)){
            return handleKeyNotExists(key, orderId);
        }

        // 1.2 订单如果已经被其他司机抢走了
        if(result.startsWith(RESULT_ALREADY_TAKEN_PREFIX)){
            String takenDriverId = result.substring(RESULT_ALREADY_TAKEN_PREFIX.length());
            return "抢单失败：订单已被司机" + takenDriverId + "抢走";
        }

        // 1.3 假设脚本返回的不是我们的预期SUCCESS
        if(!RESULT_SUCCESS.equals(result)){
            return "抢单失败";
        }

        // 2. 抢单成功，去MySQL当中落库
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("driverId", driverId);
            params.put("orderId", orderId);

            int rows = orderDao.acceptNewOrder(params);
            if(rows == 1){
                // MySQL更新成功
                stringRedisTemplate.delete(key); // 这里有点牺牲效率。
                return "接单成功";
            }else{
                // MySQL更新失败：回滚Redis的key到“待抢”
                rollbackRedisKey(key);
                return "抢单失败";
            }
        }catch (Exception e){
            // MySQL抛异常：同样需要回滚Redis，避免key被永久占用。
            rollbackRedisKey(key);
            throw new ZxdsException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> searchDriverExecuteOrder(Map<String, Object> param) {
        return orderDao.searchDriverExecuteOrder(param);
    }

    private String handleKeyNotExists(String key, long orderId) {
        // 通过订单ID查询订单的状态status
        Integer status = orderDao.getOrderStatusById(orderId);

        if(status == null){
            return "抢单失败：订单不存在";
        }

        if(status != 1){
            return "抢单失败：订单已被抢走或已失效";
        }

        // status 一定是等于 1 的。
        // 订单仍然处在等待接单阶段：重建Redis key，允许司机重试
        stringRedisTemplate.opsForValue().set(key, KEY_VALUE_NONE, KEY_TTL_SECONDS, TimeUnit.SECONDS);
        return "抢单失败：请重试";
    }

    // Redis回滚：把key改回none，重新允许被抢
    private void rollbackRedisKey(String key){
        stringRedisTemplate.opsForValue().set(key, KEY_VALUE_NONE, KEY_TTL_SECONDS, TimeUnit.SECONDS);
    }


    @Override
    public Integer searchOrderStatus(Map<String, Object> param) {
        Integer status = orderDao.searchOrderStatus(param);
        if (status == null) {
            //throw new ZxdsException("没有查询到数据，请核对查询条件");
            status = 0;
        }
        return status;
    }

    // 取消订单相关的常量
    private static final String RESULT_CANNOT_CANCEL_PREFIX = "CANNOT_CANCEL:ALREADY_TAKEN:";
    private static final DefaultRedisScript<String> DELETE_SCRIPT;

    // 删除订单缓存也需要使用Lua脚本来保证原子性。如果不使用它，会有并发问题。
    static {
        DELETE_SCRIPT = new DefaultRedisScript<>();
        DELETE_SCRIPT.setScriptText("""
                local orderKey = KEYS[1]
                local exists = redis.call('EXISTS', orderKey)
                if exists == 0 then
                    return 'KEY_NOT_EXIST'
                end
                local current = redis.call('GET', orderKey)
                if current ~= 'none' then
                    return 'CANNOT_CANCEL:ALREADY_TAKEN:' .. current
                end
                redis.call('SET', orderKey, 'cancelled')
                redis.call('EXPIRE', orderKey, 30)
                return 'SUCCESS'
                """);
        DELETE_SCRIPT.setResultType(String.class);
    }

    // 1. 删除订单缓存
    // 2. 删除订单
    // 3. 删除账单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteUnAcceptOrder(Map<String, Object> param) {
        long orderId = MapUtil.getLong(param, "orderId");
        String orderKey = "order#" + orderId;

        // 1. 执行Lua脚本
        String result = stringRedisTemplate.execute(DELETE_SCRIPT, Collections.singletonList(orderKey));

        // 2. 处理脚本执行结果
        if(RESULT_KEY_NOT_EXIST.equals(result)){
            return "订单取消失败：订单不存在或已过期";
        }

        if(result.startsWith(RESULT_CANNOT_CANCEL_PREFIX)){
            String driverId = result.substring(RESULT_CANNOT_CANCEL_PREFIX.length());
            if ("cancelled".equals(driverId)) {
                return "订单取消失败：订单已被取消或正在取消中";
            }
            return "订单取消失败：订单已被司机" + driverId + "接单，无法取消";
        }

        if(!RESULT_SUCCESS.equals(result)){
            return "订单取消失败：系统异常";
        }

        // 订单缓存删除成功了
        // 3. 更新MySQL数据库
        try {
            // 3.1 删除订单记录
            int rows = orderDao.deleteUnAcceptOrder(param);
            if(rows != 1){
                throw new ZxdsException("订单删除失败");
            }
            // 3.2 删除关联的账单记录
            rows = orderBillDao.deleteUnAcceptOrderBill(orderId);
            if(rows != 1){
                throw new ZxdsException("账单删除失败");
            }

            return "订单取消成功";

        }catch (Exception e) {
            // 出现数据库异常，数据库回滚了。
            rollbackRedisKey(orderKey);
            throw new ZxdsException(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> searchDriverCurrentOrder(long driverId) {
        return orderDao.searchDriverCurrentOrder(driverId);
    }

    @Override
    public Map<String, Object> hasCustomerCurrentOrder(long customerId) {
        Map<String, Object> result = new HashMap<>();

        // 1.查询是否有未接单的订单（如果有，则返回订单详情）
        Map<String, Object> unAcceptOrder = orderDao.hasCustomerUnAcceptOrder(customerId);
        result.put("hasCustomerUnAcceptOrder", unAcceptOrder != null);
        result.put("unAcceptOrder", unAcceptOrder);

        // 2.查询是否有未完成的订单（如果有，则返回订单ID）
        Long unFinishedOrderId = orderDao.hasCustomerUnFinishedOrder(customerId);
        result.put("hasCustomerUnFinishedOrder", unFinishedOrderId != null);
        result.put("unFinishedOrder", unFinishedOrderId);

        return result;
    }

    @Override
    public Map<String, Object> searchOrderForMoveById(Map<String, Object> param) {
        return orderDao.searchOrderForMoveById(param);
    }

    @Override
    public int arriveStartPlace(Map<String, Object> param) {

        // 1.获取订单ID。
        Long orderId = MapUtil.getLong(param, "orderId");

        // 2.更新MySQL中的订单状态 status 更新为 3
        int rows = orderDao.updateOrderStatus(param);
        if(rows != 1) {
            throw new ZxdsException("更新订单状态失败");
        }

        // 3.MySQL更新成功了，再写入Redis标志位
        // 业务说明：
        // - 司机端点击“到达代驾点”后，向Redis当中写入一个标志位："1"
        // - 乘客端在APP上确认司机到达后，会将Redis中的这个标志位从"1"修改为"2"
        // - 司机端点击“开始代驾”的时候，必须校验该标志位是否为"2"，只有车主确认司机已到达才允许计费并开始代驾。
        // 不需要设置有效期吗？不需要，因为订单状态在进入下一个状态的时候，这个标志位就没用了，到时写代码删除这个标志位。
        stringRedisTemplate.opsForValue().set("order_driver_arrived#" + orderId, "1");

        return rows;
    }

    @Override
    public boolean confirmArriveStartPlace(long orderId) {
        String key = "order_driver_arrived#" + orderId;
        String value = stringRedisTemplate.opsForValue().get(key);
        if("1".equals(value)) {
            stringRedisTemplate.opsForValue().set(key, "2");
            return true;
        }
        return false;
    }

    @Override
    public int startDriving(Map<String, Object> param) {
        Long orderId = MapUtil.getLong(param, "orderId");
        String key = "order_driver_arrived#" + orderId;
        String value = stringRedisTemplate.opsForValue().get(key);
        if("2".equals(value)){
            int rows = orderDao.updateOrderStatus(param);
            if(rows == 1) {
                // 数据库更新成功了，删除标志位。
                stringRedisTemplate.delete(key);
            }
            return rows;
        }
        return 0;
    }

    @Override
    public int updateOrderStatus(Map<String, Object> param) {
        int rows = orderDao.updateOrderStatus(param);
        if (rows != 1) {
            throw new ZxdsException("更新订单记录失败");
        }
        return rows;
    }

    @Override
    public PageUtils searchOrderByPage(Map<String, Object> param) {
        long total = orderDao.searchOrderCount(param);
        List<Map<String, Object>> list = new ArrayList<>();
        if(total > 0){
            list = orderDao.searchOrderByPage(param);
        }
        Integer pageIndex = MapUtil.getInt(param, "pageIndex");
        Integer pageSize = MapUtil.getInt(param, "pageSize");
        return new PageUtils(list, total, pageIndex, pageSize);
    }

    @Override
    public Map<String, Object> searchOrderContent(long orderId) {
        Map<String, Object> map = orderDao.searchOrderContent(orderId);
        if(map != null){
            // 从后端数据库返回的是json格式的字符串，将其转换为对象，发送给前端系统。
            map.replace("startPlaceLocation", JSONUtil.parseObj(MapUtil.getStr(map, "startPlaceLocation")));
            map.replace("endPlaceLocation", JSONUtil.parseObj(MapUtil.getStr(map, "endPlaceLocation")));
        }
        return map;
    }

    @Override
    public boolean validDriverOwnOrder(Map<String, Object> param) {
        return orderDao.validDriverOwnOrder(param);
    }

    @Override
    public Map<String, Object> searchSettlementNeedData(long orderId) {
        Map<String, Object> data = orderDao.searchSettlementNeedData(orderId);
        if (data == null) {
            throw new ZxdsException("未查询到该订单的结算基础数据");
        }
        return data;
    }

    @Override
    public Map<String, Object> searchOrderById(Map<String, Object> param) {
        Map<String, Object> map = orderDao.searchOrderById(param);
        String startPlaceLocation = MapUtil.getStr(map, "startPlaceLocation");
        String endPlaceLocation = MapUtil.getStr(map, "endPlaceLocation");
        map.replace("startPlaceLocation", JSONUtil.parseObj(startPlaceLocation));
        map.replace("endPlaceLocation", JSONUtil.parseObj(endPlaceLocation));
        return map;
    }

    @Override
    public Map<String, Object> validCanPayOrder(Map<String, Object> param) {
        Map<String, Object> map = orderDao.validCanPayOrder(param);
        if(CollUtil.isEmpty(map)){
            throw new ZxdsException("订单无法支付");
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrderPrepayId(Map<String, Object> param) {
        int rows = orderDao.updateOrderPrepayId(param);
        if(rows != 1) {
            throw new ZxdsException("更新预支付订单ID失败");
        }
        return rows;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handlePayment(String uuid, String payId, String driverOpenId, String payTime) {
        // 1.更新订单状态之前，先查询订单的状态
        // 因为乘客端付款成功之后，会主动发送ajax请求，要求更新订单状态。
        // 底层有并发：微信可能回调，乘客端也可能发送ajax请求更新订单状态。
        // 微信回调会走 handlePayment，乘客端发送ajax请求也会走 handlePayment方法。
        // 因此后端接收到付款通知消息之后，不要着急修改订单状态，先看一下订单状态是否已经是7了。
        // 保证幂等性。
        Map<String, String> map = orderDao.searchOrderIdAndStatus(uuid);
        int status = MapUtil.getInt(map, "status", 0);
        if(status == 7){
            log.info("订单状态已为7，不需要重复处理支付回调，uuid: {}", uuid);
            return;
        }

        // 2.更新订单的payId，状态，付款时间（更新订单）
        Map<String, Object> param = Map.of("uuid", uuid, "payId", payId, "payTime", payTime);
        int rows = orderDao.updateOrderPayIdAndStatus(param);
        if(rows != 1) {
            log.warn("并发更新订单状态失败，跳过后续奖励发放以及分账操作，uuid: {}", uuid);
            return;
        }

        // 3. 查询系统奖励
        map = orderDao.searchDriverIdAndIncentiveFee(uuid);
        String incentiveFee = MapUtil.getStr(map, "incentiveFee", "0.00");
        Long driverId = MapUtil.getLong(map, "driverId");

        // 4. 为司机发放奖励（远程feign调用）
        if(new BigDecimal(incentiveFee).compareTo(BigDecimal.ZERO) > 0){ // 条件成立表示有奖励费
            TransferForm form = new TransferForm();
            form.setUuid(IdUtil.simpleUUID());
            form.setAmount(incentiveFee);
            form.setDriverId(driverId);
            form.setType((byte)2);
            form.setRemark("系统奖励费");
            drServiceApi.transfer(form);
            log.info("系统奖励费发放成功，司机ID：{}，金额：{}", driverOpenId, incentiveFee);
        }

        // 5.执行分账（微信平台官方建议1分钟之后进行分账操作，不要立即分账。立即分账可能报错/失败）
        // 向MQ的延迟队列发送一条分账信息（分账信息在延迟队列当中待1分钟，1分钟到期之后，这条消息会自动转入死信队列。）
        // 在死信队列上有消费者监听消息，一旦拿到这个消息，立即调用微信平台的V3分账接口进行分账。
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("uuid", uuid);
        msgMap.put("payId", payId);
        msgMap.put("driverOpenId", driverOpenId);

        rabbitTemplate.convertAndSend("order_delay_exchange", "order_delay_routing_key", msgMap);
        log.info("已发送延迟分账消息，订单号：{}", uuid);

        // 6. 发完分账消息之后，不用等，因为分账是1分钟之后的事儿，异步的，下面直接更新订单状态为8
        rows = orderDao.finishOrder(uuid);
        if(rows != 1) {
            throw new ZxdsException("更新订单结束状态失败");
        }

    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String updateOrderAboutPayment(Map<String, Object> param) {
        long orderId = MapUtil.getLong(param, "orderId");

        // 1. 查询订单状态
        Map<String, String> map = orderDao.searchUuidAndStatus(orderId);
        if (map == null || map.isEmpty()) {
            throw new ZxdsException("未查询到该订单");
        }
        String uuid = MapUtil.getStr(map, "uuid");
        int status = MapUtil.getInt(map, "status", 0);

        // 如果订单状态已经是已付款（7）或已完成（8），直接返回
        if (status == 7 || status == 8) {
            return "付款成功";
        }

        try {
            // 2. 调用微信 V3 官方 SDK 主动查询支付结果
            Transaction transaction = wxPayService.queryOrderByOutTradeNo(uuid);

            // 3. 判断支付状态
            if ("SUCCESS".equals(transaction.getTradeState().name())) {
                // 支付成功了，接下来就是：更新订单、发放奖励给司机、分账。
                String driverOpenId = transaction.getAttach();
                String payId = transaction.getTransactionId();
                String payTime = transaction.getSuccessTime();

                // 4. 更新订单付款信息（注意：这里修改为按 orderId 更新）
                param.put("payId", payId);
                param.put("payTime", payTime);
                int rows = orderDao.updateOrderAboutPayment(param);
                if (rows != 1) {
                    throw new ZxdsException("更新订单相关付款信息失败");
                }

                // 5. 查询系统奖励并发放
                map = orderDao.searchDriverIdAndIncentiveFee(uuid);
                String incentiveFee = MapUtil.getStr(map, "incentiveFee", "0.00");
                long driverId = MapUtil.getLong(map, "driverId", 0L);

                if (new BigDecimal(incentiveFee).compareTo(BigDecimal.ZERO) > 0) {
                    TransferForm form = new TransferForm();
                    form.setUuid(IdUtil.simpleUUID());
                    form.setAmount(incentiveFee);
                    form.setDriverId(driverId);
                    form.setType((byte) 2);
                    form.setRemark("系统奖励费");
                    // 远程feign调用，给司机发放奖励。
                    drServiceApi.transfer(form);
                }

                // 6. 发送延迟分账消息（MQ 解耦）
                Map<String, String> msgMap = new HashMap<>();
                msgMap.put("uuid", uuid);
                msgMap.put("driverOpenId", driverOpenId);
                msgMap.put("payId", payId);

                rabbitTemplate.convertAndSend("order_delay_exchange", "order_delay_routing_key", msgMap);
                log.info("【主动查单】已发送延迟分账消息，订单号: {}", uuid);

                // 7. 更新订单为已完成状态（8）
                rows = orderDao.finishOrder(uuid);
                if (rows != 1) {
                    throw new ZxdsException("更新订单结束状态失败");
                }
                return "付款成功";
            } else if ("NOTPAY".equals(transaction.getTradeState().name())) {
                return "未付款";
            } else {
                return "付款异常: " + transaction.getTradeState().name();
            }
        } catch (Exception e) {
            log.error("主动查询微信支付结果异常, orderId: {}", orderId, e);
            throw new ZxdsException("查询支付结果失败");
        }
    }

}















