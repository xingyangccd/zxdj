package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.customer.controller.form.DeleteCustomerCarByIdForm;
import com.example.zxds.bff.customer.controller.form.InsertCustomerCarForm;
import com.example.zxds.bff.customer.controller.form.SearchCustomerCarListForm;
import com.example.zxds.bff.customer.service.CustomerCarService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/car")
@RequiredArgsConstructor
@Tag(name = "CustomerCarController", description = "客户车辆Web接口")
public class CustomerCarController {

    private final CustomerCarService customerCarService;

    @PostMapping
    @Operation(summary = "添加客户车辆")
    @SaCheckLogin
    public R insertCustomerCar(@RequestBody @Valid InsertCustomerCarForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        customerCarService.insertCustomerCar(form);
        return R.ok();
    }

    @PostMapping("/search")
    @Operation(summary = "查询客户车辆列表")
    @SaCheckLogin
    public R searchCustomerCarList(@RequestBody @Valid SearchCustomerCarListForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setCustomerId(customerId);
        List<Map<String, Object>> list = customerCarService.searchCustomerCarList(form);
        return R.ok().put("result", list);
    }

    @DeleteMapping
    @Operation(summary = "删除客户车辆")
    @SaCheckLogin
    public R deleteCustomerCarById(@RequestBody @Valid DeleteCustomerCarByIdForm form) {
        int rows = customerCarService.deleteCustomerCarById(form);
        return R.ok().put("rows", rows);
    }
}