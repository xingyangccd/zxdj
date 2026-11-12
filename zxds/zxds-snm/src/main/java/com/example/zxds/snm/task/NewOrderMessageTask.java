package com.example.zxds.snm.task;

import com.example.zxds.snm.entity.NewOrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 新订单消息的任务类：
 * 1.发送消息
 * 2.接收消息
 * 3.删除消息队列（司机注销账号了，队列直接删除。）
 * 4.清空消息队列当中的消息
 * 以上方法均提供同步方式和异步方式。
 */
@Component
@Slf4j
//@RequiredArgsConstructor
public class NewOrderMessageTask {

    // SpringBoot自动配置的 RabbitTemplate实例，核心对象，用它发送消息和接收消息。
    private final RabbitTemplate rabbitTemplate;
    // Spring AMQP管理接口，用于管理交换机和队列等。
    private final AmqpAdmin amqpAdmin;
    // 交换机的名称
    private static final String EXCHANGE_NAME = "new_order_private";
    // 消息过期时间：1分钟（订单消息发过来1分钟之内没有消费，自动删除。）
    private static final int TTL = 60 * 1000;
    // 队列闲置过期时间：7天，长期不接单的司机，超过7天，我们把队列删除掉，防止队列太多。
    private static final int QUEUE_EXPIRES = 7 * 24 * 60 * 60 * 1000; // 毫秒

    // 手动的将构造方法定义出来，避免bean重复的问题
    public NewOrderMessageTask(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    /**
     * 同步发送新订单消息
     * 发送前先声明队列并绑定路由键。
     * 队列声明是幂等性的，如果队列已存在也不会创建新的队列。
     *
     * @param list 新订单消息列表
     */
    public void sendNewOrderMessage(List<NewOrderMessage> list) {
        for (NewOrderMessage message : list) {
            String userId = message.getUserId();
            String queueName = "queue_" + userId;

            // 1. 先声明队列并绑定，确保消息有队列可以投递。
            rabbitTemplate.execute(channel -> {
                // 队列闲置7天自动被删除，避免长期不接单的司机队列无限堆积。
                Map<String, Object> args = new HashMap<>();
                args.put("x-expires", QUEUE_EXPIRES);
                // 声明队列
                // 1. 通过args设置队列的属性
                // 2. 以下方法的参数：队列名、持久化、不独占、不自动删除、额外的参数
                channel.queueDeclare(queueName, true, false, false, args);
                // 将队列绑定到交换机，使用userId作为路由键
                channel.queueBind(queueName, EXCHANGE_NAME, userId);
                return null;
            });

            // 2.发送消息
            // 参数1：交换机名称
            // 参数2：路由键
            // 参数3：消息对象
            // 参数4：消息处理器，设置消息的属性。
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, userId, message, msg -> {
                // 发送消息并通过消息后处理器设置消息的有效时间为1分钟。
                msg.getMessageProperties().setExpiration(String.valueOf(TTL));
                return msg;
            });
            log.info("发送成功：{}", message.getOrderId());
        }
    }

    // 异步发送消息
    @Async
    public void sendNewOrderMessageAsync(List<NewOrderMessage> list) {
        sendNewOrderMessage(list);
    }

    /**
     * 同步接收新订单消息（轮询方式）
     * 司机端每隔3秒调用一次这个方法，把队列当中积压的消息一次性全部取走。
     *
     * @param userId 司机ID
     * @return 新订单消息列表（按照时间倒序，新消息在前）
     */
    public List<NewOrderMessage> receiveNewOrderMessage(long userId) {
        // 队列的名字
        String queueName = "queue_" + userId;
        // 准备一个空的List集合
        List<NewOrderMessage> list = new ArrayList<>();

        // execute: 在RabbitMQ Channel 当中执行相关操作，自动管理连接和信道。
        return rabbitTemplate.execute(channel -> {
            // 可以编写MQ的原生代码。
            // 设置队列的属性（最多闲置7天）
            Map<String, Object> args = new HashMap<>();
            args.put("x-expires", QUEUE_EXPIRES);
            // 声明队列
            channel.queueDeclare(queueName, true, false, false, args);
            // 使用路由键绑定队列和交换机
            channel.queueBind(queueName, EXCHANGE_NAME, String.valueOf(userId));

            // receiveAndConvert：非阻塞拉取，没有消息立即返回 null
            // 内部自动使用 Jackson2JsonMessageConverter 反序列化，并自动 ack
            // 仍然是采用一条消息一条消息接收。
            NewOrderMessage msg;
            while ((msg = (NewOrderMessage) rabbitTemplate.receiveAndConvert(queueName)) != null) {
                list.add(msg);
            }

            // 翻转列表，让最新的消息排在最前面
            Collections.reverse(list);

            return list;
        });
    }

    /**
     * 同步清空队列。（司机开始接单、停止接单、退出系统时都需要。）
     */
    public void clearNewOrderQueue(long userId) {
        String queueName = "queue_" + userId;
        rabbitTemplate.execute(channel -> {
            Map<String, Object> args = new HashMap<>();
            args.put("x-expires", QUEUE_EXPIRES);
            channel.queueDeclare(queueName, true, false, false, args);
            channel.queueBind(queueName, EXCHANGE_NAME, String.valueOf(userId));
            while (channel.basicGet(queueName, true) != null) {
            }
            log.info("队列清空：{}", queueName);
            return null;
        });
    }

    /**
     * 异步清空队列
     */
    @Async
    public void clearNewOrderQueueAsync(long userId) {
        clearNewOrderQueue(userId);
    }

    /**
     * 同步删除队列
     */
    public void deleteNewOrderQueue(long userId) {
        amqpAdmin.deleteQueue("queue_" + userId);
    }

    /**
     * 异步删除队列
     */
    @Async
    public void deleteNewOrderQueueAsync(long userId) {
        amqpAdmin.deleteQueue("queue_" + userId);
    }
}



















