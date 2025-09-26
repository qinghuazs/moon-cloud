package com.moon.cloud.captcha.generator;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.enums.CaptchaType;
import org.springframework.stereotype.Component;

/**
 * 数字验证码生成器
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Component
public class DigitCaptchaGenerator extends AbstractCaptchaGenerator {

    @Override
    protected String generateCode(CaptchaProperties properties) {
        return generateRandomString(DIGITS, properties.getLength());
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.DIGIT;
    }
}