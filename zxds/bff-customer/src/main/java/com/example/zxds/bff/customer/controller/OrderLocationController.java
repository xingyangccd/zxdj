package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.zxds.bff.customer.controller.form.SearchOrderLocationCacheForm;
import com.example.zxds.bff.customer.service.OrderLocationService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/order/location")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OrderLocationController", description = "订单定位服务Web接口")
public class OrderLocationController {
    private final OrderLocationService orderLocationService;

    @GetMapping("/cache")
    @Operation(summary = "查询订单定位缓存")
    @SaCheckLogin
    public R searchOrderLocationCache(@Valid SearchOrderLocationCacheForm form) {
        Map<String, Object> map = orderLocationService.searchOrderLocationCache(form);
        return R.ok().put("result", map);
    }
}
