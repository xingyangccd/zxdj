package com.example.zxds.odr.db.dao;

import com.example.zxds.odr.db.pojo.OrderBillEntity;

import java.util.Map;

public interface OrderBillDao {

    /**
     * 创建账单
     */
    int insert(OrderBillEntity entity);

    /**
     * 删除账单
     */
    int deleteUnAcceptOrderBill(long orderId);

    /**
     * 更新账单
     */
    int updateBillFee(Map<String, Object> param);

    /**
     * 获取司机预览账单详情需要的数据
     */
    Map<String, Object> searchReviewDriverOrderBill(Map<String, Object> param);

    /**
     * 更新账单（更新代金券面额和实际需要支付的金额）
     */
    int updateBillPayment(Map<String, Object> param);

}




