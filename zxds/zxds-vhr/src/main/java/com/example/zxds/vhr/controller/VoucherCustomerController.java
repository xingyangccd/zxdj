package com.example.zxds.vhr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.vhr.controller.form.UseVoucherForm;
import com.example.zxds.vhr.service.VoucherCustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/voucher/customer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "VoucherCustomerController", description = "客户代金券web接口")
public class VoucherCustomerController {
    private final VoucherCustomerService voucherCustomerService;

    @PostMapping
    @Operation(summary = "使用代金券")
    public R useVoucher(@RequestBody @Valid UseVoucherForm form){
        Map<String, Object> param = BeanUtil.beanToMap(form);
        String discount = voucherCustomerService.useVoucher(param);
        return R.ok().put("result", discount);
    }
}
