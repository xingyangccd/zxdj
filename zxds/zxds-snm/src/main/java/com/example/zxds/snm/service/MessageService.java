package com.example.zxds.snm.service;

import com.example.zxds.snm.db.pojo.MessageEntity;
import com.example.zxds.snm.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {
    // 保存一条消息主体记录（message集合）
    String insertMessage(MessageEntity entity);

    // 根据 id 查询消息详情（message集合）
    HashMap searchMessageById(String id);

    // 分页查询某个用户的消息列表（message和message_ref两个集合连接查询）
    List<HashMap> searchMessageByPage(long userId, String identity, long start, int length);

    // 保存一条消息与接收人的关联记录（message_ref集合）
    String insertRef(MessageRefEntity entity);

    // 查询某个用户的未读消息数量（message_ref集合）
    long searchUnreadCount(long userId, String identity);

    // 清除该用户所有"最后一条消息"的标记（message_ref集合）
    long clearLastFlag(long userId, String identity);

    // 将指定 ID 的消息关联记录标记为已读（message_ref集合）
    long updateUnreadMessage(String id);

    // 根据 id 删除单条消息关联记录（message_ref集合）
    long deleteMessageRefById(String id);

    // 删除某个用户的所有消息关联记录（message_ref集合）
    long deleteUserMessageRef(long userId, String identity);
}