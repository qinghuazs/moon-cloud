package com.moon.cloud.captcha.generator;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.enums.CaptchaType;
import org.springframework.stereotype.Component;

/**
 * 字母验证码生成器
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Component
public class AlphaCaptchaGenerator extends AbstractCaptchaGenerator {

    @Override
    protected String generateCode(CaptchaProperties properties) {
        // 随机决定使用大写或小写字母
        String letters = SECURE_RANDOM.nextBoolean() ? UPPER_LETTERS : LOWER_LETTERS + UPPER_LETTERS;
        return generateRandomString(letters, properties.getLength());
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.ALPHA;
    }
}