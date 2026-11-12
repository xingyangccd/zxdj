package com.demo.bff.dto;

import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    private String status;
    private String productName;
}