package com.example.zxds.odr.service;

import com.example.zxds.common.util.PageUtils;
import com.example.zxds.odr.db.pojo.OrderBillEntity;
import com.example.zxds.odr.db.pojo.OrderEntity;

import java.util.Map;

public interface OrderService {

    Map<String, Object> searchDriverTodayBusinessData(long driverId);

    /**
     * 保存订单和账单
     */
    String insertOrder(OrderEntity orderEntity, OrderBillEntity orderBillEntity);

    /**
     * 司机抢单的核心业务方法
     *
     * @param driverId 司机
     * @param orderId  订单ID
     * @return 字符串，例如“接单成功”，“抢单失败”....
     */
    String acceptNewOrder(long driverId, long orderId);

    /**
     * 根据司机ID和订单ID查询订单的信息。
     */
    Map<String, Object> searchDriverExecuteOrder(Map<String, Object> param);

    /**
     * 查询订单状态
     */
    Integer searchOrderStatus(Map<String, Object> param);

    /**
     * 删除订单
     * 1. 删除Redis缓存
     * 2. 删除订单
     * 3. 删除账单
     */
    String deleteUnAcceptOrder(Map<String, Object> param);

    /**
     * 查询司机当前正在执行的订单（status = 2 3 4）
     */
    Map<String, Object> searchDriverCurrentOrder(long driverId);

    /**
     * 查询乘客当前订单
     */
    Map<String, Object> hasCustomerCurrentOrder(long customerId);

    /**
     * 为司乘同显页面准备数据
     */
    Map<String, Object> searchOrderForMoveById(Map<String, Object> param);

    /**
     * 司机到达代驾点（订单的起点）
     */
    int arriveStartPlace(Map<String, Object> param);

    /**
     * 乘客确认司机已到达代驾点（把标志位1修改为2）
     */
    boolean confirmArriveStartPlace(long orderId);

    /**
     * 司机开始代驾
     */
    int startDriving(Map<String, Object> param);

    /**
     * 更新订单状态
     */
    int updateOrderStatus(Map<String, Object> param);

    /**
     * 分页查询订单信息
     */
    PageUtils searchOrderByPage(Map<String, Object> param);

    /**
     * 根据订单ID查询订单详细信息
     */
    Map<String, Object> searchOrderContent(long orderId);

    /**
     * 验证这个订单是不是这个司机的。
     */
    boolean validDriverOwnOrder(Map<String, Object> param);

    /**
     * 查询结算时需要的数据
     */
    Map<String, Object> searchSettlementNeedData(long orderId);

    /**
     * 查询订单和账单的详细信息
     */
    Map<String, Object> searchOrderById(Map<String, Object> param);

    /**
     * 验证订单是否可以支付
     */
    Map<String, Object> validCanPayOrder(Map<String, Object> param);

    /**
     * 更新订单的预支付ID
     */
    int updateOrderPrepayId(Map<String, Object> param);

    /**
     * 处理支付结果（给微信支付平台回调准备的）
     * @param uuid 商户内部生成的订单号。
     * @param payId 微信平台生成的支付ID。
     * @param driverOpenId 司机的openId
     * @param payTime 支付时间
     */
    void handlePayment(String uuid, String payId, String driverOpenId, String payTime);

    /**
     * 主动查询支付结果并处理支付结果。
     */
    String updateOrderAboutPayment(Map<String, Object> param);
}




