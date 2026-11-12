package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.driver.controller.form.UpdateLocationCacheForm;
import com.example.zxds.bff.driver.controller.form.UpdateOrderLocationCacheForm;
import com.example.zxds.bff.driver.service.DriverLocationService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/driver/location")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DriverLocationController", description = "司机定位服务Web接口")
public class DriverLocationController {
    private final DriverLocationService driverLocationService;

    @PutMapping
    @Operation(summary = "更新司机GPS定位缓存")
    @SaCheckLogin
    public R updateLocationCache(@RequestBody @Valid UpdateLocationCacheForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        driverLocationService.updateLocationCache(form);
        return R.ok();
    }

    @PutMapping("/order-location")
    @Operation(summary = "更新订单定位缓存")
    @SaCheckLogin
    public R updateOrderLocationCache(@RequestBody @Valid UpdateOrderLocationCacheForm form) {
        driverLocationService.updateOrderLocationCache(form);
        return R.ok();
    }
}
