package com.example.zxds.snm.task;

import com.example.zxds.common.exception.ZxdsException;
import com.example.zxds.snm.db.pojo.MessageEntity;
import com.example.zxds.snm.db.pojo.MessageRefEntity;
import com.example.zxds.snm.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MessageTask {

    // Task其实也是一个Service的角色。
    // 在Task当中编写收发消息的核心逻辑。
    // 收发消息的时候需要同时操作 MongoDB + MQ
    private final RabbitTemplate rabbitTemplate; // MQ
    private final MessageService messageService; // MongoDB

    public MessageTask(@Qualifier("stringRabbitTemplate") RabbitTemplate rabbitTemplate, MessageService messageService) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageService = messageService;
    }

    /**
     * 同步发送私有消息
     */
    public void sendPrivateMessage(String identity, long userId, Integer ttl, MessageEntity entity) {
        try {
            // 将消息保存到MongoDB（message集合）
            String id = messageService.insertMessage(entity);

            String exchangeName = identity + "_private"; // 以 身份标识 作为交换机
            String routingKey = String.valueOf(userId); // 以userId作为路由键
            String queueName = "queue_" + identity + "_" + userId;

            // 确保队列存在并绑定
            rabbitTemplate.execute(channel -> {
                // 声明队列
                channel.queueDeclare(queueName, true, false, false, null);
                // 队列 交换机 通过路由键进行绑定。
                channel.queueBind(queueName, exchangeName, routingKey);
                return null;
            });

            Map<String, Object> headers = new HashMap<>();
            headers.put("messageId", id);

            rabbitTemplate.convertAndSend(exchangeName, routingKey, entity.getMsg(), message -> {
                message.getMessageProperties().getHeaders().putAll(headers);
                message.getMessageProperties().setContentEncoding("UTF-8");
                if (ttl != null && ttl > 0) {
                    message.getMessageProperties().setExpiration(ttl.toString());
                }
                return message;
            });

            log.debug("私有消息发送成功, userId: {}", userId);
        } catch (Exception e) {
            log.error("发送私有消息异常", e);
            throw new ZxdsException("向MQ发送消息失败");
        }
    }

    /**
     * 异步发送私有消息
     */
    @Async
    public void sendPrivateMessageAsync(String identity, long userId, Integer ttl, MessageEntity entity) {
        sendPrivateMessage(identity, userId, ttl, entity);
    }

    /**
     * 同步发送广播消息
     */
    public void sendBroadcastMessage(String identity, Integer ttl, MessageEntity entity) {
        try {
            // 保存到MongoDB
            String id = messageService.insertMessage(entity);

            // 广播消息（交换机已在配置类创建）
            String exchangeName = identity + "_broadcast";

            Map<String, Object> headers = new HashMap<>();
            headers.put("messageId", id);

            rabbitTemplate.convertAndSend(exchangeName, "", entity.getMsg(), message -> {
                message.getMessageProperties().getHeaders().putAll(headers);
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message.getMessageProperties().setContentEncoding("UTF-8");
                if (ttl != null && ttl > 0) {
                    message.getMessageProperties().setExpiration(ttl.toString());
                }
                return message;
            });

            log.debug("广播消息发送成功");
        } catch (Exception e) {
            log.error("发送广播消息异常", e);
            throw new ZxdsException("向MQ发送消息失败");
        }
    }

    /**
     * 异步发送广播消息
     */
    @Async
    public void sendBroadcastMessageAsync(String identity, Integer ttl, MessageEntity entity) {
        sendBroadcastMessage(identity, ttl, entity);
    }

    /**
     * 同步接收消息
     */
    public int receive(String identity, long userId) {
        String queueName = "queue_" + identity + "_" + userId;
        int count = 0;

        try {
            // 确保队列存在，并绑定到对应的私有交换机和广播交换机
            rabbitTemplate.execute(channel -> {
                // 队列的声明（幂等性）
                channel.queueDeclare(queueName, true, false, false, null);
                // 绑定私有交换机
                channel.queueBind(queueName, identity + "_private", String.valueOf(userId));
                // 绑定广播交换机
                channel.queueBind(queueName, identity + "_broadcast", "");
                return null;
            });

            // 接收私有消息
            count += receiveMessages(queueName, identity, userId, "私有");
            // 接收广播消息
            count += receiveMessages(queueName, identity, userId, "广播");

            return count;
        } catch (Exception e) {
            log.error("接收消息异常", e);
            throw new ZxdsException("接收消息失败");
        }
    }

    /**
     * 异步接收消息
     */
    @Async
    public void receiveAsync(String identity, long userId) {
        receive(identity, userId);
    }

    /**
     * 接收消息通用方法
     */
    private int receiveMessages(String queueName, String identity, long userId, String type) {
        int count = 0;

        while (true) {
            Message message = rabbitTemplate.receive(queueName);
            if (message == null) {
                break;
            }

            try {
                Map<String, Object> headers = message.getMessageProperties().getHeaders();
                // 这里是从MQ消息头上获取的messageId（message集合中的_id主键值。）
                Object idObj = headers.get("messageId");
                if (idObj == null) {
                    log.warn("接收到的{}消息缺失messageId，直接丢弃", type);
                    continue;
                }

                // 消息的ID以及消息内容。
                String messageId = idObj.toString();
                String content = new String(message.getBody(), StandardCharsets.UTF_8);

                log.debug("从RabbitMQ接收的{}消息：{}", type, content);

                MessageRefEntity refEntity = new MessageRefEntity();
                refEntity.setMessageId(messageId);
                refEntity.setReceiverId(userId);
                refEntity.setReceiverIdentity(identity);
                refEntity.setReadFlag(false);
                refEntity.setLastFlag(true);

                // 接收消息后，将接收者的状态信息保存到MongoDB
                messageService.insertRef(refEntity);

                count++;
            } catch (Exception e) {
                log.error("处理{}消息异常，消息已从队列移除，messageId: {}", type, message.getMessageProperties().getHeaders().get("messageId"), e);
            }
        }

        return count;
    }


    /**
     * 同步删除消息队列
     */
    public void deleteQueue(String identity, long userId) {
        String queueName = "queue_" + identity + "_" + userId;
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDelete(queueName);
                return null;
            });
            log.debug("消息队列删除成功: {}", queueName);
        } catch (Exception e) {
            log.error("删除队列失败", e);
            throw new ZxdsException("删除队列失败");
        }
    }

    /**
     * 异步删除消息队列
     */
    @Async
    public void deleteQueueAsync(String identity, long userId) {
        deleteQueue(identity, userId);
    }

    /**
     * 专门接收账单消息的方法（只接收一条消息，并且接收即表示已读）
     */
    public String receiveBillMessage(String identity, long userId) {
        // identity 传入的是 "customer_bill"
        String exchangeName = identity + "_private"; // customer_bill_private
        String queueName = "queue_" + identity + "_" + userId; // queue_customer_bill_1001
        String routingKey = String.valueOf(userId);

        try {
            // 确保队列存在并绑定
            rabbitTemplate.execute(channel -> {
                channel.queueDeclare(queueName, true, false, false, null);
                channel.queueBind(queueName, exchangeName, routingKey);
                return null;
            });

            // 接收一条消息（因为队列已经隔离，拿到的一定是账单）
            Message message = rabbitTemplate.receive(queueName);
            if (message == null) {
                return "";
            }

            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            Object idObj = headers.get("messageId");
            if (idObj == null) return "";

            String messageId = idObj.toString();
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);

            log.debug("从RabbitMQ接收的账单消息：{}", msg);

            MessageRefEntity entity = new MessageRefEntity();
            entity.setMessageId(messageId);
            entity.setReceiverId(userId);
            entity.setReceiverIdentity(identity);
            entity.setReadFlag(true); // 账单接收即已读
            entity.setLastFlag(true);
            messageService.insertRef(entity);

            return msg;
        } catch (Exception e) {
            log.error("接收账单消息异常", e);
            throw new ZxdsException("接收账单消息失败");
        }
    }
}