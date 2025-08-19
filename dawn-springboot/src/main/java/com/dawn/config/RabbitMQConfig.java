package com.dawn.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.dawn.constant.RabbitMQConstant.*;

@Configuration
public class RabbitMQConfig {

    /**
     * 业务队列：持久化、非独占、不自动删除
     * 适用场景：核心业务消息，需要高可靠性
     */
    @Bean
    public Queue articleQueue() {
        return new Queue(MAXWELL_QUEUE, true, false, false);
        // durable=true: 服务器重启后队列仍存在
        // exclusive=false: 多个应用可以共享使用
        // autoDelete=false: 即使没有消费者也不删除
    }

    @Bean
    public FanoutExchange maxWellExchange() {
        return new FanoutExchange(MAXWELL_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingArticleDirect() {
        return BindingBuilder.bind(articleQueue()).to(maxWellExchange());
    }

    /**
     * 邮件队列：持久化、非独占、不自动删除
     * 适用场景：重要通知消息，需要保证送达
     */
    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true, false, false);
    }

    @Bean
    public FanoutExchange emailExchange() {
        return new FanoutExchange(EMAIL_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingEmailDirect() {
        return BindingBuilder.bind(emailQueue()).to(emailExchange());
    }

    /**
     * 订阅队列：持久化、非独占、不自动删除
     * 适用场景：用户订阅通知，需要持续处理
     */
    @Bean
    public Queue subscribeQueue() {
        return new Queue(SUBSCRIBE_QUEUE, true, false, false);
    }

    @Bean
    public FanoutExchange subscribeExchange() {
        return new FanoutExchange(SUBSCRIBE_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingSubscribeDirect() {
        return BindingBuilder.bind(subscribeQueue()).to(subscribeExchange());
    }

    // ===== TTL 和死信队列配置 =====

    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    /**
     * 死信队列绑定死信交换机
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * TTL 队列（带死信配置）
     * 方法一：队列级别 TTL
     */
    @Bean
    public Queue ttlQueue() {
        return QueueBuilder.durable(TTL_QUEUE)
                .ttl(5000)  // 队列中消息的 TTL：5秒
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)  // 死信交换机
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)  // 死信路由键
                .build();
    }

    /**
     * TTL 交换机
     */
    @Bean
    public DirectExchange ttlExchange() {
        return new DirectExchange(TTL_EXCHANGE, true, false);
    }

    /**
     * TTL 队列绑定 TTL 交换机
     */
    @Bean
    public Binding ttlBinding() {
        return BindingBuilder.bind(ttlQueue())
                .to(ttlExchange())
                .with("ttl.routing.key");
    }

    /**
     * 方法二：使用 Map 配置 TTL 队列（更灵活的配置方式）
     */
    @Bean
    public Queue customTtlQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 60000);  // 消息 TTL：60秒
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);  // 死信交换机
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);  // 死信路由键
        args.put("x-max-length", 1000);  // 队列最大长度
        args.put("x-overflow", "reject-publish");  // 队列满时的处理策略
        
        return new Queue("custom_ttl_queue", true, false, false, args);
    }

    // ===== 不同参数组合示例 =====

    /**
     * 临时队列：非持久化、非独占、自动删除
     * 适用场景：临时任务、测试环境
     */
    @Bean
    public Queue temporaryQueue() {
        return new Queue("temporary_queue", false, false, true);
        // durable=false: 服务器重启后队列消失
        // exclusive=false: 可以被多个连接使用
        // autoDelete=true: 没有消费者时自动删除
    }

    /**
     * RPC回调队列：持久化、独占、自动删除
     * 适用场景：RPC调用的回调响应
     */
    @Bean
    public Queue rpcCallbackQueue() {
        return new Queue("rpc_callback_queue", true, true, true);
        // durable=true: 持久化保证消息不丢失
        // exclusive=true: 只有当前连接可以使用
        // autoDelete=true: 连接断开时自动删除
    }

    /**
     * 缓存队列：非持久化、非独占、自动删除
     * 适用场景：缓存数据、临时数据传输
     */
    @Bean
    public Queue cacheQueue() {
        return new Queue("cache_queue", false, false, true);
        // durable=false: 不需要持久化，重启后数据可以重新生成
        // exclusive=false: 多个应用可以使用
        // autoDelete=true: 没有消费者时释放资源
    }

}
