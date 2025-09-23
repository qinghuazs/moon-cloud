package com.moon.cloud.email.enums;

/**
 * 邮件发送状态枚举
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public enum EmailStatus {

    /**
     * 待发送
     */
    PENDING(0, "待发送"),

    /**
     * 发送中
     */
    SENDING(1, "发送中"),

    /**
     * 发送成功
     */
    SUCCESS(2, "发送成功"),

    /**
     * 发送失败
     */
    FAILED(3, "发送失败"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消"),

    /**
     * 重试中
     */
    RETRYING(5, "重试中");

    private final Integer code;
    private final String description;

    EmailStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EmailStatus fromCode(Integer code) {
        for (EmailStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown email status code: " + code);
    }
}