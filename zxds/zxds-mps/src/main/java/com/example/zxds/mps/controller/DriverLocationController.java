package com.example.zxds.mps.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.mps.controller.form.*;
import com.example.zxds.mps.service.DriverLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/driver/location")
@Tag(name = "DriverLocationController", description = "司机定位服务Web接口")
public class DriverLocationController {
    private final DriverLocationService driverLocationService;

    @PutMapping
    @Operation(summary = "更新司机的GPS定位缓存")
    public R updateLocationCache(@RequestBody @Valid UpdateLocationCacheForm form) {
        Map<String, Object> map = BeanUtil.beanToMap(form);
        driverLocationService.updateLocationCache(map);
        return R.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除司机GPS定位缓存")
    public R deleteLocationCache(@RequestBody @Valid RemoveLocationCacheForm form) {
        driverLocationService.removeLocationCache(form.getDriverId());
        return R.ok();
    }

    @PostMapping("/search")
    @Operation(summary = "查询符合某个订单接单的司机列表")
    public R searchBefittingDriverAboutOrder(@RequestBody @Valid SearchBefittingDriverAboutOrderForm form) {

        double startPlaceLatitude = Double.parseDouble(form.getStartPlaceLatitude());
        double startPlaceLongitude = Double.parseDouble(form.getStartPlaceLongitude());
        double endPlaceLatitude = Double.parseDouble(form.getEndPlaceLatitude());
        double endPlaceLongitude = Double.parseDouble(form.getEndPlaceLongitude());
        double mileage = Double.parseDouble(form.getMileage());

        List<Map<String, Object>> list = driverLocationService.searchBefittingDriverAboutOrder(
                startPlaceLatitude,
                startPlaceLongitude,
                endPlaceLatitude,
                endPlaceLongitude,
                mileage);

        return R.ok().put("result", list);
    }

    @PutMapping("/order-location")
    @Operation(summary = "更新订单定位缓存")
    public R updateOrderLocationCache(@RequestBody @Valid UpdateOrderLocationCacheForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        driverLocationService.updateOrderLocationCache(param);
        return R.ok();
    }

    @GetMapping("/order-location")
    @Operation(summary = "查询订单定位缓存")
    public R searchOrderLocationCache(@Valid SearchOrderLocationCacheForm form) {
        Map<String, Object> map = driverLocationService.searchOrderLocationCache(form.getOrderId());
        return R.ok().put("result", map);
    }

}

















