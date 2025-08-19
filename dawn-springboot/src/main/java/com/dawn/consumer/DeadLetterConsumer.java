package com.dawn.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.dawn.constant.RabbitMQConstant.DEAD_LETTER_QUEUE;

/**
 * 死信队列消费者
 */
@Slf4j
@Component
public class DeadLetterConsumer {

    /**
     * 监听死信队列. 默认情况下，Spring AMQP 使用自动确认（Auto Ack），但处理死信队列时建议手动确认。
     * @param message 消息体
     * @param channel 通道
     */
    @RabbitListener(queues = DEAD_LETTER_QUEUE, ackMode = "MANUAL")
    public void handleDeadLetter(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String body = new String(message.getBody());
            log.warn("收到死信消息: {}", body);
            
            // 获取消息的属性信息
            String exchange = (String) message.getMessageProperties().getHeaders().get("x-first-death-exchange");
            String queue = (String) message.getMessageProperties().getHeaders().get("x-first-death-queue");
            String reason = (String) message.getMessageProperties().getHeaders().get("x-first-death-reason");
            
            log.warn("死信消息详情 - 原交换机: {}, 原队列: {}, 死亡原因: {}, deliveryTag: {}", exchange, queue, reason, deliveryTag);
            
            // 根据死亡原因进行不同的处理
            switch (reason) {
                case "expired":
                    log.warn("消息过期处理: {}", body);
                    handleExpiredMessage(body);
                    break;
                case "rejected":
                    log.warn("消息被拒绝处理: {}", body);
                    handleRejectedMessage(body);
                    break;
                case "maxlen":
                    log.warn("队列长度超限处理: {}", body);
                    handleMaxLengthMessage(body);
                    break;
                default:
                    log.warn("未知死信原因: {}", reason);
                    handleUnknownReasonMessage(body);
                    break;
            }
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理死信消息失败: ", e);
            try {
                // 拒绝消息，不重新入队.避免将死信队列的消息再次转发到原队列，造成死循环风险。
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                log.error("拒绝死信消息失败: ", ioException);
            }
        }
    }

    /**
     * 处理过期消息
     */
    private void handleExpiredMessage(String message) {
        log.info("处理过期消息: {}", message);
        // 这里可以进行补偿操作，比如：
        // 1. 记录到数据库
        // 2. 发送邮件通知
        // 3. 重新发送到其他队列
        // 4. 写入日志文件等
    }

    /**
     * 处理被拒绝的消息
     */
    private void handleRejectedMessage(String message) {
        log.info("处理被拒绝的消息: {}", message);
        // 分析被拒绝的原因，进行相应处理
    }

    /**
     * 处理队列长度超限的消息
     */
    private void handleMaxLengthMessage(String message) {
        log.info("处理队列长度超限的消息: {}", message);
        // 可能需要扩容或者优化消费速度
    }

    /**
     * 处理未知原因的死信消息
     */
    private void handleUnknownReasonMessage(String message) {
        log.info("处理未知原因的死信消息: {}", message);
        // 记录详细信息用于排查问题
    }
}
