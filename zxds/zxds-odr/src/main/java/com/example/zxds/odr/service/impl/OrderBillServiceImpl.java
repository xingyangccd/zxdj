package com.example.zxds.odr.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.common.util.SnowflakeUtil;
import com.example.zxds.odr.db.dao.OrderBillDao;
import com.example.zxds.odr.db.dao.OrderDao;
import com.example.zxds.odr.db.dao.OrderProfitsharingDao;
import com.example.zxds.odr.db.pojo.OrderProfitsharingEntity;
import com.example.zxds.odr.service.OrderBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderBillServiceImpl implements OrderBillService {

    private final OrderBillDao orderBillDao;
    private final OrderDao orderDao;
    private final OrderProfitsharingDao orderProfitsharingDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBillFee(Map<String, Object> param) {
        // 更新账单数据
        int rows = orderBillDao.updateBillFee(param);
        if(rows != 1){
            throw new ZxdsException("更新账单失败");
        }

        // 更新订单数据
        rows = orderDao.updateOrderMileageAndFee(param);
        if(rows != 1){
            throw new ZxdsException("更新订单失败");
        }

        // 插入分账记录
        OrderProfitsharingEntity entity = new OrderProfitsharingEntity();
        entity.setId(SnowflakeUtil.orderId());
        entity.setOrderId(MapUtil.getLong(param, "orderId"));
        entity.setRuleId(MapUtil.getLong(param, "ruleId"));

        entity.setAmountFee(getDecimal(param, "total"));
        entity.setPaymentRate(getDecimal(param, "paymentRate"));
        entity.setPaymentFee(getDecimal(param, "paymentFee"));
        entity.setTaxRate(getDecimal(param, "taxRate"));
        entity.setTaxFee(getDecimal(param, "taxFee"));
        entity.setSystemIncome(getDecimal(param, "systemIncome"));
        entity.setDriverIncome(getDecimal(param, "driverIncome"));

        rows = orderProfitsharingDao.insert(entity);
        if(rows != 1){
            throw new ZxdsException("添加分账记录失败");
        }

        return rows;
    }

    private BigDecimal getDecimal(Map<String, Object> param, String key){
        return new BigDecimal(MapUtil.getStr(param, key));
    }

    @Override
    public Map<String, Object> searchReviewDriverOrderBill(Map<String, Object> param) {
        return orderBillDao.searchReviewDriverOrderBill(param);
    }

    @Override
    public int updateBillPayment(Map<String, Object> param) {
        int rows = orderBillDao.updateBillPayment(param);
        if(rows != 1){
            throw new ZxdsException("更新账单实际支付费用失败");
        }
        return rows;
    }
}




















