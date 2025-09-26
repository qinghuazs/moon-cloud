package com.moon.cloud.captcha.service;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.core.CaptchaGenerator;
import com.moon.cloud.captcha.core.CaptchaStorage;
import com.moon.cloud.captcha.core.CaptchaValidator;
import com.moon.cloud.captcha.enums.CaptchaType;
import com.moon.cloud.captcha.exception.CaptchaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 验证码服务实现
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final CaptchaProperties properties;
    private final CaptchaStorage storage;
    private final CaptchaValidator validator;
    private final Map<CaptchaType, CaptchaGenerator> generators;

    public CaptchaServiceImpl(CaptchaProperties properties,
                             CaptchaStorage storage,
                             CaptchaValidator validator,
                             List<CaptchaGenerator> generatorList) {
        this.properties = properties;
        this.storage = storage;
        this.validator = validator;
        this.generators = generatorList.stream()
                .collect(Collectors.toMap(CaptchaGenerator::getType, g -> g));
    }

    @Override
    public Captcha generate() {
        return generate(properties.getType());
    }

    @Override
    public Captcha generate(CaptchaType type) {
        return generate(type, properties.getLength());
    }

    @Override
    public Captcha generate(int length) {
        return generate(properties.getType(), length);
    }

    @Override
    public Captcha generate(CaptchaType type, int length) {
        CaptchaGenerator generator = generators.get(type);
        if (generator == null) {
            throw new CaptchaException("不支持的验证码类型: " + type);
        }

        // 创建临时配置
        CaptchaProperties tempProperties = new CaptchaProperties();
        tempProperties.setType(type);
        tempProperties.setLength(length);
        tempProperties.setExpireTime(properties.getExpireTime());
        tempProperties.setSecurity(properties.getSecurity());
        tempProperties.setImage(properties.getImage());
        tempProperties.setMath(properties.getMath());

        Captcha captcha = generator.generate(tempProperties);
        log.debug("生成验证码: id={}, type={}, code={}", captcha.getId(), type, captcha.getCode());
        return captcha;
    }

    @Override
    public void save(String key, Captcha captcha) {
        save(key, captcha, properties.getExpireTime(), TimeUnit.SECONDS);
    }

    @Override
    public void save(String key, Captcha captcha, long timeout, TimeUnit unit) {
        storage.save(key, captcha, timeout, unit);
        log.debug("保存验证码: key={}, timeout={} {}", key, timeout, unit);
    }

    @Override
    public boolean validate(String key, String code) {
        return validator.validate(key, code);
    }

    @Override
    public boolean validate(String key, String code, boolean caseSensitive) {
        return validator.validate(key, code, caseSensitive);
    }

    @Override
    public Captcha get(String key) {
        return storage.get(key);
    }

    @Override
    public void remove(String key) {
        storage.remove(key);
        log.debug("删除验证码: key={}", key);
    }

    @Override
    public boolean isLocked(String key) {
        return validator.isLocked(key);
    }

    @Override
    public int getFailureCount(String key) {
        return validator.getFailureCount(key);
    }
}