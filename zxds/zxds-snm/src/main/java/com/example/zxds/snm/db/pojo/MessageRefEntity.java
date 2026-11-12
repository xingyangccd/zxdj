package com.example.zxds.snm.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "message_ref")
public class MessageRefEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // MongoDB 文档主键，自动生成
    @Id
    private String _id;

    // 对应 message 集合中那条消息的 _id
    // 唯一索引：防止因 MQ 重复投递或并发消费导致同一条消息被多次处理，确保消息消费的幂等性
    @Indexed(unique = true)
    private String messageId;

    // 接收人用户 ID
    @Indexed
    private Long receiverId;

    // 接收人身份（司机 driver / 乘客 customer）
    private String receiverIdentity;

    // 是否已读
    @Indexed
    private Boolean readFlag;

    // 是否为新接收的消息（用于标记最新/未读提示）
    @Indexed
    private Boolean lastFlag;
}