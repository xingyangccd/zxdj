package com.demo.order.controller;

import com.demo.order.common.R;
import com.demo.order.dto.OrderDTO;
import com.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders/{id}")
    public R<OrderDTO> getOrder(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }
}