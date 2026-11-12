package com.example.zxds.odr.mq;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.wxpay.WxPayService;
import com.example.zxds.odr.db.dao.OrderProfitsharingDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfitSharingConsumer {
    private final OrderProfitsharingDao orderProfitsharingDao;
    private final WxPayService wxPayService;

    @RabbitListener(queues = "order_profit_sharing_queue")
    @Transactional(rollbackFor = Exception.class)
    public void handleProfitSharing(Map<String, Object> msgMap) {
        String uuid = MapUtil.getStr(msgMap, "uuid");
        String payId = MapUtil.getStr(msgMap, "payId");
        String driverOpenId = MapUtil.getStr(msgMap, "driverOpenId");

        log.info("【MQ分账】接收到延迟分账消息，开始处理，订单号：{}", uuid);

        // 1. 查询分账记录
        Map<String, Object> map = orderProfitsharingDao.searchDriverIncome(uuid);
        if(CollectionUtil.isEmpty(map)) {
            log.error("【MQ分账】没有查询到分账记录，uuid:{}", uuid);
            return;
        }
        String driverIncome = MapUtil.getStr(map, "driverIncome"); // 具体的司机的收入
        Long profitSharingId = MapUtil.getLong(map, "profitsharingId"); // 分账记录表中的主键值

        // 金额需要转换成分
        long amount = NumberUtil.mul(driverIncome, "100").setScale(0, RoundingMode.FLOOR).longValue();
        amount = 1;

        try {
            // 2. 调用V3分账接口
            // 这里使用 profitSharingId 变量做outOrderNo（这是商户平台内部自己的分账单的单号）
            String outOrderNo = String.valueOf(profitSharingId);
            String state = wxPayService.requestProfitSharing(payId, outOrderNo, driverOpenId, amount);

            // 3. 处理结果
            if("FINISHED".equals(state)) {
                // 分账既然完成了，这里就要更新分账记录的状态。
                int rows = orderProfitsharingDao.updateProfitsharingStatus(profitSharingId);
                if(rows != 1) {
                    throw new ZxdsException("更新分账记录状态失败");
                }
                log.info("【MQ分账】分账成功，订单号：{}", uuid);
            } else if("PROCESSING".equals(state)) {
                // 当前状态处于：分账中（正在处理分账）
                log.info("【MQ分账】分账处理中，稍后查询，订单号：{}", uuid);
            } else {
                log.error("【MQ分账】分账失败，订单号：{}", uuid);
            }
        }catch (Exception e) {
            log.error("【MQ分账】执行分账异常，订单号：{}", uuid, e);
            // 默认情况下 Spring AMQP 开启了自动确认模式（AUTO）
            // 当消费者方法抛出异常时，消息不会被确认（ACK），RabbitMQ 就会认为处理失败并将消息重新入队再次投递。
            throw new ZxdsException("分账失败，触发MQ重试", e);
        }
    }
}






















