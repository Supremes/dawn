package com.dawn.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import static com.dawn.constant.RabbitMQConstant.TTL_EXCHANGE;

public interface TtlMessageService {
    void sendQueueTtlMessage(String messageContent);

    /**
     * 发送带消息级 TTL 的消息
     */
    void sendMessageTtlMessage(String messageContent, long ttlMillis);
    /**
     * 发送延迟消息（使用消息级 TTL 实现）
     */
    void sendDelayMessage(String messageContent, long delayMillis);

    /**
     * 批量发送测试消息
     */
    void sendTestMessages();
}
