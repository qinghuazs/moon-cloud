package com.moon.cloud.captcha.core;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.enums.CaptchaType;

/**
 * 验证码生成器接口
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public interface CaptchaGenerator {

    /**
     * 生成验证码
     *
     * @param properties 配置属性
     * @return 验证码对象
     */
    Captcha generate(CaptchaProperties properties);

    /**
     * 获取验证码类型
     *
     * @return 验证码类型
     */
    CaptchaType getType();

    /**
     * 判断是否支持该类型
     *
     * @param type 验证码类型
     * @return 是否支持
     */
    default boolean supports(CaptchaType type) {
        return getType() == type;
    }
}