package com.example.zxds.odr.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    // 让消费者端也使用JSON转换器
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

    // ============== 1. 延迟队列配置 ==============
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange("order_delay_exchange", true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置队列中消息的存活时间为 60000 毫秒（即 1 分钟），超时后消息将变成死信
        args.put("x-message-ttl", 60000);
        // 指定死信交换机：当消息超时后，将被转发到这个交换机
        args.put("x-dead-letter-exchange", "order_profit_sharing_exchange");
        // 指定死信路由键：配合死信交换机，决定超时消息最终进入哪个死信队列
        args.put("x-dead-letter-routing-key", "order_profit_sharing_routing_key");
        // 声明并返回一个持久化的延迟队列，并将上面的死信参数绑定到该队列上
        return new Queue("order_delay_queue", true, false, false, args);
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderDelayExchange()).with("order_delay_routing_key");
    }

    // ============== 2. 死信队列配置 ==============
    @Bean
    public DirectExchange orderProfitSharingExchange() {
        return new DirectExchange("order_profit_sharing_exchange", true, false);
    }

    @Bean
    public Queue orderProfitsharingQueue() {
        return new Queue("order_profit_sharing_queue", true);
    }

    @Bean
    public Binding orderProfitsharingBinding() {
        return BindingBuilder.bind(orderProfitsharingQueue()).to(orderProfitSharingExchange()).with("order_profit_sharing_routing_key");
    }
}