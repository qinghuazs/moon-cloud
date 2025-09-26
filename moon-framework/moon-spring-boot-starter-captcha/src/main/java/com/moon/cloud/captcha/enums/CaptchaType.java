package com.moon.cloud.captcha.enums;

import lombok.Getter;

/**
 * 验证码类型枚举
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Getter
public enum CaptchaType {

    /**
     * 数字验证码
     */
    DIGIT("DIGIT", "纯数字验证码"),

    /**
     * 字母验证码
     */
    ALPHA("ALPHA", "纯字母验证码"),

    /**
     * 混合验证码（字母+数字）
     */
    MIXED("MIXED", "字母数字混合验证码"),

    /**
     * 算术验证码
     */
    MATH("MATH", "算术运算验证码"),

    /**
     * 图形验证码
     */
    IMAGE("IMAGE", "图形验证码"),

    /**
     * 滑块验证码
     */
    SLIDER("SLIDER", "滑块验证码"),

    /**
     * 短信验证码
     */
    SMS("SMS", "短信验证码"),

    /**
     * 邮箱验证码
     */
    EMAIL("EMAIL", "邮箱验证码");

    private final String code;
    private final String description;

    CaptchaType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}