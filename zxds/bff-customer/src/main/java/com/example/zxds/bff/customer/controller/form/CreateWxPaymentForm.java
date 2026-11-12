package com.example.zxds.bff.customer.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建支付订单")
public class CreateWxPaymentForm {
    @NotNull(message = "orderId不为空")
    @Min(value = 1, message = "orderId不能小于1")
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "客户ID")
    private Long customerId;

    @Min(value = 1, message = "voucherId不能小于1")
    @Schema(description = "代金券ID")
    private Long voucherId;
}