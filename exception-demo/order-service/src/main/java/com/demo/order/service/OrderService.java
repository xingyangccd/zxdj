package com.demo.order.service;

import com.demo.order.common.BizException;
import com.demo.order.common.ErrorCode;
import com.demo.order.dto.OrderDTO;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    public OrderDTO getById(Long id) {
        if (id == 1L) {
            /*try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
            OrderDTO dto = new OrderDTO();
            dto.setId(1L);
            dto.setStatus("PAID");
            dto.setProductName("iPhone 15");
            return dto;
        }
        throw new BizException(ErrorCode.ORDER_NOT_FOUND);
    }
}