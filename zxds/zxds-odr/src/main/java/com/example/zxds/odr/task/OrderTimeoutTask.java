package com.example.zxds.odr.task;

import cn.hutool.core.collection.CollUtil;
import com.example.zxds.odr.db.dao.OrderDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {
    private final OrderDao orderDao;
    private final StringRedisTemplate stringRedisTemplate;
    private final ClearTimeoutOrderService clearTimeoutOrderService;

    //@Scheduled(cron = "0 */1 * * * ?")
    public void clearTimeoutOrders() {
        // 1.查询超期的订单ID。
        List<Long> orderIds = orderDao.searchTimeoutOrderIds();
        if (CollUtil.isEmpty(orderIds)) {
            return;
        }

        log.info("定时任务：发现{}条超时未接单的订单，开始清理...", orderIds.size());

        // 2. 遍历集合进行订单和账单的删除
        for (Long orderId : orderIds) {
            try {
                if(!stringRedisTemplate.hasKey("order#" + orderId)) {
                    clearTimeoutOrderService.clearTimeoutOrder(orderId);
                }
                log.info("定时任务：成功清理超时订单，orderId={}", orderId);
            } catch (Exception e) {
                log.error("定时任务：清理超时订单失败：orderId={}", orderId);
            }
        }
    }
}
