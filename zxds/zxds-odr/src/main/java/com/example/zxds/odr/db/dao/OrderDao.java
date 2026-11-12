package com.example.zxds.odr.db.dao;


import com.example.zxds.odr.db.pojo.OrderEntity;

import java.util.List;
import java.util.Map;

public interface OrderDao {

    /**
     * 查询司机当天的营业数据
     */
    Map<String, Object> searchDriverTodayBusinessData(long driverId);

    /**
     * 保存新订单
     */
    int insert(OrderEntity entity);

    /**
     * 通过订单的uuid获取订单id
     */
    String searchOrderIdByUUID(String uuid);

    /**
     * 司机接单：修改状态 接单时间 司机ID，有前提：status必须等于1
     */
    int acceptNewOrder(Map<String, Object> param);

    /**
     * 根据订单ID查询订单状态
     */
    Integer getOrderStatusById(long orderId);

    /**
     * 根据订单ID和司机ID查询订单信息。
     */
    Map<String, Object> searchDriverExecuteOrder(Map<String, Object> param);

    /**
     * 查询订单的状态
     */
    Integer searchOrderStatus(Map<String, Object> param);

    /**
     * 删除订单
     */
    int deleteUnAcceptOrder(Map<String, Object> param);

    /**
     * 查询司机当前的订单数据（状态status要求是2 3 4的订单数据）
     */
    Map<String, Object> searchDriverCurrentOrder(long driverId);

    /**
     * 查询超期的订单ID集合。（status是1，并且下单时间超过16分钟了）
     */
    List<Long> searchTimeoutOrderIds();

    /**
     * 查询乘客未完成的订单（status = 2 3 4）
     */
    Long hasCustomerUnFinishedOrder(long customerId);

    /**
     * 在乘客端需要查询司机没有接单的订单（status = 1）
     */
    Map<String, Object> hasCustomerUnAcceptOrder(long customerId);

    /**
     * 为司乘同显页面准备数据
     */
    Map<String, Object> searchOrderForMoveById(Map<String, Object> param);

    /**
     * 更新订单状态（通用方法）
     */
    int updateOrderStatus(Map<String, Object> param);

    /**
     * 查询符合条件的订单数量
     */
    long searchOrderCount(Map<String, Object> param);

    /**
     * 查询符合条件的订单数据
     */
    List<Map<String, Object>> searchOrderByPage(Map<String, Object> param);

    /**
     * 根据订单ID查询订单的详细内容。
     */
    Map<String, Object> searchOrderContent(long orderId);

    /**
     * 更新订单（更新实例费用、实际里程、系统奖励费等）
     */
    int updateOrderMileageAndFee(Map<String, Object> param);

    /**
     * 验证该订单的执行人是不是该司机。
     */
    boolean validDriverOwnOrder(Map<String, Object> param);

    /**
     * 查询结算时需要的数据。
     */
    Map<String, Object> searchSettlementNeedData(long orderId);

    /**
     * 查询账单和订单的详细信息
     */
    Map<String, Object> searchOrderById(Map<String, Object> param);

    /**
     * 验证该订单是否能够支付
     */
    Map<String, Object> validCanPayOrder(Map<String, Object> param);

    /**
     * 更新预支付ID
     */
    int updateOrderPrepayId(Map<String, Object> param);

    /**
     * 根据outTradeNo查询订单ID和状态
     */
    Map<String, String> searchOrderIdAndStatus(String uuid);

    /**
     * 根据outTradeNo查询司机ID以及该司机要发放的奖励
     */
    Map<String, String> searchDriverIdAndIncentiveFee(String uuid);

    /**
     * 更新订单的支付时间、状态、payId
     */
    int updateOrderPayIdAndStatus(Map<String, Object> param);

    /**
     * 将订单状态更新为8，订单已结束
     */
    int finishOrder(String uuid);

    /**
     * 根据订单ID查询uuid和status
     */
    Map<String, String> searchUuidAndStatus(long orderId);

    /**
     * 根据订单ID更新状态，pay_id，pay_time
     */
    int updateOrderAboutPayment(Map<String, Object> param);
}




