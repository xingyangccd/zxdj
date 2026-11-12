package com.example.zxds.snm.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "message")
public class MessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // MongoDB 文档主键，自动生成
    @Id
    private String _id;

    // 消息的业务唯一标识，唯一索引用于防止重复发送/重复入库
    @Indexed(unique = true)
    private String uuid;

    // 发送者用户 ID，如果是系统自动发送的，该属性值为 0
    @Indexed
    private Long senderId;

    // 发送者身份（司机 driver / 乘客 customer / 系统 system）
    private String senderIdentity;

    // 发送者头像 URL，默认使用系统头像
    private String senderPhoto = "http://static-1258386385.cos.ap-beijing.myqcloud.com/img/System.jpg";

    // 发送者名称（用户姓名），消息页面展示用
    private String senderName;

    // 消息正文
    private String msg;

    // 发送时间
    @Indexed
    private Date sendTime;
}