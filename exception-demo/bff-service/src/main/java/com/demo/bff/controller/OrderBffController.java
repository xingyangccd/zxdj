package com.demo.bff.controller;

import com.demo.bff.client.OrderClientWrapper;
import com.demo.bff.common.R;
import com.demo.bff.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderBffController {

    private final OrderClientWrapper orderClientWrapper;

    @GetMapping("/bff/orders/{id}")
    public R<OrderDTO> getOrder(@PathVariable Long id) {
        return R.ok(orderClientWrapper.getOrder(id));
    }
}