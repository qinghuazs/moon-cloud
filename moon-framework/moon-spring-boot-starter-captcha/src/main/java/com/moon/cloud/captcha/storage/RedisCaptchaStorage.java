package com.moon.cloud.captcha.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.core.CaptchaStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis验证码存储实现
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass(RedisTemplate.class)
public class RedisCaptchaStorage implements CaptchaStorage {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CaptchaProperties properties;
    private final ObjectMapper objectMapper;

    public RedisCaptchaStorage(RedisTemplate<String, Object> redisTemplate,
                               CaptchaProperties properties,
                               ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(String key, Captcha captcha, long timeout, TimeUnit unit) {
        String redisKey = getRedisKey(key);
        try {
            redisTemplate.opsForValue().set(redisKey, captcha, timeout, unit);
        } catch (Exception e) {
            log.error("保存验证码到Redis失败: key={}", key, e);
        }
    }

    @Override
    public Captcha get(String key) {
        String redisKey = getRedisKey(key);
        try {
            Object obj = redisTemplate.opsForValue().get(redisKey);
            if (obj instanceof Captcha) {
                return (Captcha) obj;
            }
            return null;
        } catch (Exception e) {
            log.error("从Redis获取验证码失败: key={}", key, e);
            return null;
        }
    }

    @Override
    public boolean remove(String key) {
        String redisKey = getRedisKey(key);
        String failureKey = getFailureKey(key);
        try {
            redisTemplate.delete(failureKey);
            return Boolean.TRUE.equals(redisTemplate.delete(redisKey));
        } catch (Exception e) {
            log.error("从Redis删除验证码失败: key={}", key, e);
            return false;
        }
    }

    @Override
    public boolean exists(String key) {
        String redisKey = getRedisKey(key);
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("检查验证码是否存在失败: key={}", key, e);
            return false;
        }
    }

    @Override
    public void update(String key, Captcha captcha) {
        String redisKey = getRedisKey(key);
        try {
            Long expire = redisTemplate.getExpire(redisKey);
            if (expire != null && expire > 0) {
                redisTemplate.opsForValue().set(redisKey, captcha, expire, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("更新验证码失败: key={}", key, e);
        }
    }

    @Override
    public int recordFailure(String key) {
        String failureKey = getFailureKey(key);
        try {
            Long count = redisTemplate.opsForValue().increment(failureKey);
            if (count != null) {
                redisTemplate.expire(failureKey, properties.getSecurity().getLockTime(), TimeUnit.SECONDS);
                return count.intValue();
            }
            return 0;
        } catch (Exception e) {
            log.error("记录验证失败次数失败: key={}", key, e);
            return 0;
        }
    }

    @Override
    public void clear() {
        // Redis存储不支持清空所有验证码
        log.warn("Redis存储不支持清空所有验证码");
    }

    /**
     * 获取Redis键
     */
    private String getRedisKey(String key) {
        return properties.getRedis().getKeyPrefix() + key;
    }

    /**
     * 获取失败次数键
     */
    private String getFailureKey(String key) {
        return properties.getRedis().getFailurePrefix() + key;
    }

    /**
     * 获取失败次数
     */
    public int getFailureCount(String key) {
        String failureKey = getFailureKey(key);
        try {
            Object count = redisTemplate.opsForValue().get(failureKey);
            if (count != null) {
                return Integer.parseInt(count.toString());
            }
            return 0;
        } catch (Exception e) {
            log.error("获取失败次数失败: key={}", key, e);
            return 0;
        }
    }

    /**
     * 清除失败记录
     */
    public void clearFailureRecord(String key) {
        String failureKey = getFailureKey(key);
        try {
            redisTemplate.delete(failureKey);
        } catch (Exception e) {
            log.error("清除失败记录失败: key={}", key, e);
        }
    }
}