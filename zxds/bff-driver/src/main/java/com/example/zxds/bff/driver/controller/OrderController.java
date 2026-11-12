package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.driver.controller.form.*;
import com.example.zxds.bff.driver.service.OrderService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OrderController", description = "订单模块web接口")
public class OrderController {
    private final OrderService orderService;

    @PutMapping("/driver")
    @SaCheckLogin
    @Operation(summary = "司机抢单")
    public R acceptNewOrder(@RequestBody @Valid AcceptNewOrderForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        String result = orderService.acceptNewOrder(form);
        return R.ok().put("result", result);
    }

    @GetMapping("/driver/execute-order")
    @SaCheckLogin
    @Operation(summary = "查询司机正在执行的订单信息")
    public R searchExecuteOrder(@Valid SearchDriverExecuteOrderForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchDriverExecuteOrder(form);
        return R.ok().put("result", map);
    }

    @GetMapping("/driver/current")
    @SaCheckLogin
    @Operation(summary = "查询司机当前订单")
    public R searchDriverCurrentOrder() {
        long driverId = StpUtil.getLoginIdAsLong();
        SearchDriverCurrentOrderForm form = new SearchDriverCurrentOrderForm();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchDriverCurrentOrder(form);
        return R.ok().put("result", map);
    }

    @GetMapping("/move-info")
    @SaCheckLogin
    @Operation(summary = "查询订单信息用于司乘同显功能")
    public R searchOrderForMoveById(@Valid SearchOrderForMoveByIdForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchOrderForMoveById(form);
        return R.ok().put("result", map);
    }

    @PutMapping("/arrive")
    @Operation(summary = "司机到达上车点")
    @SaCheckLogin
    public R arriveStartPlace(@RequestBody @Valid ArriveStartPlaceForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = orderService.arriveStartPlace(form);
        return R.ok().put("result", rows);
    }

    @PutMapping("/start-driving")
    @Operation(summary = "开始代驾")
    @SaCheckLogin
    public R startDriving(@RequestBody @Valid StartDrivingForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = orderService.startDriving(form);
        return R.ok().put("rows", rows);
    }

    @PutMapping("/status")
    @SaCheckLogin
    @Operation(summary = "更新订单状态")
    public R updateOrderStatus(@RequestBody @Valid UpdateOrderStatusForm form) {
        int rows = orderService.updateOrderStatus(form);
        return R.ok().put("rows", rows);
    }

    @PutMapping("/bill")
    @SaCheckLogin
    @Operation(summary = "更新订单账单费用")
    public R updateBillFee(@RequestBody @Valid UpdateBillFeeForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        int rows = orderService.updateOrderBill(form);
        return R.ok().put("rows", rows);
    }

    @GetMapping("/driver-orders/review")
    @SaCheckLogin
    @Operation(summary = "查询司机预览订单")
    public R searchReviewDriverOrderBill(@Valid SearchReviewDriverOrderBillForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Map<String, Object> map = orderService.searchReviewDriverOrderBill(form);
        return R.ok().put("result", map);
    }

    @GetMapping("/status")
    @SaCheckLogin
    @Operation(summary = "查询订单状态")
    public R searchOrderStatus(@Valid SearchOrderStatusForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        form.setDriverId(driverId);
        Integer status = orderService.searchOrderStatus(form);
        return R.ok().put("result", status);
    }

    @PostMapping("/payment-status-sync")
    @SaCheckLogin
    @Operation(summary = "查看付款结果并更新订单、发放奖励、分账等")
    public R updateOrderAboutPayment(@RequestBody @Valid UpdateOrderAboutPaymentForm form) {
        long driverId = StpUtil.getLoginIdAsLong();
        String result = orderService.updateOrderAboutPayment(driverId, form);
        return R.ok().put("result", result);
    }

}
