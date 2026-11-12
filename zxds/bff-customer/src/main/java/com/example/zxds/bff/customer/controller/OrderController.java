package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.customer.controller.form.*;
import com.example.zxds.bff.customer.service.OrderService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@Tag(name = "OrderController", description = "订单模块Web接口")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "创建新订单")
    @SaCheckLogin
    public R createNewOrder(@RequestBody @Valid CreateNewOrderForm form) {
        Long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Map<String, Object> result = orderService.createNewOrder(form);
        return R.ok().put("result", result);
    }

    @GetMapping("/status")
    @Operation(summary = "查询订单状态")
    @SaCheckLogin
    public R searchOrderStatus(@Valid SearchOrderStatusForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Integer status = orderService.searchOrderStatus(form);
        return R.ok().put("result", status);
    }

    @DeleteMapping("/unaccepted")
    @Operation(summary = "关闭没有司机接单的订单")
    @SaCheckLogin
    public R deleteUnAcceptOrder(@RequestBody @Valid DeleteUnAcceptOrderForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        String result = orderService.deleteUnAcceptOrder(form);
        return R.ok().put("result", result);
    }

    @GetMapping("/customer/current")
    @Operation(summary = "查询乘客当前订单")
    @SaCheckLogin
    public R getCustomerCurrentOrder() {
        long customerId = StpUtil.getLoginIdAsLong();
        HasCustomerCurrentOrderForm form = new HasCustomerCurrentOrderForm();
        form.setCustomerId(customerId);
        Map<String, Object> map = orderService.hasCustomerCurrentOrder(form);
        return R.ok().put("result", map);
    }

    @GetMapping("/move-info")
    @SaCheckLogin
    @Operation(summary = "根据ID查询订单信息")
    public R searchOrderForMoveById(@Valid SearchOrderForMoveByIdForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Map<String, Object> map = orderService.searchOrderForMoveById(form);
        return R.ok().put("result", map);
    }

    @PutMapping("/confirm-arrive")
    @SaCheckLogin
    @Operation(summary = "确定司机已经到达")
    public R confirmArriveStartPlace(@RequestBody @Valid ConfirmArriveStartPlaceForm form) {
        boolean result = orderService.confirmArriveStartPlace(form);
        return R.ok().put("result", result);
    }

    @GetMapping
    @SaCheckLogin
    @Operation(summary = "根据ID查询订单+账单+司机简要")
    public R searchOrderById(@Valid SearchOrderByIdForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);

        Map<String, Object> map = orderService.searchOrderById(form);
        return R.ok().put("result", map);
    }

    @PostMapping("/payments")
    @Operation(summary = "创建支付订单")
    @SaCheckLogin
    public R createWxPayment(@RequestBody @Valid CreateWxPaymentForm form) {
        Long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        Map<String, String> map = orderService.createWxPayment(form.getOrderId(), form.getCustomerId(), form.getVoucherId());
        return R.ok().put("result", map);
    }

    @PostMapping("/payment-status-sync")
    @Operation(summary = "主动查询付款结果及同步订单状态")
    @SaCheckLogin
    public R updateOrderAboutPayment(@RequestBody @Valid UpdateOrderAboutPaymentForm form) {
        String result = orderService.updateOrderAboutPayment(form);
        return R.ok().put("result", result);
    }

}