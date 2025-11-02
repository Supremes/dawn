package com.dawn.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private RedisTemplate<String, Object> buildTemplate(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.dawn")
                .build();
        ObjectMapper objectMapper = JsonMapper.builder().activateDefaultTyping(
                validator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY
        ).build();
        // 设置可见性，任何修饰符的属性都可以被序列化和反序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        /*
         * 日期时间处理
         * mapper.registerModule(new JavaTimeModule())和 mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)这两行代码通常配合使用，专门用于正确处理 Java 8 引入的日期时间 API（如 LocalDateTime、ZonedDateTime等）
         * JavaTimeModule：这是 Jackson 的一个扩展模块，为新的时间类提供序列化和反序列化器。如果不注册此模块，Jackson 可能无法处理这些类型
         * 禁用时间戳：默认情况下，Jackson 会将 Date或 LocalDateTime等对象序列化为数字形式的时间戳（如 1677842850000）。禁用此特性后，日期会被格式化为更易读的 ISO-8601 标准字符串（例如 "2025-11-02T20:17:37"）
         */
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return buildTemplate(factory, objectMapper);
    }
}