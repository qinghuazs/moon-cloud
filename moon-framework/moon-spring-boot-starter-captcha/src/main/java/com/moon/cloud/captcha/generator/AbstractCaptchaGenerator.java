package com.moon.cloud.captcha.generator;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.core.CaptchaGenerator;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 抽象验证码生成器
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public abstract class AbstractCaptchaGenerator implements CaptchaGenerator {

    protected static final SecureRandom SECURE_RANDOM = new SecureRandom();
    protected static final String DIGITS = "0123456789";
    protected static final String LOWER_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    protected static final String UPPER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    protected static final String MIXED_CHARS = DIGITS + LOWER_LETTERS + UPPER_LETTERS;

    @Override
    public Captcha generate(CaptchaProperties properties) {
        String code = generateCode(properties);

        return Captcha.builder()
                .id(UUID.randomUUID().toString())
                .code(code)
                .answer(code)  // 默认答案就是验证码本身
                .type(getType())
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusSeconds(properties.getExpireTime()))
                .failCount(0)
                .used(false)
                .build();
    }

    /**
     * 生成验证码字符串
     *
     * @param properties 配置属性
     * @return 验证码字符串
     */
    protected abstract String generateCode(CaptchaProperties properties);

    /**
     * 生成随机字符串
     *
     * @param characters 字符集
     * @param length     长度
     * @return 随机字符串
     */
    protected String generateRandomString(String characters, int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(SECURE_RANDOM.nextInt(characters.length())));
        }
        return code.toString();
    }
}