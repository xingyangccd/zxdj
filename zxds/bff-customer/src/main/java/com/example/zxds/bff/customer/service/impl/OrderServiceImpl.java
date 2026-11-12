package com.example.zxds.bff.customer.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.bff.customer.feign.*;
import com.example.zxds.bff.customer.service.OrderService;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import com.example.zxds.common.wxpay.WxPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final MpsServiceApi mpsServiceApi;
    private final RuleServiceApi ruleServiceApi;
    private final OdrServiceApi odrServiceApi;
    private final SnmServiceApi snmServiceApi;
    private final DrServiceApi drServiceApi;
    private final VhrServiceApi vhrServiceApi;
    private final CstServiceApi cstServiceApi;
    private final WxPayService wxPayService;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Map<String, Object> createNewOrder(CreateNewOrderForm form) {

        Long customerId = form.getCustomerId();
        String startPlace = form.getStartPlace();
        String startPlaceLatitude = form.getStartPlaceLatitude();
        String startPlaceLongitude = form.getStartPlaceLongitude();
        String endPlace = form.getEndPlace();
        String endPlaceLatitude = form.getEndPlaceLatitude();
        String endPlaceLongitude = form.getEndPlaceLongitude();
        String favourFee = form.getFavourFee();
        String carPlate = form.getCarPlate();
        String carType = form.getCarType();

        // 1.重新预估里程和时长
        EstimateOrderMileageAndMinuteForm mileageForm = new EstimateOrderMileageAndMinuteForm();
        mileageForm.setMode("driving");
        mileageForm.setStartPlaceLatitude(startPlaceLatitude);
        mileageForm.setStartPlaceLongitude(startPlaceLongitude);
        mileageForm.setEndPlaceLatitude(endPlaceLatitude);
        mileageForm.setEndPlaceLongitude(endPlaceLongitude);

        R r = mpsServiceApi.estimateOrderMileageAndMinute(mileageForm);
        Map<String, Object> map = (Map<String, Object>) r.get("result");
        String mileage = MapUtil.getStr(map, "mileage"); // 预估的里程
        int minute = MapUtil.getInt(map, "minute"); // 预估的时长

        // 2. 预估这个订单的代驾费（预估订单金额）。
        EstimateOrderChargeForm chargeForm = new EstimateOrderChargeForm();
        chargeForm.setMileage(mileage); // 预估的公里数
        chargeForm.setTime(new DateTime().toTimeStr()); // 下单时间

        r = ruleServiceApi.estimateOrderCharge(chargeForm);
        map = (Map<String, Object>) r.get("result");

        // 从map中将所有的数据取出
        String expectsFee = MapUtil.getStr(map, "amount");
        String chargeRuleId = MapUtil.getStr(map, "chargeRuleId");
        Short baseMileage = MapUtil.getShort(map, "baseMileage");
        String baseMileagePrice = MapUtil.getStr(map, "baseMileagePrice");
        String exceedMileagePrice = MapUtil.getStr(map, "exceedMileagePrice");
        Short baseMinute = MapUtil.getShort(map, "baseMinute");
        String exceedMinutePrice = MapUtil.getStr(map, "exceedMinutePrice");
        Short baseReturnMileage = MapUtil.getShort(map, "baseReturnMileage");
        String exceedReturnPrice = MapUtil.getStr(map, "exceedReturnPrice");

        // 3.搜索5公里范围之内适合接单的司机，如果存在适合接单的司机则创建订单，否则不创建订单。
        SearchBefittingDriverAboutOrderForm searchDriverForm = new SearchBefittingDriverAboutOrderForm();
        searchDriverForm.setStartPlaceLatitude(startPlaceLatitude);
        searchDriverForm.setStartPlaceLongitude(startPlaceLongitude);
        searchDriverForm.setEndPlaceLatitude(endPlaceLatitude);
        searchDriverForm.setEndPlaceLongitude(endPlaceLongitude);
        searchDriverForm.setMileage(mileage);

        // 查找合适的司机。
        r = mpsServiceApi.searchBefittingDriverAboutOrder(searchDriverForm);
        List<Map<String, Object>> list = (List<Map<String, Object>>) r.get("result"); // 适合接单的司机

        // 准备好要返回的结果
        Map<String, Object> result = new HashMap<>();
        result.put("count", list.size()); // 适合接单的司机数量。
        if (!list.isEmpty()) {
            // 有合适的司机
            // 4.生成订单以及账单记录。
            InsertOrderForm insertOrderForm = new InsertOrderForm();
            // uuid是充当订单号，微信支付的时候需要使用。
            insertOrderForm.setUuid(IdUtil.simpleUUID());
            insertOrderForm.setCustomerId(customerId);
            insertOrderForm.setStartPlace(startPlace);
            insertOrderForm.setStartPlaceLatitude(startPlaceLatitude);
            insertOrderForm.setStartPlaceLongitude(startPlaceLongitude);
            insertOrderForm.setEndPlace(endPlace);
            insertOrderForm.setEndPlaceLatitude(endPlaceLatitude);
            insertOrderForm.setEndPlaceLongitude(endPlaceLongitude);
            insertOrderForm.setExpectsMileage(mileage);
            insertOrderForm.setExpectsFee(expectsFee);
            insertOrderForm.setFavourFee(favourFee);
            insertOrderForm.setDate(new DateTime().toDateStr());
            insertOrderForm.setChargeRuleId(Long.parseLong(chargeRuleId));
            insertOrderForm.setCarPlate(carPlate);
            insertOrderForm.setCarType(carType);
            insertOrderForm.setBaseMileage(baseMileage);
            insertOrderForm.setBaseMileagePrice(baseMileagePrice);
            insertOrderForm.setExceedMileagePrice(exceedMileagePrice);
            insertOrderForm.setBaseMinute(baseMinute);
            insertOrderForm.setExceedMinutePrice(exceedMinutePrice);
            insertOrderForm.setBaseReturnMileage(baseReturnMileage);
            insertOrderForm.setExceedReturnPrice(exceedReturnPrice);

            r = odrServiceApi.insertOrder(insertOrderForm);
            String orderId = MapUtil.getStr(r, "result");

            // 5.发送通知消息符合条件司机，让司机接单（异步发消息给司机，乘客不用等。）
            SendNewOrderMessageForm messageForm = new SendNewOrderMessageForm();
            String[] driversContent = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                // 取出其中一个合适的司机。
                Map<String, Object> one = list.get(i);
                String driverId = MapUtil.getStr(one, "driverId");
                // 把数字进行四舍五入向上取整保留1位小数。
                String distance = new BigDecimal(MapUtil.getStr(one, "distance")).setScale(1, RoundingMode.CEILING).toString();
                driversContent[i] = driverId + "#" + distance;
            }
            messageForm.setDriversContent(driversContent);
            messageForm.setOrderId(Long.parseLong(orderId));
            messageForm.setFrom(startPlace);
            messageForm.setTo(endPlace);
            messageForm.setExpectsFee(expectsFee);
            messageForm.setMileage(new BigDecimal(mileage).setScale(1, RoundingMode.CEILING).toString());
            messageForm.setMinute(minute);
            messageForm.setFavourFee(favourFee);

            snmServiceApi.sendNewOrderMessageAsync(messageForm); // 异步发送消息，通知司机。

            // 将订单orderId返回
            result.put("orderId", orderId);
        }
        // 最终返回的数据：{"count": 3, "orderId": 123152456456}
        return result;
    }

    @Override
    public Integer searchOrderStatus(SearchOrderStatusForm form) {
        R r = odrServiceApi.searchOrderStatus(form);
        return MapUtil.getInt(r, "result");
    }

    @Override
    public String deleteUnAcceptOrder(DeleteUnAcceptOrderForm form) {
        R r = odrServiceApi.deleteUnAcceptOrder(form);
        return MapUtil.getStr(r, "result");
    }

    @Override
    public Map<String, Object> hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form) {
        R r = odrServiceApi.hasCustomerCurrentOrder(form);
        return (Map<String, Object>) r.get("result");
    }

    @Override
    public Map<String, Object> searchOrderForMoveById(SearchOrderForMoveByIdForm form) {
        R r = odrServiceApi.searchOrderForMoveById(form);
        return (Map<String, Object>) r.get("result");
    }

    @Override
    public boolean confirmArriveStartPlace(ConfirmArriveStartPlaceForm form) {
        R r = odrServiceApi.confirmArriveStartPlace(form);
        return MapUtil.getBool(r, "result");
    }

    @Override
    public Map<String, Object> searchOrderById(SearchOrderByIdForm form) {
        R r = odrServiceApi.searchOrderById(form);
        // map是订单+账单的详细信息。
        Map<String, Object> map = (Map<String, Object>) r.get("result");
        Long driverId = MapUtil.getLong(map, "driverId");
        if(driverId != null){
            SearchDriverBriefInfoForm infoForm = new SearchDriverBriefInfoForm();
            infoForm.setDriverId(driverId);
            r = drServiceApi.searchDriverBriefInfo(infoForm);
            Map<String, Object> driverMap = (Map<String, Object>) r.get("result");
            // 账单+订单+司机，合并成一个Map
            map.putAll(driverMap);
            return map;
        }
        return null;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class) // 分布式事务
    public Map<String, String> createWxPayment(long orderId, long customerId, Long voucherId) {
        // 1. 验证该订单是否可以支付
        ValidCanPayOrderForm validCanPayOrderForm = new ValidCanPayOrderForm();
        validCanPayOrderForm.setOrderId(orderId);
        validCanPayOrderForm.setCustomerId(customerId);
        R canPayResult = odrServiceApi.validCanPayOrder(validCanPayOrderForm);
        Map<String, Object> orderInfo = (Map<String, Object>) canPayResult.get("result");
        // 实际需要支付的
        String amount = MapUtil.getStr(orderInfo, "realFee");
        String uuid = MapUtil.getStr(orderInfo, "uuid");
        Long driverId = MapUtil.getLong(orderInfo, "driverId");

        // 2. 使用代金券
        String discount = "0.00";
        if(voucherId != null) { // 条件成立表示乘客使用了代金券
            UseVoucherForm useVoucherForm = new UseVoucherForm();
            useVoucherForm.setVoucherId(voucherId);
            useVoucherForm.setCustomerId(customerId);
            useVoucherForm.setAmount(amount);
            useVoucherForm.setOrderId(orderId);
            R useVoucherResult = vhrServiceApi.useVoucher(useVoucherForm);
            // 使用代金券的话，最终返回的是代金券的面额
            discount = MapUtil.getStr(useVoucherResult, "result");
        }

        if(new BigDecimal(amount).compareTo(new BigDecimal(discount)) < 0){
            throw new ZxdsException("总金额不能小于代金券面额");
        }

        // 3. 更新实付金额（更新账单）
        amount = NumberUtil.sub(amount, discount).toString(); // 获取最终要支付的真正的金额
        UpdateBillPaymentForm updateBillPaymentForm = new UpdateBillPaymentForm();
        updateBillPaymentForm.setOrderId(orderId);
        updateBillPaymentForm.setRealPay(amount);
        updateBillPaymentForm.setVoucherFee(discount);
        odrServiceApi.updateBillPayment(updateBillPaymentForm);

        // 4. 查询乘客openId
        SearchCustomerOpenIdForm searchCustomerOpenIdForm = new SearchCustomerOpenIdForm();
        searchCustomerOpenIdForm.setCustomerId(customerId);
        R customerOpenIdResult = cstServiceApi.searchCustomerOpenId(searchCustomerOpenIdForm);
        String customerOpenId = MapUtil.getStr(customerOpenIdResult, "result");

        // 5. 查询司机openId
        SearchDriverOpenIdForm searchDriverOpenIdForm = new SearchDriverOpenIdForm();
        searchDriverOpenIdForm.setDriverId(driverId);
        R driverOpenIdResult = drServiceApi.searchDriverOpenId(searchDriverOpenIdForm);
        String driverOpenId = MapUtil.getStr(driverOpenIdResult, "result");

        try {
            // 6. 创建微信支付单（微信支付平台，生成微信支付单）
            // 金额需要从元转换为分
            int totalFen = NumberUtil.mul(amount, "100").setScale(0, RoundingMode.FLOOR).intValue();
            //totalFen = 1; // 测试的时候，写1分钱。生产环境这个代码删除。
            totalFen = 4;

            // 调用V3支付服务进行微信支付单的创建。
            Map<String, String> payParams = wxPayService.jsapiPrepay(uuid, "代驾费", totalFen, customerOpenId, driverOpenId);

            // 提取 prepay_id 用于落库
            String prepayId = payParams.get("package").replace("prepay_id=", "");

            // 7. 更新订单的预支付ID
            UpdateOrderPrepayIdForm updateOrderPrepayIdForm = new UpdateOrderPrepayIdForm();
            updateOrderPrepayIdForm.setOrderId(orderId);
            updateOrderPrepayIdForm.setPrepayId(prepayId);
            odrServiceApi.updateOrderPrepayId(updateOrderPrepayIdForm);

            // 返回给前端小程序，前端小程序使用这些参数拉起微信支付。
            payParams.put("uuid", uuid);
            return payParams;
        }catch (Exception e){
            log.error("创建支付订单失败", e);
            throw new ZxdsException("创建支付订单失败");
        }
    }

    @Override
    public String updateOrderAboutPayment(UpdateOrderAboutPaymentForm form) {
        R r = odrServiceApi.updateOrderAboutPayment(form);
        return MapUtil.getStr(r, "result");
    }
}

















