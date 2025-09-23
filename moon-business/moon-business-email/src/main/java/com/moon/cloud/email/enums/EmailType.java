package com.moon.cloud.email.enums;

/**
 * 邮件类型枚举
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public enum EmailType {

    /**
     * 纯文本邮件
     */
    TEXT(1, "纯文本"),

    /**
     * HTML邮件
     */
    HTML(2, "HTML"),

    /**
     * 模板邮件
     */
    TEMPLATE(3, "模板邮件"),

    /**
     * 带附件邮件
     */
    ATTACHMENT(4, "带附件");

    private final Integer code;
    private final String description;

    EmailType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EmailType fromCode(Integer code) {
        for (EmailType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown email type code: " + code);
    }
}