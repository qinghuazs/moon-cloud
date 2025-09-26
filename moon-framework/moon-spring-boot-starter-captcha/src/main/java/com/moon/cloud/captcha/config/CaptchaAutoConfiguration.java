package com.moon.cloud.captcha.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moon.cloud.captcha.core.CaptchaGenerator;
import com.moon.cloud.captcha.core.CaptchaStorage;
import com.moon.cloud.captcha.core.CaptchaValidator;
import com.moon.cloud.captcha.enums.StorageType;
import com.moon.cloud.captcha.generator.*;
import com.moon.cloud.captcha.service.CaptchaService;
import com.moon.cloud.captcha.service.CaptchaServiceImpl;
import com.moon.cloud.captcha.storage.MemoryCaptchaStorage;
import com.moon.cloud.captcha.storage.RedisCaptchaStorage;
import com.moon.cloud.captcha.validator.DefaultCaptchaValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 验证码自动配置
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
@ConditionalOnProperty(prefix = "moon.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
    DigitCaptchaGenerator.class,
    AlphaCaptchaGenerator.class,
    MixedCaptchaGenerator.class,
    MathCaptchaGenerator.class
})
public class CaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CaptchaStorage captchaStorage(CaptchaProperties properties,
                                         ObjectMapper objectMapper,
                                         RedisTemplate<String, Object> redisTemplate) {
        StorageType storageType = properties.getStorage();
        log.info("初始化验证码存储: type={}", storageType);

        switch (storageType) {
            case REDIS:
                if (redisTemplate != null) {
                    return new RedisCaptchaStorage(redisTemplate, properties, objectMapper);
                } else {
                    log.warn("Redis未配置，使用内存存储");
                    return new MemoryCaptchaStorage();
                }
            case MEMORY:
            default:
                return new MemoryCaptchaStorage();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaValidator captchaValidator(CaptchaStorage storage, CaptchaProperties properties) {
        return new DefaultCaptchaValidator(storage, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaService captchaService(CaptchaProperties properties,
                                        CaptchaStorage storage,
                                        CaptchaValidator validator,
                                        List<CaptchaGenerator> generators) {
        log.info("初始化验证码服务: type={}, length={}, expire={}s",
                properties.getType(), properties.getLength(), properties.getExpireTime());
        return new CaptchaServiceImpl(properties, storage, validator, generators);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}