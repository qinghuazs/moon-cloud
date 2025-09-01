package com.moon.cloud.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        // 设置key的序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置value的序列化方式
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        // 设置hash key的序列化方式
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // 设置hash value的序列化方式
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}