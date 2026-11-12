package com.example.zxds.bff.driver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.zxds.bff.driver.controller.form.ReceiveNewOrderMessageForm;
import com.example.zxds.bff.driver.service.NewOrderMessageService;
import com.example.zxds.common.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message/new-orders")
@RequiredArgsConstructor
@Tag(name = "NewOrderMessageController", description = "新订单消息Web接口")
public class NewOrderMessageController {

    private final NewOrderMessageService newOrderMessageService;

    @GetMapping("/received")
    @Operation(summary = "接收新订单消息")
    @SaCheckLogin
    public R receiveNewOrderMessage() {
        long driverId = StpUtil.getLoginIdAsLong();
        ReceiveNewOrderMessageForm form = new ReceiveNewOrderMessageForm();
        form.setUserId(driverId);
        List<Map<String, Object>> list = newOrderMessageService.receiveNewOrderMessage(form);
        return R.ok().put("result", list);
    }
}