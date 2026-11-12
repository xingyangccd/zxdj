package com.example.zxds.bff.customer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.customer.controller.form.ReceiveBillMessageForm;
import com.example.zxds.bff.customer.service.MessageService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "MessageController", description = "消息模块网络接口")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/bill")
    @SaCheckLogin
    @Operation(summary = "接收账单消息")
    public R receiveBillMessage(@Valid ReceiveBillMessageForm form) {
        long customerId = StpUtil.getLoginIdAsLong();
        form.setUserId(customerId);
        form.setIdentity("customer_bill");

        String msg = messageService.receiveBillMessage(form);
        return R.ok().put("result", msg);
    }
}