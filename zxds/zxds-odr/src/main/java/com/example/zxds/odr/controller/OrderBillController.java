package com.example.zxds.odr.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.zxds.common.util.R;
import com.example.zxds.odr.controller.form.SearchReviewDriverOrderBillForm;
import com.example.zxds.odr.controller.form.UpdateBillFeeForm;
import com.example.zxds.odr.controller.form.UpdateBillPaymentForm;
import com.example.zxds.odr.service.OrderBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
@Tag(name = "OrderBillController", description = "订单费用账单Web接口")
public class OrderBillController {

    private final OrderBillService orderBillService;

    @PutMapping
    @Operation(summary = "更新订单账单费用")
    public R updateBillFee(@RequestBody @Valid UpdateBillFeeForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = orderBillService.updateBillFee(param);
        return R.ok().put("rows", rows);
    }

    @GetMapping("/driver-order-bills/review")
    @Operation(summary = "查询司机预览账单")
    public R searchReviewDriverOrderBill(@Valid SearchReviewDriverOrderBillForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        Map<String, Object> map = orderBillService.searchReviewDriverOrderBill(param);
        return R.ok().put("result", map);
    }

    @PutMapping("/payment")
    @Operation(summary = "更新账单实际支付金额")
    public R updateBillPayment(@RequestBody @Valid UpdateBillPaymentForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        int rows = orderBillService.updateBillPayment(param);
        return R.ok().put("rows", rows);
    }
}