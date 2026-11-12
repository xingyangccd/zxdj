package com.example.zxds.snm.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.snm.controller.form.ClearNewOrderQueueForm;
import com.example.zxds.snm.controller.form.DeleteNewOrderQueueForm;
import com.example.zxds.snm.controller.form.ReceiveNewOrderMessageForm;
import com.example.zxds.snm.controller.form.SendNewOrderMessageForm;
import com.example.zxds.snm.entity.NewOrderMessage;
import com.example.zxds.snm.task.NewOrderMessageTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/message/new-orders")
@RequiredArgsConstructor
@Tag(name = "NewOrderMessageController", description = "新订单消息Web接口")
public class NewOrderMessageController {

    private final NewOrderMessageTask task;

    @PostMapping("/async")
    @Operation(summary = "异步发送新订单消息")
    public R sendNewOrderMessageAsync(@RequestBody @Valid SendNewOrderMessageForm form) {
        task.sendNewOrderMessageAsync(buildMessage(form));
        return R.ok();
    }

    @PostMapping
    @Operation(summary = "同步发送新订单消息")
    public R sendNewOrderMessage(@RequestBody @Valid SendNewOrderMessageForm form) {
        task.sendNewOrderMessage(buildMessage(form));
        return R.ok();
    }

    // 将form转换成list集合。
    private List<NewOrderMessage> buildMessage(SendNewOrderMessageForm form) {
        List<NewOrderMessage> list = new ArrayList<>();
        for(String one: form.getDriversContent()){
            // one是什么？"1111#3"
            String[] temp = one.split("#");
            // 创建 NewOrderMessage 对象
            NewOrderMessage message = new NewOrderMessage();

            // 调用set方法给对象的属性赋值。
            message.setUserId(temp[0]);
            message.setDistance(temp[1]);
            message.setOrderId(form.getOrderId().toString());
            message.setFrom(form.getFrom());
            message.setTo(form.getTo());
            message.setMileage(form.getMileage());
            message.setMinute(form.getMinute().toString());
            message.setExpectsFee(form.getExpectsFee());
            message.setFavourFee(form.getFavourFee());

            list.add(message);
        }
        return list;
    }

    @GetMapping("/received")
    @Operation(summary = "接收新订单消息")
    public R receiveNewOrderMessage(@Valid ReceiveNewOrderMessageForm form) {
        List<NewOrderMessage> list = task.receiveNewOrderMessage(form.getUserId());
        return R.ok().put("result", list);
    }

    @DeleteMapping("/queue")
    @Operation(summary = "同步删除消息队列")
    public R deleteNewOrderQueue(@RequestBody @Valid DeleteNewOrderQueueForm form) {
        task.deleteNewOrderQueue(form.getUserId());
        return R.ok();
    }

    @DeleteMapping("/queue/async")
    @Operation(summary = "异步删除消息队列")
    public R deleteNewOrderQueueAsync(@RequestBody @Valid DeleteNewOrderQueueForm form) {
        task.deleteNewOrderQueueAsync(form.getUserId());
        return R.ok();
    }

    @DeleteMapping("/queue/message")
    @Operation(summary = "同步清空消息队列中的消息")
    public R clearNewOrderQueue(@RequestBody @Valid ClearNewOrderQueueForm form) {
        task.clearNewOrderQueue(form.getUserId());
        return R.ok();
    }

    @DeleteMapping("/queue/message/async")
    @Operation(summary = "异步清空消息队列中的消息")
    public R clearNewOrderQueueAsync(@RequestBody @Valid ClearNewOrderQueueForm form) {
        task.clearNewOrderQueueAsync(form.getUserId());
        return R.ok();
    }

}



















