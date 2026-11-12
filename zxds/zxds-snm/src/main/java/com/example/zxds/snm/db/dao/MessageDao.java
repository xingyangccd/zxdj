package com.example.zxds.snm.db.dao;

import cn.hutool.json.JSONObject;
import com.example.zxds.snm.db.pojo.MessageEntity;
import com.example.zxds.snm.db.pojo.MessageRefEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

// 操作 message 集合
@Repository
@RequiredArgsConstructor
public class MessageDao {

    // 统一按北京时间展示。
    // 服务器在北京，上午 10:00 存消息，MongoDB 中实际保存的是该时刻对应的 UTC 时间戳（02:00）。
    // 取出的 Date 是同一绝对时刻，展示时统一按北京时区格式化，才能显示为 10:00。
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(BEIJING_ZONE);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(BEIJING_ZONE);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(BEIJING_ZONE);

    private final MongoTemplate mongoTemplate;

    // 保存消息（返回值是：保存的那一条文档的 _id 【主键】）
    public String insert(MessageEntity entity) {
        return mongoTemplate.save(entity).get_id();
    }

    // 根据 id 查询消息
    public HashMap searchMessageById(String id) {
        HashMap map = mongoTemplate.findById(id, HashMap.class, "message");
        if (map == null) {
            return null;
        }
        Date sendTime = (Date) map.get("sendTime");
        // 做日期的格式化处理，因为mongodb当中存储的日期是UTC时间戳，需要根据东八区进行日期时间格式化。
        map.replace("sendTime", DATETIME_FORMATTER.format(sendTime.toInstant()));
        return map;
    }

    // 分页查询某个用户的消息
    // message 和 message_ref 关联查询，因为消息正文是存储在 message 当中的
    public List<HashMap> searchMessageByPage(long userId, String identity, long start, int length) {
        // 1. 把 MongoDB 的 ObjectId（_id）转成字符串，用于关联查询。
        //    直接按 _id 查询时不需要转换，但 message_ref 中的 messageId 是字符串类型，所以这里要转。
        JSONObject json = new JSONObject();
        // $toString 是 MongoDB 的类型转换操作符，$_id 表示当前文档的 _id 字段
        json.set("$toString", "$_id");

        // 2. 构建聚合查询：关联 message 和 message_ref，分页获取用户消息
        Aggregation aggregation = Aggregation.newAggregation(
                // 2.1 给 message 临时添加 id 字段，值为上面转换后的字符串
                Aggregation.addFields().addField("id").withValue(json).build(),
                // 2.2 关联 message_ref：message.id = message_ref.messageId。
                //     结果是一个数组，命名为 ref，包含该消息的所有接收者。
                Aggregation.lookup("message_ref", "id", "messageId", "ref"),
                // 2.3 只保留当前用户的数据，过滤后 ref 中只剩 1 个元素
                Aggregation.match(Criteria.where("ref.receiverId").is(userId).and("ref.receiverIdentity").is(identity)),
                // 2.4 按发送时间降序排列
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "sendTime")),
                // 2.5 分页：跳过指定条数
                Aggregation.skip(start),
                // 2.6 分页：限制返回条数
                Aggregation.limit(length));
        // 3. 执行查询，从 message 集合开始聚合
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "message", HashMap.class);
        List<HashMap> list = results.getMappedResults();

        /*
            [
                {
                    "_id": ObjectId(_id),
                    "id" : "65a4f1b7c3e4b5f6a7b8c9d0",
                    "uuid": "xx",
                    "senderId": 10001,
                    "sendIdentity": "driver",
                    "senderPhoto": "发送人的头像",
                    "senderName": "发送人的名字",
                    "msg": "您好，我已经到达上车点",
                    "sendTime": "2024-01-15T06:30:00.000Z",
                    "ref": [
                        {
                            "_id": "ref111",
                            "messageId": "65a4f1b7c3e4b5f6a7b8c9d0",
                            "receiverId": 1001,
                            "readFlag": false,
                            "receiverIdentity": "student",
                            "lastFlag": true
                        }
                    ]
                },
                {
                    "_id": "65a4f1b7c3e4b5f6a7b8c9d1",
                    .....
                    "msg": "作业提醒",
                    "sendTime": "2024-01-14T08:30:00.000Z",
                    "ref": [
                        {
                            "_id": "ref222",
                            "messageId": "65a4f1b7c3e4b5f6a7b8c9d1",
                            "receiverId": 1001,
                            "readFlag": true,
                            "receiverIdentity": "student",
                            "lastFlag": false
                        }
                    ]
                }
            ]
        */
        // 4. 处理结果集：提取状态信息、格式化时间
        for (HashMap one : list) {
            // 4.1 取出当前用户的消息状态记录（已过滤，只有 1 条）
            List<MessageRefEntity> refList = (List<MessageRefEntity>) one.get("ref");
            if (refList == null || refList.isEmpty()) {
                continue;
            }
            MessageRefEntity entity = refList.get(0);

            // 4.2 把状态信息放入外层 Map
            one.put("readFlag", entity.getReadFlag());// 是否已读
            one.put("refId", entity.get_id());// 状态记录 ID

            // 4.3 删除关联数据和 MongoDB 内部 ID，保持返回结果简洁
            one.remove("ref");
            one.remove("_id");

            // 4.4 按北京时间格式化：今天显示时间，非今天显示日期
            Date sendTime = (Date) one.get("sendTime"); // 2024-01-14T08:30:00.000Z
            Instant instant = sendTime.toInstant();
            String today = DATE_FORMATTER.format(Instant.now()); // 获取今天的日期
            if (today.equals(DATE_FORMATTER.format(instant))) { // 条件成立表示这一条消息是今天的日期。
                // 今天的消息只存放时间，没有日期。
                one.put("sendTime", TIME_FORMATTER.format(instant));
            } else {
                // 不是今天的消息只存放日期，不存放时间。
                one.put("sendTime", DATE_FORMATTER.format(instant));
            }
        }

        /*
        最终结果：
            [
                {
                    "id" : "65a4f1b7c3e4b5f6a7b8c9d0",
                    "uuid": "xx",
                    "senderId": 10001,
                    "sendIdentity": "driver",
                    "senderPhoto": "发送人的头像",
                    "senderName": "发送人的名字",
                    "msg": "您好，我已经到达上车点",
                    "sendTime": "10:10",
                    "readFlag": false,
                    "refId": "ObjectId(....)"
                },
                {
                    "id" : "65a4f1b7c3e4b5f6a7b8c9d1",
                    "uuid": "xx",
                    "senderId": 10001,
                    "sendIdentity": "driver",
                    "senderPhoto": "发送人的头像",
                    "senderName": "发送人的名字",
                    "msg": "您好，我已经到达上车点",
                    "sendTime": "2010-10-10",
                    "readFlag": false,
                    "refId": "ObjectId(....)"
                }
            ]
        */
        return list;
    }
}