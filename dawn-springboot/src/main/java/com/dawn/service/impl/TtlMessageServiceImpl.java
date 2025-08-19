package com.dawn.service.impl;

import com.dawn.service.TtlMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.dawn.constant.RabbitMQConstant.TTL_EXCHANGE;

/**
 * TTL 消息发送服务
 */
@Slf4j
@Service
public class TtlMessageServiceImpl implements TtlMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送带队列级 TTL 的消息
     */
    public void sendQueueTtlMessage(String messageContent) {
        log.info("发送队列级 TTL 消息: {}", messageContent);
        
        rabbitTemplate.convertAndSend(
                TTL_EXCHANGE, 
                "ttl.routing.key", 
                messageContent
        );
    }

    /**
     * 发送带消息级 TTL 的消息
     */
    public void sendMessageTtlMessage(String messageContent, long ttlMillis) {
        log.info("发送消息级 TTL 消息: {}, TTL: {}ms", messageContent, ttlMillis);
        
        // 设置消息属性
        MessageProperties properties = new MessageProperties();
        properties.setExpiration(String.valueOf(ttlMillis));  // 设置消息级 TTL
        
        Message message = MessageBuilder
                .withBody(messageContent.getBytes())
                .andProperties(properties)
                .build();
        
        rabbitTemplate.send(TTL_EXCHANGE, "ttl.routing.key", message);
    }

    /**
     * 发送延迟消息（使用消息级 TTL 实现）,消息头包含了更多的业务信息
     */
    public void sendDelayMessage(String messageContent, long delayMillis) {
        log.info("发送延迟消息: {}, 延迟: {}ms", messageContent, delayMillis);
        
        // 创建消息属性
        MessageProperties properties = new MessageProperties();
        properties.setExpiration(String.valueOf(delayMillis));
        properties.setHeader("original-message", messageContent);
        properties.setHeader("send-time", System.currentTimeMillis());
        
        Message message = MessageBuilder
                .withBody(messageContent.getBytes())
                .andProperties(properties)
                .build();
        
        // 发送到 TTL 队列，过期后会转到死信队列
        rabbitTemplate.send(TTL_EXCHANGE, "ttl.routing.key", message);
    }

    /**
     * 批量发送测试消息
     */
    public void sendTestMessages() {
        // 1. 发送队列级 TTL 消息（30秒后过期）
        sendQueueTtlMessage("这是一个队列级 TTL 消息，30秒后过期");
        
        // 2. 发送消息级 TTL 消息（10秒后过期）
        sendMessageTtlMessage("这是一个消息级 TTL 消息，10秒后过期", 10000);
        
        // 3. 发送延迟消息（5秒后过期）
        sendDelayMessage("这是一个延迟消息，5秒后过期", 5000);
        
        log.info("测试消息发送完成，请观察控制台日志");
    }
}
