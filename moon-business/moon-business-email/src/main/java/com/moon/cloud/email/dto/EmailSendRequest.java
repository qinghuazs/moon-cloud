package com.moon.cloud.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Schema(description = "邮件发送请求")
public class EmailSendRequest {

    @Schema(description = "收件人邮箱列表")
    @NotEmpty(message = "收件人不能为空")
    private List<@Email(message = "邮箱格式不正确") String> toEmails;

    @Schema(description = "抄送邮箱列表")
    private List<@Email(message = "邮箱格式不正确") String> ccEmails;

    @Schema(description = "密送邮箱列表")
    private List<@Email(message = "邮箱格式不正确") String> bccEmails;

    @Schema(description = "邮件主题")
    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    @Schema(description = "邮件内容")
    private String content;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板变量")
    private Map<String, Object> templateVariables;

    @Schema(description = "邮件类型：1-纯文本，2-HTML，3-模板，4-带附件")
    private Integer emailType;

    @Schema(description = "附件列表")
    private List<AttachmentInfo> attachments;

    @Schema(description = "计划发送时间")
    private LocalDateTime scheduledAt;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "是否异步发送")
    private Boolean async = true;

    @Schema(description = "发送方邮箱（可选，默认使用系统配置）")
    @Email(message = "发送方邮箱格式不正确")
    private String fromEmail;

    @Schema(description = "发送方名称（可选，默认使用系统配置）")
    private String fromName;

    public EmailSendRequest() {}

    // 附件信息内部类
    @Schema(description = "附件信息")
    public static class AttachmentInfo {
        @Schema(description = "附件名称")
        private String fileName;

        @Schema(description = "附件路径或Base64内容")
        private String fileContent;

        @Schema(description = "内容类型")
        private String contentType;

        @Schema(description = "是否为Base64编码")
        private Boolean isBase64 = false;

        public AttachmentInfo() {}

        public AttachmentInfo(String fileName, String fileContent, String contentType) {
            this.fileName = fileName;
            this.fileContent = fileContent;
            this.contentType = contentType;
        }

        // Getters and Setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileContent() {
            return fileContent;
        }

        public void setFileContent(String fileContent) {
            this.fileContent = fileContent;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Boolean getIsBase64() {
            return isBase64;
        }

        public void setIsBase64(Boolean isBase64) {
            this.isBase64 = isBase64;
        }
    }

    // Getters and Setters
    public List<String> getToEmails() {
        return toEmails;
    }

    public void setToEmails(List<String> toEmails) {
        this.toEmails = toEmails;
    }

    public List<String> getCcEmails() {
        return ccEmails;
    }

    public void setCcEmails(List<String> ccEmails) {
        this.ccEmails = ccEmails;
    }

    public List<String> getBccEmails() {
        return bccEmails;
    }

    public void setBccEmails(List<String> bccEmails) {
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

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, Object> getTemplateVariables() {
        return templateVariables;
    }

    public void setTemplateVariables(Map<String, Object> templateVariables) {
        this.templateVariables = templateVariables;
    }

    public Integer getEmailType() {
        return emailType;
    }

    public void setEmailType(Integer emailType) {
        this.emailType = emailType;
    }

    public List<AttachmentInfo> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentInfo> attachments) {
        this.attachments = attachments;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
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

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
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
}