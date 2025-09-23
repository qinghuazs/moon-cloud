package com.moon.cloud.email.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 邮件发送记录实体类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@TableName("email_record")
@Schema(description = "邮件发送记录")
public class EmailRecord {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    @TableField("template_id")
    @Schema(description = "模板ID")
    private Long templateId;

    @TableField("template_code")
    @Schema(description = "模板编码")
    private String templateCode;

    @TableField("from_email")
    @Schema(description = "发送方邮箱")
    private String fromEmail;

    @TableField("from_name")
    @Schema(description = "发送方名称")
    private String fromName;

    @TableField("to_emails")
    @Schema(description = "收件人邮箱(多个用逗号分隔)")
    private String toEmails;

    @TableField("cc_emails")
    @Schema(description = "抄送邮箱(多个用逗号分隔)")
    private String ccEmails;

    @TableField("bcc_emails")
    @Schema(description = "密送邮箱(多个用逗号分隔)")
    private String bccEmails;

    @TableField("subject")
    @Schema(description = "邮件主题")
    private String subject;

    @TableField("content")
    @Schema(description = "邮件内容")
    private String content;

    @TableField("email_type")
    @Schema(description = "邮件类型：1-纯文本，2-HTML，3-模板，4-带附件")
    private Integer emailType;

    @TableField("template_variables")
    @Schema(description = "模板变量JSON")
    private String templateVariables;

    @TableField("attachments")
    @Schema(description = "附件信息JSON")
    private String attachments;

    @TableField("status")
    @Schema(description = "发送状态：0-待发送，1-发送中，2-成功，3-失败，4-取消，5-重试中")
    private Integer status;

    @TableField("retry_count")
    @Schema(description = "重试次数")
    private Integer retryCount;

    @TableField("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;

    @TableField("send_at")
    @Schema(description = "发送时间")
    private LocalDateTime sendAt;

    @TableField("scheduled_at")
    @Schema(description = "计划发送时间")
    private LocalDateTime scheduledAt;

    @TableField("completed_at")
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    @Schema(description = "创建人")
    private Long createdBy;

    @TableField("business_id")
    @Schema(description = "业务ID")
    private String businessId;

    @TableField("business_type")
    @Schema(description = "业务类型")
    private String businessType;

    public EmailRecord() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToEmails() {
        return toEmails;
    }

    public void setToEmails(String toEmails) {
        this.toEmails = toEmails;
    }

    public String getCcEmails() {
        return ccEmails;
    }

    public void setCcEmails(String ccEmails) {
        this.ccEmails = ccEmails;
    }

    public String getBccEmails() {
        return bccEmails;
    }

    public void setBccEmails(String bccEmails) {
        this.bccEmails = bccEmails;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getEmailType() {
        return emailType;
    }

    public void setEmailType(Integer emailType) {
        this.emailType = emailType;
    }

    public String getTemplateVariables() {
        return templateVariables;
    }

    public void setTemplateVariables(String templateVariables) {
        this.templateVariables = templateVariables;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getSendAt() {
        return sendAt;
    }

    public void setSendAt(LocalDateTime sendAt) {
        this.sendAt = sendAt;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    @Override
    public String toString() {
        return "EmailRecord{" +
                "id=" + id +
                ", templateCode='" + templateCode + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", toEmails='" + toEmails + '\'' +
                ", subject='" + subject + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}