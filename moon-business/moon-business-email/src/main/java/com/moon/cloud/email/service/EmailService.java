package com.moon.cloud.email.service;

import com.moon.cloud.email.dto.EmailSendRequest;
import com.moon.cloud.email.dto.EmailSendResponse;
import com.moon.cloud.email.entity.EmailRecord;

import java.util.List;
import java.util.Map;

/**
 * 邮件服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface EmailService {

    /**
     * 发送邮件
     *
     * @param request 邮件发送请求
     * @return 发送响应
     */
    EmailSendResponse sendEmail(EmailSendRequest request);

    /**
     * 发送简单文本邮件
     *
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 发送响应
     */
    EmailSendResponse sendSimpleEmail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     *
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 发送响应
     */
    EmailSendResponse sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * 使用模板发送邮件
     *
     * @param to 收件人
     * @param templateCode 模板编码
     * @param variables 模板变量
     * @return 发送响应
     */
    EmailSendResponse sendTemplateEmail(String to, String templateCode, Map<String, Object> variables);

    /**
     * 批量发送邮件
     *
     * @param toEmails 收件人列表
     * @param subject 主题
     * @param content 内容
     * @return 发送响应列表
     */
    List<EmailSendResponse> sendBatchEmail(List<String> toEmails, String subject, String content);

    /**
     * 发送带附件的邮件
     *
     * @param request 邮件发送请求
     * @return 发送响应
     */
    EmailSendResponse sendEmailWithAttachments(EmailSendRequest request);

    /**
     * 定时发送邮件
     *
     * @param request 邮件发送请求
     * @return 发送响应
     */
    EmailSendResponse scheduleEmail(EmailSendRequest request);

    /**
     * 重发邮件
     *
     * @param emailRecordId 邮件记录ID
     * @return 发送响应
     */
    EmailSendResponse resendEmail(Long emailRecordId);

    /**
     * 取消发送邮件
     *
     * @param emailRecordId 邮件记录ID
     * @return 是否成功
     */
    boolean cancelEmail(Long emailRecordId);

    /**
     * 获取邮件发送记录
     *
     * @param emailRecordId 邮件记录ID
     * @return 邮件记录
     */
    EmailRecord getEmailRecord(Long emailRecordId);

    /**
     * 获取邮件发送统计
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Map<String, Object> getEmailStatistics(String businessType, String startDate, String endDate);

    /**
     * 验证邮件地址
     *
     * @param email 邮件地址
     * @return 是否有效
     */
    boolean validateEmail(String email);

    /**
     * 测试邮件配置
     *
     * @return 测试结果
     */
    boolean testEmailConfig();
}