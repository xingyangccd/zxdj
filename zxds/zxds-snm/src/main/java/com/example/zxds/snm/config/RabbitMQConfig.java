package com.example.zxds.snm.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * RabbitTemplate：用于发送和接收消息
     * 设置 JSON 消息转换器，这样可以直接发送 Java 对象，不用手动序列化
     */
    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean("stringRabbitTemplate")
    public RabbitTemplate stringRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 字符串专用的
        rabbitTemplate.setMessageConverter(new SimpleMessageConverter());
        return rabbitTemplate;
    }


    /**
     * Direct 交换机：new_order_private
     * 参数：交换机名，持久化(true)，不自动删除(false)
     * 启动时自动创建；队列由业务代码按 driverId 动态声明
     */
    @Bean
    public DirectExchange newOrderExchange() {
        return new DirectExchange("new_order_private", true, false);
    }

    // 另外，再声明后续需要使用的交换机
    // ==================== 私有消息交换机 (Direct) ====================
    @Bean
    public DirectExchange driverPrivateExchange() {
        return new DirectExchange("driver_private", true, false);
    }

    @Bean
    public DirectExchange customerPrivateExchange() {
        return new DirectExchange("customer_private", true, false);
    }

    @Bean
    public DirectExchange customerBillPrivateExchange() {
        return new DirectExchange("customer_bill_private", true, false);
    }

    // ==================== 广播消息交换机 (Fanout) ====================
    @Bean
    public FanoutExchange driverBroadcastExchange() {
        return new FanoutExchange("driver_broadcast", true, false);
    }

    @Bean
    public FanoutExchange customerBroadcastExchange() {
        return new FanoutExchange("customer_broadcast", true, false);
    }
}