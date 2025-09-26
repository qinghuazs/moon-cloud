package com.moon.cloud.captcha.validator;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.core.CaptchaStorage;
import com.moon.cloud.captcha.core.CaptchaValidator;
import com.moon.cloud.captcha.storage.RedisCaptchaStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认验证码验证器
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Slf4j
@Component
public class DefaultCaptchaValidator implements CaptchaValidator {

    private final CaptchaStorage storage;
    private final CaptchaProperties properties;

    public DefaultCaptchaValidator(CaptchaStorage storage, CaptchaProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    @Override
    public boolean validate(String key, String code) {
        return validate(key, code, properties.getSecurity().isCaseSensitive());
    }

    @Override
    public boolean validate(String key, String code, boolean caseSensitive) {
        if (key == null || code == null) {
            return false;
        }

        // 检查是否被锁定
        if (isLocked(key)) {
            log.warn("验证码验证失败，键已被锁定: key={}", key);
            return false;
        }

        // 获取验证码
        Captcha captcha = storage.get(key);
        if (captcha == null) {
            log.debug("验证码不存在: key={}", key);
            recordFailure(key);
            return false;
        }

        // 检查是否过期
        if (captcha.isExpired()) {
            log.debug("验证码已过期: key={}", key);
            storage.remove(key);
            recordFailure(key);
            return false;
        }

        // 检查是否已使用
        if (captcha.getUsed() != null && captcha.getUsed() && !properties.getSecurity().isAllowReuse()) {
            log.debug("验证码已使用: key={}", key);
            recordFailure(key);
            return false;
        }

        // 验证验证码
        String answer = captcha.getAnswer() != null ? captcha.getAnswer() : captcha.getCode();
        boolean valid;
        if (caseSensitive) {
            valid = code.equals(answer);
        } else {
            valid = code.equalsIgnoreCase(answer);
        }

        if (valid) {
            // 验证成功，标记为已使用
            captcha.setUsed(true);
            storage.update(key, captcha);

            // 清除失败记录
            clearFailureRecord(key);

            // 如果不允许重复使用，删除验证码
            if (!properties.getSecurity().isAllowReuse()) {
                storage.remove(key);
            }

            log.debug("验证码验证成功: key={}", key);
            return true;
        } else {
            log.debug("验证码验证失败: key={}, expected={}, actual={}", key, answer, code);
            recordFailure(key);

            // 记录失败次数
            int failCount = captcha.getFailCount() != null ? captcha.getFailCount() + 1 : 1;
            captcha.setFailCount(failCount);
            storage.update(key, captcha);

            // 失败次数过多，删除验证码
            if (failCount >= properties.getSecurity().getMaxRetry()) {
                storage.remove(key);
                log.warn("验证码失败次数过多，已删除: key={}, failCount={}", key, failCount);
            }

            return false;
        }
    }

    @Override
    public void recordFailure(String key) {
        int count = storage.recordFailure(key);
        log.debug("记录验证失败: key={}, count={}", key, count);
    }

    @Override
    public boolean isLocked(String key) {
        int count = getFailureCount(key);
        return count >= properties.getSecurity().getMaxRetry();
    }

    @Override
    public int getFailureCount(String key) {
        if (storage instanceof RedisCaptchaStorage) {
            return ((RedisCaptchaStorage) storage).getFailureCount(key);
        }
        // 内存存储暂不实现
        return 0;
    }

    @Override
    public void clearFailureRecord(String key) {
        if (storage instanceof RedisCaptchaStorage) {
            ((RedisCaptchaStorage) storage).clearFailureRecord(key);
        }
        log.debug("清除失败记录: key={}", key);
    }
}