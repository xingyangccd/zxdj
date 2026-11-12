package com.example.zxds.mis.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.example.zxds.common.util.PageUtils;
import com.example.zxds.common.util.R;
import com.example.zxds.mis.api.controller.form.SearchOrderByPageForm;
import com.example.zxds.mis.api.controller.form.SearchOrderComprehensiveInfoForm;
import com.example.zxds.mis.api.controller.form.SearchOrderLastGpsForm;
import com.example.zxds.mis.api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "OrderController", description = "订单管理Web接口")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/page")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "分页查询订单记录")
    public R searchOrderByPage(@RequestBody @Valid SearchOrderByPageForm form) {
        PageUtils pageUtils = orderService.searchOrderByPage(form);
        return R.ok().put("result", pageUtils);
    }

    @GetMapping("/comprehensive-info")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "查询订单")
    public R searchOrderComprehensiveInfo(@Valid SearchOrderComprehensiveInfoForm form) {
        Map<String, Object> result = orderService.searchOrderComprehensiveInfo(form.getOrderId());
        return R.ok().put("result", result);
    }

    @GetMapping("/last-gps")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "获取某个订单最后的GPS定位")
    public R searchOrderLastGps(@Valid SearchOrderLastGpsForm form) {
        Map<String, Object> result = orderService.searchOrderLastGps(form);
        return R.ok().put("result", result);
    }
}
