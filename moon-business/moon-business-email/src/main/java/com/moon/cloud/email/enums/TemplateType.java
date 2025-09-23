package com.moon.cloud.email.enums;

/**
 * 邮件模板类型枚举
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public enum TemplateType {

    /**
     * 用户注册
     */
    USER_REGISTER("USER_REGISTER", "用户注册"),

    /**
     * 密码重置
     */
    PASSWORD_RESET("PASSWORD_RESET", "密码重置"),

    /**
     * 邮箱验证
     */
    EMAIL_VERIFY("EMAIL_VERIFY", "邮箱验证"),

    /**
     * 登录通知
     */
    LOGIN_NOTIFY("LOGIN_NOTIFY", "登录通知"),

    /**
     * 系统通知
     */
    SYSTEM_NOTIFY("SYSTEM_NOTIFY", "系统通知"),

    /**
     * 营销推广
     */
    MARKETING("MARKETING", "营销推广"),

    /**
     * 账单通知
     */
    BILL_NOTIFY("BILL_NOTIFY", "账单通知"),

    /**
     * 安全提醒
     */
    SECURITY_ALERT("SECURITY_ALERT", "安全提醒"),

    /**
     * 活动邀请
     */
    EVENT_INVITE("EVENT_INVITE", "活动邀请"),

    /**
     * 自定义模板
     */
    CUSTOM("CUSTOM", "自定义模板");

    private final String code;
    private final String description;

    TemplateType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TemplateType fromCode(String code) {
        for (TemplateType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown template type code: " + code);
    }
}