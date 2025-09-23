package com.moon.cloud.email.controller;

import com.moon.cloud.email.dto.EmailSendRequest;
import com.moon.cloud.email.dto.EmailSendResponse;
import com.moon.cloud.email.entity.EmailRecord;
import com.moon.cloud.email.service.EmailQueueService;
import com.moon.cloud.email.service.EmailService;
import com.moon.cloud.response.web.MoonCloudResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 邮件管理控制器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Tag(name = "邮件管理", description = "邮件发送和管理相关接口")
@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailQueueService emailQueueService;

    @Operation(summary = "发送邮件", description = "发送单个或批量邮件")
    @PostMapping("/send")
    public MoonCloudResponse<EmailSendResponse> sendEmail(@Valid @RequestBody EmailSendRequest request) {
        EmailSendResponse response = emailService.sendEmail(request);
        return MoonCloudResponse.success(response);
    }

    @Operation(summary = "发送简单文本邮件", description = "发送简单的文本邮件")
    @PostMapping("/send/simple")
    public MoonCloudResponse<EmailSendResponse> sendSimpleEmail(
            @Parameter(description = "收件人邮箱") @RequestParam String to,
            @Parameter(description = "邮件主题") @RequestParam String subject,
            @Parameter(description = "邮件内容") @RequestParam String content) {
        EmailSendResponse response = emailService.sendSimpleEmail(to, subject, content);
        return MoonCloudResponse.success(response);
    }

    @Operation(summary = "发送HTML邮件", description = "发送HTML格式的邮件")
    @PostMapping("/send/html")
    public MoonCloudResponse<EmailSendResponse> sendHtmlEmail(
            @Parameter(description = "收件人邮箱") @RequestParam String to,
            @Parameter(description = "邮件主题") @RequestParam String subject,
            @Parameter(description = "HTML内容") @RequestParam String htmlContent) {
        EmailSendResponse response = emailService.sendHtmlEmail(to, subject, htmlContent);
        return MoonCloudResponse.success(response);
    }

    @Operation(summary = "发送模板邮件", description = "使用模板发送邮件")
    @PostMapping("/send/template")
    public MoonCloudResponse<EmailSendResponse> sendTemplateEmail(
            @Parameter(description = "收件人邮箱") @RequestParam String to,
            @Parameter(description = "模板编码") @RequestParam String templateCode,
            @Parameter(description = "模板变量") @RequestBody(required = false) Map<String, Object> variables) {
        EmailSendResponse response = emailService.sendTemplateEmail(to, templateCode, variables);
        return MoonCloudResponse.success(response);
    }

    @Operation(summary = "批量发送邮件", description = "批量发送邮件给多个收件人")
    @PostMapping("/send/batch")
    public MoonCloudResponse<List<EmailSendResponse>> sendBatchEmail(
            @Parameter(description = "收件人邮箱列表") @RequestParam List<String> toEmails,
            @Parameter(description = "邮件主题") @RequestParam String subject,
            @Parameter(description = "邮件内容") @RequestParam String content) {
        List<EmailSendResponse> responses = emailService.sendBatchEmail(toEmails, subject, content);
        return MoonCloudResponse.success(responses);
    }

    @Operation(summary = "定时发送邮件", description = "设置邮件定时发送")
    @PostMapping("/send/schedule")
    public MoonCloudResponse<EmailSendResponse> scheduleEmail(@Valid @RequestBody EmailSendRequest request) {
        EmailSendResponse response = emailService.scheduleEmail(request);
        return MoonCloudResponse.success(response);
    }

    @Operation(summary = "重发邮件", description = "重新发送失败的邮件")
    @PostMapping("/resend/{emailRecordId}")
    public MoonCloudResponse<EmailSendResponse> resendEmail(
            @Parameter(description = "邮件记录ID") @PathVariable Long emailRecordId) {
        EmailSendResponse response = emailService.resendEmail(emailRecordId);
        return MoonCloudResponse.success(response);
    }

    @Operation(summary = "取消发送邮件", description = "取消待发送或定时发送的邮件")
    @PostMapping("/cancel/{emailRecordId}")
    public MoonCloudResponse<Boolean> cancelEmail(
            @Parameter(description = "邮件记录ID") @PathVariable Long emailRecordId) {
        boolean result = emailService.cancelEmail(emailRecordId);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "获取邮件记录", description = "根据ID获取邮件发送记录")
    @GetMapping("/record/{emailRecordId}")
    public MoonCloudResponse<EmailRecord> getEmailRecord(
            @Parameter(description = "邮件记录ID") @PathVariable Long emailRecordId) {
        EmailRecord record = emailService.getEmailRecord(emailRecordId);
        return MoonCloudResponse.success(record);
    }

    @Operation(summary = "获取邮件统计", description = "获取邮件发送统计信息")
    @GetMapping("/statistics")
    public MoonCloudResponse<Map<String, Object>> getEmailStatistics(
            @Parameter(description = "业务类型") @RequestParam(required = false) String businessType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        Map<String, Object> statistics = emailService.getEmailStatistics(businessType, startDate, endDate);
        return MoonCloudResponse.success(statistics);
    }

    @Operation(summary = "验证邮箱地址", description = "验证邮箱地址格式是否正确")
    @GetMapping("/validate")
    public MoonCloudResponse<Boolean> validateEmail(
            @Parameter(description = "邮箱地址") @RequestParam String email) {
        boolean isValid = emailService.validateEmail(email);
        return MoonCloudResponse.success(isValid);
    }

    @Operation(summary = "测试邮件配置", description = "测试邮件服务器配置是否正确")
    @PostMapping("/test")
    public MoonCloudResponse<Boolean> testEmailConfig() {
        boolean result = emailService.testEmailConfig();
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "获取队列状态", description = "获取邮件队列状态信息")
    @GetMapping("/queue/status")
    public MoonCloudResponse<Object> getQueueStatus() {
        Object status = emailQueueService.getQueueStatus();
        return MoonCloudResponse.success(status);
    }

    @Operation(summary = "处理邮件队列", description = "手动触发邮件队列处理")
    @PostMapping("/queue/process")
    public MoonCloudResponse<Void> processQueue() {
        emailQueueService.processQueue();
        return MoonCloudResponse.success();
    }

    @Operation(summary = "清空邮件队列", description = "清空所有待发送的邮件队列")
    @PostMapping("/queue/clear")
    public MoonCloudResponse<Boolean> clearQueue() {
        boolean result = emailQueueService.clearQueue();
        return MoonCloudResponse.success(result);
    }
}