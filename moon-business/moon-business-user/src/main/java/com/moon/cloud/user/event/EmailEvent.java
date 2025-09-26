package com.moon.cloud.user.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 邮件事件
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEvent implements Serializable {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 邮件类型
     */
    private EmailType emailType;

    /**
     * 收件人邮箱
     */
    private String to;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 邮件类型枚举
     */
    public enum EmailType {
        VERIFICATION_CODE("验证码"),
        PASSWORD_RESET_SUCCESS("密码重置成功"),
        WELCOME("欢迎邮件"),
        ACCOUNT_LOCKED("账号锁定通知");

        private final String description;

        EmailType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}