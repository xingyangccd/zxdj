package com.example.lockdemo.controller;

import com.example.lockdemo.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 初始化库存：GET /stock/init?productId=1001&stock=100
     */
    @GetMapping("/init")
    public String init(@RequestParam Long productId, @RequestParam int stock) {
        stockService.initStock(productId, stock);
        return "初始化成功，商品 " + productId + " 库存：" + stock;
    }

    /**
     * 扣减库存：GET /stock/deduct?productId=1001&quantity=1
     */
    @GetMapping("/deduct")
    public String deduct(@RequestParam Long productId, @RequestParam int quantity) {
        return stockService.deductStock(productId, quantity);
    }

    /**
     * 查询库存：GET /stock/query?productId=1001
     */
    @GetMapping("/query")
    public String query(@RequestParam Long productId) {
        Integer stock = stockService.getStock(productId);
        return "商品 " + productId + " 当前库存：" + stock;
    }
}