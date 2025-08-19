package com.dawn.constant;

public interface RabbitMQConstant {

    String MAXWELL_QUEUE = "maxwell_queue";

    String MAXWELL_EXCHANGE = "maxwell_exchange";

    String EMAIL_QUEUE = "email_queue";

    String EMAIL_EXCHANGE = "email_exchange";

    String SUBSCRIBE_QUEUE = "subscribe_queue";

    String SUBSCRIBE_EXCHANGE = "subscribe_exchange";

    // TTL 和死信队列相关常量
    String TTL_QUEUE = "ttl_queue";

    String TTL_EXCHANGE = "ttl_exchange";

    String DEAD_LETTER_QUEUE = "dead_letter_queue";

    String DEAD_LETTER_EXCHANGE = "dead_letter_exchange";

    String DEAD_LETTER_ROUTING_KEY = "dead_letter_routing_key";
}
