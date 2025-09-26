package com.moon.cloud.user.service;

/**
 * 邮件服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);

    /**
     * 发送密码重置成功通知邮件
     *
     * @param to 收件人邮箱
     */
    void sendPasswordResetNotification(String to);
}