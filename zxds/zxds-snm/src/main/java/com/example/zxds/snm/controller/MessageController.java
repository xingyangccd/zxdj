package com.example.zxds.snm.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.snm.controller.form.*;
import com.example.zxds.snm.db.pojo.MessageEntity;
import com.example.zxds.snm.service.MessageService;
import com.example.zxds.snm.task.MessageTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Tag(name = "MessageController", description = "消息模块网络接口")
public class MessageController {

    private final MessageService messageService;
    private final MessageTask messageTask;

    @Operation(summary = "根据ID查询消息")
    @GetMapping
    public R searchMessageById(@Valid SearchMessageByIdForm form) {
        Map<String, Object> map = messageService.searchMessageById(form.getId());
        return R.ok().put("result", map);
    }

    @Operation(summary = "未读消息更新成已读消息")
    @PutMapping("/read")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form) {
        long rows = messageService.updateUnreadMessage(form.getId());
        return R.ok().put("rows", rows);
    }

    @Operation(summary = "删除消息")
    @DeleteMapping
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form) {
        long rows = messageService.deleteMessageRefById(form.getId());
        return R.ok().put("rows", rows);
    }

    @Operation(summary = "刷新用户消息")
    @PutMapping("/refresh")
    public R refreshMessage(@Valid @RequestBody RefreshMessageForm form) {
        // 接收消息并保存到mongoDB
        messageTask.receive(form.getIdentity(), form.getUserId());

        // 将用户所有消息的 lastFlag 标记从 true 改为 false（效果：消除小红点/新消息提示）
        long lastRows = messageService.clearLastFlag(form.getUserId(), form.getIdentity());

        // 统计用户还未阅读的消息数量
        long unreadRows = messageService.searchUnreadCount(form.getUserId(), form.getIdentity());

        Map<String, Object> map = new HashMap<>();
        map.put("lastRows", String.valueOf(lastRows));
        map.put("unreadRows", String.valueOf(unreadRows));

        return R.ok().put("result", map);
    }

    @Operation(summary = "同步发送私有消息")
    @PostMapping
    public R sendPrivateMessage(@RequestBody @Valid SendPrivateMessageForm form) {
        // form转MessageEntity对象
        MessageEntity entity = buildMessageEntity(form);
        messageTask.sendPrivateMessage(form.getReceiverIdentity(), form.getReceiverId(), form.getTtl(), entity);
        return R.ok();
    }

    @Operation(summary = "异步发送私有消息")
    @PostMapping("/async")
    public R sendPrivateMessageAsync(@RequestBody @Valid SendPrivateMessageForm form) {
        MessageEntity entity = buildMessageEntity(form);
        messageTask.sendPrivateMessageAsync(form.getReceiverIdentity(), form.getReceiverId(), form.getTtl(), entity);
        return R.ok();
    }

    @GetMapping("/bill")
    @Operation(summary = "同步接收账单消息")
    public R receiveBillMessage(@Valid ReceiveBillMessageForm form) {
        String msg = messageTask.receiveBillMessage(form.getIdentity(), form.getUserId());
        return R.ok().put("result", msg);
    }

    /**
     * 抽取：将表单数据转换为消息实体
     */
    private MessageEntity buildMessageEntity(SendPrivateMessageForm form) {
        MessageEntity entity = new MessageEntity();
        entity.setSenderId(form.getSenderId());
        entity.setSenderIdentity(form.getSenderIdentity());
        entity.setSenderPhoto(form.getSenderPhoto());
        entity.setSenderName(form.getSenderName());
        entity.setMsg(form.getMsg());
        entity.setSendTime(new Date());
        return entity;
    }
}