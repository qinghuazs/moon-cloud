package com.moon.cloud.email.service.impl;

import com.moon.cloud.email.config.EmailConfig;
import com.moon.cloud.email.dto.EmailSendRequest;
import com.moon.cloud.email.dto.EmailSendResponse;
import com.moon.cloud.email.entity.EmailRecord;
import com.moon.cloud.email.entity.EmailTemplate;
import com.moon.cloud.email.enums.EmailStatus;
import com.moon.cloud.email.mapper.EmailRecordMapper;
import com.moon.cloud.email.service.EmailQueueService;
import com.moon.cloud.email.service.EmailService;
import com.moon.cloud.email.service.EmailTemplateService;
import com.moon.cloud.email.service.TemplateEngineService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 邮件服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private EmailRecordMapper emailRecordMapper;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private TemplateEngineService templateEngineService;

    @Autowired
    private EmailQueueService emailQueueService;

    @Override
    public EmailSendResponse sendEmail(EmailSendRequest request) {
        try {
            // 验证请求参数
            validateEmailRequest(request);

            // 创建邮件记录
            EmailRecord emailRecord = createEmailRecord(request);
            emailRecordMapper.insert(emailRecord);

            // 判断是否定时发送
            if (request.getSendTime() != null && request.getSendTime().isAfter(LocalDateTime.now())) {
                return scheduleEmailInternal(emailRecord);
            }

            // 判断是否使用队列
            if (emailConfig.getEnableQueue()) {
                return enqueueEmail(emailRecord);
            } else {
                return sendEmailDirectly(emailRecord);
            }

        } catch (Exception e) {
            return EmailSendResponse.builder()
                    .success(false)
                    .message("邮件发送失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public EmailSendResponse sendSimpleEmail(String to, String subject, String content) {
        EmailSendRequest request = EmailSendRequest.builder()
                .to(Collections.singletonList(to))
                .subject(subject)
                .content(content)
                .build();
        return sendEmail(request);
    }

    @Override
    public EmailSendResponse sendHtmlEmail(String to, String subject, String htmlContent) {
        EmailSendRequest request = EmailSendRequest.builder()
                .to(Collections.singletonList(to))
                .subject(subject)
                .htmlContent(htmlContent)
                .build();
        return sendEmail(request);
    }

    @Override
    public EmailSendResponse sendTemplateEmail(String to, String templateCode, Map<String, Object> variables) {
        EmailSendRequest request = EmailSendRequest.builder()
                .to(Collections.singletonList(to))
                .templateCode(templateCode)
                .templateVariables(variables)
                .build();
        return sendEmail(request);
    }

    @Override
    public List<EmailSendResponse> sendBatchEmail(List<String> toEmails, String subject, String content) {
        List<EmailSendResponse> responses = new ArrayList<>();

        for (String email : toEmails) {
            try {
                EmailSendResponse response = sendSimpleEmail(email, subject, content);
                responses.add(response);
            } catch (Exception e) {
                responses.add(EmailSendResponse.builder()
                        .success(false)
                        .message("发送失败: " + e.getMessage())
                        .build());
            }
        }

        return responses;
    }

    @Override
    public EmailSendResponse sendEmailWithAttachments(EmailSendRequest request) {
        return sendEmail(request);
    }

    @Override
    public EmailSendResponse scheduleEmail(EmailSendRequest request) {
        return sendEmail(request);
    }

    @Override
    public EmailSendResponse resendEmail(Long emailRecordId) {
        try {
            EmailRecord emailRecord = emailRecordMapper.selectById(emailRecordId);
            if (emailRecord == null) {
                return EmailSendResponse.builder()
                        .success(false)
                        .message("邮件记录不存在")
                        .build();
            }

            // 重置状态和重试次数
            emailRecord.setStatus(EmailStatus.PENDING.getCode());
            emailRecord.setErrorMessage(null);
            emailRecord.setRetryCount(0);
            emailRecord.setUpdateTime(LocalDateTime.now());
            emailRecordMapper.updateById(emailRecord);

            // 重新发送
            if (emailConfig.getEnableQueue()) {
                return enqueueEmail(emailRecord);
            } else {
                return sendEmailDirectly(emailRecord);
            }

        } catch (Exception e) {
            return EmailSendResponse.builder()
                    .success(false)
                    .message("重发邮件失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean cancelEmail(Long emailRecordId) {
        try {
            EmailRecord emailRecord = emailRecordMapper.selectById(emailRecordId);
            if (emailRecord == null) {
                return false;
            }

            // 只有待发送和定时发送的邮件可以取消
            if (emailRecord.getStatus().equals(EmailStatus.PENDING.getCode()) ||
                emailRecord.getStatus().equals(EmailStatus.DRAFT.getCode())) {

                emailRecordMapper.updateStatus(emailRecordId, EmailStatus.CANCELLED.getCode(), "用户取消发送");
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public EmailRecord getEmailRecord(Long emailRecordId) {
        return emailRecordMapper.selectById(emailRecordId);
    }

    @Override
    public Map<String, Object> getEmailStatistics(String businessType, String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 解析日期
            LocalDateTime start = null;
            LocalDateTime end = null;

            if (StringUtils.hasText(startDate)) {
                start = LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (StringUtils.hasText(endDate)) {
                end = LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            // 查询统计数据
            Map<String, Object> params = new HashMap<>();
            params.put("businessType", businessType);
            params.put("startTime", start);
            params.put("endTime", end);

            List<Map<String, Object>> statusStats = emailRecordMapper.selectStatisticsByStatus(params);
            List<Map<String, Object>> dailyStats = emailRecordMapper.selectDailyStatistics(params);

            // 处理状态统计
            int totalSent = 0;
            int successCount = 0;
            int failedCount = 0;
            int pendingCount = 0;

            for (Map<String, Object> stat : statusStats) {
                int status = (Integer) stat.get("status");
                int count = ((Number) stat.get("count")).intValue();

                if (status == EmailStatus.SUCCESS.getCode()) {
                    successCount = count;
                } else if (status == EmailStatus.FAILED.getCode()) {
                    failedCount = count;
                } else if (status == EmailStatus.PENDING.getCode()) {
                    pendingCount = count;
                }
                totalSent += count;
            }

            // 计算成功率
            String successRate = totalSent > 0 ?
                String.format("%.2f%%", (double) successCount / totalSent * 100) : "0.00%";

            statistics.put("totalSent", totalSent);
            statistics.put("successCount", successCount);
            statistics.put("failedCount", failedCount);
            statistics.put("pendingCount", pendingCount);
            statistics.put("successRate", successRate);
            statistics.put("statusStatistics", statusStats);
            statistics.put("dailyStats", dailyStats);

        } catch (Exception e) {
            statistics.put("error", e.getMessage());
        }

        return statistics;
    }

    @Override
    public boolean validateEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean testEmailConfig() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailConfig.getUsername());
            message.setSubject("邮件配置测试");
            message.setText("这是一封邮件配置测试邮件，如果您收到此邮件，说明邮件配置正确。");
            message.setFrom(emailConfig.getUsername());

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("邮件配置测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 验证邮件发送请求
     */
    private void validateEmailRequest(EmailSendRequest request) {
        if (request.getTo() == null || request.getTo().isEmpty()) {
            throw new IllegalArgumentException("收件人不能为空");
        }

        if (!StringUtils.hasText(request.getSubject()) && !StringUtils.hasText(request.getTemplateCode())) {
            throw new IllegalArgumentException("邮件主题不能为空");
        }

        if (!StringUtils.hasText(request.getContent()) &&
            !StringUtils.hasText(request.getHtmlContent()) &&
            !StringUtils.hasText(request.getTemplateCode())) {
            throw new IllegalArgumentException("邮件内容不能为空");
        }

        // 验证邮箱格式
        for (String email : request.getTo()) {
            if (!validateEmail(email)) {
                throw new IllegalArgumentException("邮箱格式错误: " + email);
            }
        }

        // 验证收件人数量限制
        int totalRecipients = request.getTo().size();
        if (request.getCc() != null) {
            totalRecipients += request.getCc().size();
        }
        if (request.getBcc() != null) {
            totalRecipients += request.getBcc().size();
        }

        if (totalRecipients > emailConfig.getMaxRecipients()) {
            throw new IllegalArgumentException("收件人数量不能超过 " + emailConfig.getMaxRecipients());
        }
    }

    /**
     * 创建邮件记录
     */
    private EmailRecord createEmailRecord(EmailSendRequest request) {
        EmailRecord record = new EmailRecord();
        record.setToEmail(String.join(",", request.getTo()));

        if (request.getCc() != null && !request.getCc().isEmpty()) {
            record.setCcEmail(String.join(",", request.getCc()));
        }

        if (request.getBcc() != null && !request.getBcc().isEmpty()) {
            record.setBccEmail(String.join(",", request.getBcc()));
        }

        record.setSubject(request.getSubject());
        record.setContent(request.getContent());
        record.setHtmlContent(request.getHtmlContent());
        record.setTemplateCode(request.getTemplateCode());

        if (request.getTemplateVariables() != null) {
            record.setTemplateVariables(convertMapToJson(request.getTemplateVariables()));
        }

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            record.setAttachmentInfo(convertAttachmentsToJson(request.getAttachments()));
        }

        record.setBusinessType(request.getBusinessType());
        record.setPriority(request.getPriority() != null ? request.getPriority() : 3);
        record.setScheduleTime(request.getSendTime());
        record.setMaxRetryCount(request.getEnableRetry() != null && request.getEnableRetry() ?
                                emailConfig.getRetryCount() : 0);
        record.setStatus(EmailStatus.PENDING.getCode());
        record.setRetryCount(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        return record;
    }

    /**
     * 定时发送邮件
     */
    private EmailSendResponse scheduleEmailInternal(EmailRecord emailRecord) {
        // 更新状态为草稿，等待定时任务处理
        emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.DRAFT.getCode(), null);

        return EmailSendResponse.builder()
                .emailRecordId(emailRecord.getId())
                .success(true)
                .message("邮件已加入定时发送队列")
                .sentTime(LocalDateTime.now())
                .build();
    }

    /**
     * 邮件入队
     */
    private EmailSendResponse enqueueEmail(EmailRecord emailRecord) {
        boolean enqueued = emailQueueService.enqueue(emailRecord);

        if (enqueued) {
            return EmailSendResponse.builder()
                    .emailRecordId(emailRecord.getId())
                    .success(true)
                    .message("邮件已加入发送队列")
                    .sentTime(LocalDateTime.now())
                    .build();
        } else {
            emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.FAILED.getCode(), "加入队列失败");
            return EmailSendResponse.builder()
                    .emailRecordId(emailRecord.getId())
                    .success(false)
                    .message("邮件队列已满或加入队列失败")
                    .build();
        }
    }

    /**
     * 直接发送邮件
     */
    @Async
    public EmailSendResponse sendEmailDirectly(EmailRecord emailRecord) {
        try {
            // 更新状态为发送中
            emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.SENDING.getCode(), null);

            // 准备邮件内容
            String subject = emailRecord.getSubject();
            String content = emailRecord.getContent();
            String htmlContent = emailRecord.getHtmlContent();

            // 如果使用模板，渲染模板内容
            if (StringUtils.hasText(emailRecord.getTemplateCode())) {
                EmailTemplate template = emailTemplateService.getTemplateByCode(emailRecord.getTemplateCode());
                if (template != null) {
                    Map<String, Object> variables = convertJsonToMap(emailRecord.getTemplateVariables());

                    subject = templateEngineService.renderTemplate(template.getSubject(),
                                                                 template.getTemplateEngine(), variables);
                    htmlContent = templateEngineService.renderTemplate(template.getContent(),
                                                                     template.getTemplateEngine(), variables);
                }
            }

            // 发送邮件
            sendActualEmail(emailRecord, subject, content, htmlContent);

            // 更新状态为成功
            emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.SUCCESS.getCode(), null);
            emailRecordMapper.updateSentTime(emailRecord.getId(), LocalDateTime.now());

            return EmailSendResponse.builder()
                    .emailRecordId(emailRecord.getId())
                    .success(true)
                    .message("邮件发送成功")
                    .sentTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // 更新状态为失败
            emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.FAILED.getCode(), e.getMessage());

            return EmailSendResponse.builder()
                    .emailRecordId(emailRecord.getId())
                    .success(false)
                    .message("邮件发送失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 实际发送邮件
     */
    private void sendActualEmail(EmailRecord emailRecord, String subject, String content, String htmlContent)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, emailConfig.getEncoding());

        // 设置发件人
        String fromName = emailConfig.getFromName();
        if (StringUtils.hasText(fromName)) {
            helper.setFrom(emailConfig.getUsername(), fromName);
        } else {
            helper.setFrom(emailConfig.getUsername());
        }

        // 设置收件人
        String[] toEmails = emailRecord.getToEmail().split(",");
        helper.setTo(toEmails);

        // 设置抄送
        if (StringUtils.hasText(emailRecord.getCcEmail())) {
            String[] ccEmails = emailRecord.getCcEmail().split(",");
            helper.setCc(ccEmails);
        }

        // 设置密送
        if (StringUtils.hasText(emailRecord.getBccEmail())) {
            String[] bccEmails = emailRecord.getBccEmail().split(",");
            helper.setBcc(bccEmails);
        }

        // 设置主题
        helper.setSubject(subject);

        // 设置内容
        if (StringUtils.hasText(htmlContent)) {
            helper.setText(content != null ? content : "", htmlContent, true);
        } else {
            helper.setText(content != null ? content : "");
        }

        // 处理附件
        if (StringUtils.hasText(emailRecord.getAttachmentInfo())) {
            // 这里需要解析附件信息并添加附件
            // 由于附件处理比较复杂，这里先做简单处理
            addAttachments(helper, emailRecord.getAttachmentInfo());
        }

        // 发送邮件
        mailSender.send(message);
    }

    /**
     * 添加附件
     */
    private void addAttachments(MimeMessageHelper helper, String attachmentInfo) throws MessagingException {
        // 解析附件信息并添加到邮件中
        // 这里是简化实现，实际项目中需要根据具体的附件存储方式来实现
        try {
            List<Map<String, Object>> attachments = convertJsonToList(attachmentInfo);
            for (Map<String, Object> attachment : attachments) {
                String filename = (String) attachment.get("filename");
                String content = (String) attachment.get("content");

                if (StringUtils.hasText(filename) && StringUtils.hasText(content)) {
                    byte[] fileContent = Base64.getDecoder().decode(content);
                    helper.addAttachment(filename, new ByteArrayResource(fileContent));
                }
            }
        } catch (Exception e) {
            System.err.println("添加附件失败: " + e.getMessage());
        }
    }

    /**
     * 将Map转换为JSON字符串
     */
    private String convertMapToJson(Map<String, Object> map) {
        // 简化实现，实际项目中建议使用Jackson或Gson
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * 将JSON字符串转换为Map
     */
    private Map<String, Object> convertJsonToMap(String json) {
        // 简化实现，实际项目中建议使用Jackson或Gson
        Map<String, Object> map = new HashMap<>();
        if (!StringUtils.hasText(json) || "{}".equals(json)) {
            return map;
        }

        // 这里需要完整的JSON解析实现
        // 为了简化，返回空Map
        return map;
    }

    /**
     * 将附件信息转换为JSON
     */
    private String convertAttachmentsToJson(List<EmailSendRequest.EmailAttachment> attachments) {
        // 简化实现
        return "[]";
    }

    /**
     * 将JSON转换为List
     */
    private List<Map<String, Object>> convertJsonToList(String json) {
        // 简化实现
        return new ArrayList<>();
    }
}