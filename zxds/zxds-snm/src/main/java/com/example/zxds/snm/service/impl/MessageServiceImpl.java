package com.example.zxds.snm.service.impl;

import com.example.zxds.snm.db.dao.MessageDao;
import com.example.zxds.snm.db.dao.MessageRefDao;
import com.example.zxds.snm.db.pojo.MessageEntity;
import com.example.zxds.snm.db.pojo.MessageRefEntity;
import com.example.zxds.snm.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageDao messageDao;

    private final MessageRefDao messageRefDao;

    @Override
    public String insertMessage(MessageEntity entity) {
        return messageDao.insert(entity);
    }

    @Override
    public List<HashMap> searchMessageByPage(long userId, String identity, long start, int length) {
        return messageDao.searchMessageByPage(userId, identity, start, length);
    }

    @Override
    public HashMap searchMessageById(String id) {
        return messageDao.searchMessageById(id);
    }

    @Override
    public String insertRef(MessageRefEntity entity) {
        return messageRefDao.insert(entity);
    }

    @Override
    public long searchUnreadCount(long userId, String identity) {
        return messageRefDao.searchUnreadCount(userId, identity);
    }

    @Override
    public long clearLastFlag(long userId, String identity) {
        return messageRefDao.clearLastFlag(userId, identity);
    }

    @Override
    public long updateUnreadMessage(String id) {
        return messageRefDao.updateUnreadMessage(id);
    }

    @Override
    public long deleteMessageRefById(String id) {
        return messageRefDao.deleteMessageRefById(id);
    }

    @Override
    public long deleteUserMessageRef(long userId, String identity) {
        return messageRefDao.deleteUserMessageRef(userId, identity);
    }
}