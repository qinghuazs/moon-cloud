package com.moon.cloud.captcha.generator;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.enums.CaptchaType;
import org.springframework.stereotype.Component;

/**
 * 混合验证码生成器（字母+数字）
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Component
public class MixedCaptchaGenerator extends AbstractCaptchaGenerator {

    @Override
    protected String generateCode(CaptchaProperties properties) {
        // 去除容易混淆的字符：0, O, o, 1, l, I
        String safeChars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
        return generateRandomString(safeChars, properties.getLength());
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.MIXED;
    }
}