package com.example.zxds.bff.customer.service;

import com.example.zxds.bff.customer.controller.form.*;

import java.util.Map;

public interface OrderService {
    /**
     * 乘客创建新订单（非常核心的方法：业务复杂）
     */
    Map<String, Object> createNewOrder(CreateNewOrderForm form);

    /**
     * 查询订单状态
     */
    Integer searchOrderStatus(SearchOrderStatusForm form);

    /**
     * 取消订单
     */
    String deleteUnAcceptOrder(DeleteUnAcceptOrderForm form);

    /**
     * 查询乘客当前的订单
     */
    Map<String, Object> hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form);

    /**
     * 根据订单ID查询订单详细信息。
     */
    Map<String, Object> searchOrderForMoveById(SearchOrderForMoveByIdForm form);

    /**
     * 乘客确认司机到达
     */
    boolean confirmArriveStartPlace(ConfirmArriveStartPlaceForm form);

    /**
     * 查询订单 账单的详细信息，以及司机的简要信息。
     */
    Map<String, Object> searchOrderById(SearchOrderByIdForm form);

    /**
     * 创建微信支付单
     */
    Map<String, String> createWxPayment(long orderId, long customerId, Long voucherId);

    String updateOrderAboutPayment(UpdateOrderAboutPaymentForm form);

}
