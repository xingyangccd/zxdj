package com.demo.order.dto;

import lombok.Data;

@Data
public class OrderDTO {
    private Long id; // 订单ID
    private String status; // 订单的支付状态
    private String productName; // 商品名字
}