package com.example.zxds.bff.driver.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.feign.*;
import com.example.zxds.bff.driver.service.OrderService;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OdrServiceApi odrServiceApi;
    private final CstServiceApi cstServiceApi;
    private final NebulaServiceApi nebulaServiceApi;
    private final RuleServiceApi ruleServiceApi;
    private final SnmServiceApi snmServiceApi;

    @Override
    public String acceptNewOrder(AcceptNewOrderForm form) {
        R r = odrServiceApi.acceptNewOrder(form);
        return MapUtil.getStr(r, "result");
    }

    @Override
    public Map<String, Object> searchDriverExecuteOrder(SearchDriverExecuteOrderForm form) {
        // 1.查询订单信息
        R orderResp = odrServiceApi.searchDriverExecuteOrder(form);
        Map<String, Object> orderMap = (Map<String, Object>)orderResp.get("result");

        // 2.查询代驾客户信息
        long customerId = MapUtil.getLong(orderMap, "customerId");
        SearchCustomerInfoInOrderForm customerInfoForm = new SearchCustomerInfoInOrderForm();
        customerInfoForm.setCustomerId(customerId);
        R cstResp = cstServiceApi.searchCustomerInfoInOrder(customerInfoForm);
        Map<String, Object> cstMap = (Map<String, Object>)cstResp.get("result");

        // 3.组装map
        Map<String, Object> result = new HashMap<>();
        result.putAll(orderMap);
        result.putAll(cstMap);

        return result;
    }

    @Override
    public Map<String, Object> searchDriverCurrentOrder(SearchDriverCurrentOrderForm form) {
        // 1.查询订单信息
        R orderResp = odrServiceApi.searchDriverCurrentOrder(form);
        Map<String, Object> orderMap = (Map<String, Object>)orderResp.get("result");

        if(MapUtil.isEmpty(orderMap)){
            return null;
        }

        // 2.查询代驾客户的信息
        Long customerId = MapUtil.getLong(orderMap, "customerId");
        SearchCustomerInfoInOrderForm customerInfoForm = new SearchCustomerInfoInOrderForm();
        customerInfoForm.setCustomerId(customerId);
        R cstResp = cstServiceApi.searchCustomerInfoInOrder(customerInfoForm);
        Map<String, Object> cstMap = (Map<String, Object>)cstResp.get("result");

        // 3.合并 订单 + 客户，并返回。
        Map<String, Object> result = new HashMap<>();
        result.putAll(orderMap);
        result.putAll(cstMap);

        return result;
    }

    @Override
    public Map<String, Object> searchOrderForMoveById(SearchOrderForMoveByIdForm form) {
        R r = odrServiceApi.searchOrderForMoveById(form);
        return (Map<String, Object>) r.get("result");
    }

    @Override
    public int arriveStartPlace(ArriveStartPlaceForm form) {
        // 调用下游的订单子服务，更新订单状态。
        R r = odrServiceApi.arriveStartPlace(form);
        int rows = MapUtil.getInt(r, "result");
        if(rows == 1){
            // TODO 调用消息子服务，给乘客发送通知消息（司机已到达上车点）
        }
        return rows;
    }

    @Override
    public int startDriving(StartDrivingForm form) {
        R r = odrServiceApi.startDriving(form);
        int rows = MapUtil.getInt(r, "rows");
        if (rows == 1) {
            // 开始代驾的时候，保存订单的监控初始数据到HBase当中。
            InsertOrderMonitoringForm monitoringForm = new InsertOrderMonitoringForm();
            monitoringForm.setOrderId(form.getOrderId());
            nebulaServiceApi.insertOrderMonitoring(monitoringForm);

            // TODO 发送通知消息给乘客（开始代驾了）
        }
        return rows;
    }

    @Override
    public int updateOrderStatus(UpdateOrderStatusForm form) {
        // 更新订单状态
        R r = odrServiceApi.updateOrderStatus(form);
        int rows = MapUtil.getInt(r, "rows");

        // 判断订单的状态，然后实现后续业务
        if(rows != 1){
            throw new ZxdsException("订单状态更新失败");
        }
        if(form.getStatus() == 6){
            // 发送账单消息给乘客。
            SendPrivateMessageForm messageForm = new SendPrivateMessageForm();
            messageForm.setReceiverIdentity("customer_bill");
            messageForm.setReceiverId(form.getCustomerId());
            // 3天之后消息丢失，不用担心：因为未支付的业务数据不会丢失。
            messageForm.setTtl(3 * 24 * 3600 * 1000); // 消息在MQ上的有效期是3天。
            // 让系统作为信使，下发计费通知
            messageForm.setSenderId(0L);
            messageForm.setSenderIdentity("system");
            messageForm.setSenderName("智行代驾");
            messageForm.setMsg("您有代驾订单待支付");
            snmServiceApi.sendPrivateMessageAsync(messageForm);
        }

        return rows;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class) // 分布式事务
    public int updateOrderBill(UpdateBillFeeForm form) {
        // 1. 判断该订单是不是该司机的
        ValidDriverOwnOrderForm validForm = new ValidDriverOwnOrderForm();
        validForm.setDriverId(form.getDriverId());
        validForm.setOrderId(form.getOrderId());
        R validResult = odrServiceApi.validDriverOwnOrder(validForm);
        if(!MapUtil.getBool(validResult, "result", false)){
            throw new ZxdsException("司机未关联该订单");
        }

        // 2. 计算订单实际里程
        CalculateOrderMileageForm mileageForm = new CalculateOrderMileageForm();
        mileageForm.setOrderId(form.getOrderId());
        R mileageResult = nebulaServiceApi.calculateOrderMileage(mileageForm);
        String realMileage = MapUtil.getStr(mileageResult, "result", "0");
        // 单位转换为公里（保留1位小数，向上取整）
        realMileage = NumberUtil.div(realMileage, "1000", 1, RoundingMode.CEILING).toString();

        // 3.查询订单结算时需要的数据。
        SearchSettlementNeedDataForm settlementForm = new SearchSettlementNeedDataForm();
        settlementForm.setOrderId(form.getOrderId());
        R settlementResult = odrServiceApi.searchSettlementNeedData(settlementForm);
        Map<String, Object> settlementData = (Map<String, Object>)settlementResult.get("result");
        String acceptTime = MapUtil.getStr(settlementData, "acceptTime"); // 接单时间
        String startTime = MapUtil.getStr(settlementData, "startTime"); // 开始时间
        int waitingMinute = MapUtil.getInt(settlementData, "waitingMinute", 0); // 等时
        String favourFee = MapUtil.getStr(settlementData, "favourFee", "0"); // 顾客好处费

        // 4.计算代驾费
        CalculateOrderChargeForm chargeForm = new CalculateOrderChargeForm();
        chargeForm.setMileage(realMileage);
        if(StrUtil.isNotBlank(startTime)){
            chargeForm.setTime(startTime.split(" ")[1]);
        }
        chargeForm.setMinute(waitingMinute);
        R chargeResult = ruleServiceApi.calculateOrderCharge(chargeForm);
        Map<String, Object> chargeData = (Map<String, Object>) chargeResult.get("result");

        // 取出后续需要的数据。
        String mileageFee = MapUtil.getStr(chargeData, "mileageFee"); // 里程费
        String returnFee = MapUtil.getStr(chargeData, "returnFee"); // 返程费
        String waitingFee = MapUtil.getStr(chargeData, "waitingFee"); // 等时费
        String amount = MapUtil.getStr(chargeData, "amount"); // 总金额
        String returnMileage = MapUtil.getStr(chargeData, "returnMileage"); // 返程里程

        // 5. 计算系统奖励费
        CalculateIncentiveFeeForm incentiveForm = new CalculateIncentiveFeeForm();
        incentiveForm.setDriverId(form.getDriverId());
        incentiveForm.setAcceptTime(acceptTime);
        R incentiveResult = ruleServiceApi.calculateIncentiveFee(incentiveForm);
        String incentiveFee = MapUtil.getStr(incentiveResult, "result", "0");

        // 组装基础费用数据
        form.setMileageFee(mileageFee);
        form.setReturnFee(returnFee);
        form.setWaitingFee(waitingFee);
        form.setIncentiveFee(incentiveFee); // 系统奖励费赋值给form的属性。
        form.setRealMileage(realMileage);
        form.setReturnMileage(returnMileage);

        // 计算总费用（总费用：不包含系统奖励费）
        String total = NumberUtil.add(amount, form.getTollFee(), form.getParkingFee(), form.getOtherFee(), favourFee).toString();
        form.setTotal(total);

        // 6. 计算分账数据
        CalculateProfitsharingForm profitForm = new CalculateProfitsharingForm();
        profitForm.setOrderId(form.getOrderId());
        profitForm.setAmount(total);
        R profitResult = ruleServiceApi.calculateProfitsharing(profitForm);
        Map<String, Object> profitData = (Map<String, Object>) profitResult.get("result");

        form.setRuleId(MapUtil.getLong(profitData, "ruleId"));
        form.setSystemIncome(MapUtil.getStr(profitData, "systemIncome"));
        form.setDriverIncome(MapUtil.getStr(profitData, "driverIncome"));
        form.setPaymentRate(MapUtil.getStr(profitData, "paymentRate"));
        form.setPaymentFee(MapUtil.getStr(profitData, "paymentFee"));
        form.setTaxRate(MapUtil.getStr(profitData, "taxRate"));
        form.setTaxFee(MapUtil.getStr(profitData, "taxFee"));

        // 7. 更新账单
        R updateResult = odrServiceApi.updateBillFee(form);
        return MapUtil.getInt(updateResult, "rows", 0);
    }

    @Override
    public Map<String, Object> searchReviewDriverOrderBill(SearchReviewDriverOrderBillForm form) {
        R r = odrServiceApi.searchReviewDriverOrderBill(form);
        return (Map<String, Object>) r.get("result");
    }

    @Override
    public Integer searchOrderStatus(SearchOrderStatusForm form) {
        R r = odrServiceApi.searchOrderStatus(form);
        return MapUtil.getInt(r, "result");
    }

    @Override
    public String updateOrderAboutPayment(long driverId, UpdateOrderAboutPaymentForm form) {
        // 1. 校验该订单是否属于当前司机（防止跨订单操作）
        ValidDriverOwnOrderForm validForm = new ValidDriverOwnOrderForm();
        validForm.setDriverId(driverId);
        validForm.setOrderId(form.getOrderId());
        R r = odrServiceApi.validDriverOwnOrder(validForm);

        if (!MapUtil.getBool(r, "result")) {
            throw new ZxdsException("司机未关联该订单");
        }

        // 2. 调用底层 ODR 服务，主动向微信发起查单并更新状态
        r = odrServiceApi.updateOrderAboutPayment(form);
        return MapUtil.getStr(r, "result");
    }
}














