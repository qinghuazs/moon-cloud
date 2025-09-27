package com.moon.cloud.appstore.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis配置类
 * 使用环境变量配置Redis连接
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    private final Dotenv dotenv;

    public RedisConfig() {
        this.dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Redis连接配置
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(getEnvValue("REDIS_HOST", "localhost"));
//        redisConfig.setPort(Integer.parseInt(getEnvValue("REDIS_PORT", "6380")));
        redisConfig.setPort(6380);
        redisConfig.setDatabase(Integer.parseInt(getEnvValue("REDIS_DATABASE", "0")));

        String password = getEnvValue("REDIS_PASSWORD", "");
        if (!password.isEmpty()) {
            redisConfig.setPassword(password);
        }

        // 连接池配置
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(getEnvValue("REDIS_MAX_ACTIVE", "8")));
        poolConfig.setMaxIdle(Integer.parseInt(getEnvValue("REDIS_MAX_IDLE", "8")));
        poolConfig.setMinIdle(Integer.parseInt(getEnvValue("REDIS_MIN_IDLE", "0")));
        poolConfig.setMaxWait(Duration.ofMillis(Long.parseLong(getEnvValue("REDIS_MAX_WAIT", "-1"))));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        // 客户端配置
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(Long.parseLong(getEnvValue("REDIS_TIMEOUT", "2000"))))
                .shutdownTimeout(Duration.ofMillis(100))
                .poolConfig(poolConfig)
                .build();

        log.info("Redis连接配置完成: {}:{}/{}",
                redisConfig.getHostName(), redisConfig.getPort(), redisConfig.getDatabase());
        log.info("Redis连接池配置: 最大连接数={}, 最大空闲连接={}, 最小空闲连接={}",
                poolConfig.getMaxTotal(), poolConfig.getMaxIdle(), poolConfig.getMinIdle());

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        // 设置序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJacksonSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))  // 默认缓存1小时
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues();  // 不缓存null值

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();

        log.info("Redis缓存管理器配置完成");
        return cacheManager;
    }

    private Jackson2JsonRedisSerializer<Object> createJacksonSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();

        // 支持Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());

        // 设置可见性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 设置类型信息
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }

    private String getEnvValue(String key, String defaultValue) {
        // 优先从系统环境变量获取
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 从.env文件获取
        value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }

        // 返回默认值
        return defaultValue;
    }
}