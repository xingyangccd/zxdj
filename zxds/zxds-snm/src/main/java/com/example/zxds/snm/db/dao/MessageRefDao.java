package com.example.zxds.snm.db.dao;

import com.example.zxds.snm.db.pojo.MessageRefEntity;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageRefDao {

    private final MongoTemplate mongoTemplate;

    // 保存消息的接收者信息
    public String insert(MessageRefEntity entity) {
        return mongoTemplate.save(entity).get_id();
    }

    // 查询某个用户未读消息数量
    public long searchUnreadCount(long userId, String identity) {
        // 封装查询条件
        Query query = Query.query(Criteria.where("readFlag").is(false).and("receiverId").is(userId).and("receiverIdentity").is(identity));
        // 执行查询，查询记录条数，方法固定：count
        return mongoTemplate.count(query, MessageRefEntity.class);
    }

    // 清除该用户的"最后一条消息"标记
    public long clearLastFlag(long userId, String identity) {
        // 执行条件
        Query query = Query.query(Criteria.where("lastFlag").is(true).and("receiverId").is(userId).and("receiverIdentity").is(identity));
        // 封装一个更新对象
        Update update = new Update().set("lastFlag", false);
        // 执行更新。
        UpdateResult result = mongoTemplate.updateMulti(query, update, "message_ref");
        // 返回更新的记录条数
        return result.getModifiedCount();
    }

    // 将指定ID的消息状态标记为已读
    public long updateUnreadMessage(String id) {
        // 封装执行条件
        Query query = Query.query(Criteria.where("_id").is(id));
        // 封装更新对象
        Update update = new Update().set("readFlag", true);
        // 执行更新
        UpdateResult result = mongoTemplate.updateFirst(query, update, "message_ref");
        // 返回更新的记录条数
        return result.getModifiedCount();
    }

    // 根据id删除message_ref中的一条记录
    public long deleteMessageRefById(String id) {
        // 封装执行条件
        Query query = Query.query(Criteria.where("_id").is(id));
        // 执行删除操作
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        // 返回删除的记录条数
        return result.getDeletedCount();
    }

    // 删除某个用户在message_ref中的所有记录
    public long deleteUserMessageRef(long userId, String identity) {
        // 封装执行条件
        Query query = Query.query(Criteria.where("receiverId").is(userId).and("receiverIdentity").is(identity));
        // 执行删除
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        // 返回删除的记录条数
        return result.getDeletedCount();
    }
}