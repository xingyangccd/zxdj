package com.example.zxds.cst.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.cst.controller.form.*;
import com.example.zxds.cst.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/customer")
@Tag(name = "CustomerController", description = "客户Web接口")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "乘客注册")
    public R registerNewCustomer(@RequestBody @Valid RegisterNewCustomerForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        String userId = customerService.registerNewCustomer(param);
        return R.ok().put("userId", userId);
    }

    @PostMapping("/token")
    @Operation(summary = "乘客登录")
    public R login(@RequestBody @Valid LoginForm form) {
        String userId = customerService.login(form.getCode());
        return R.ok().put("userId", userId);
    }

    @GetMapping("/info")
    @Operation(summary = "查询订单中的客户信息")
    public R searchCustomerInfoInOrder(@Valid SearchCustomerInfoInOrderForm form) {
        Map<String, Object> map = customerService.searchCustomerInfoInOrder(form.getCustomerId());
        return R.ok().put("result", map);
    }

    @GetMapping("/brief-info") // 简要信息
    @Operation(summary = "查询客户简明信息")
    public R searchCustomerBriefInfo(@Valid SearchCustomerBriefInfoForm form) {
        Map<String, Object> map = customerService.searchCustomerBriefInfo(form.getCustomerId());
        return R.ok().put("result", map);
    }

    @GetMapping("/open-id")
    @Operation(summary = "查询客户的OpenId")
    public R searchCustomerOpenId(@Valid SearchCustomerOpenIdForm form) {
        String openId = customerService.searchCustomerOpenId(form.getCustomerId());
        return R.ok().put("result", openId);
    }
}