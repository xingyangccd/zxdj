package com.example.zxds.odr.task;

import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.odr.db.dao.OrderBillDao;
import com.example.zxds.odr.db.dao.OrderDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClearTimeoutOrderService {

    private final OrderDao orderDao;
    private final OrderBillDao orderBillDao;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void clearTimeoutOrder(long orderId){
        orderDao.deleteUnAcceptOrder(Map.of("orderId", orderId));
        orderBillDao.deleteUnAcceptOrderBill(orderId);
        if(stringRedisTemplate.hasKey("order#" + orderId)){
            throw new ZxdsException("定时任务：订单与账单删除失败");
        }
    }
}
