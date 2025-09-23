package com.moon.cloud.email.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 邮件模板实体类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@TableName("email_template")
@Schema(description = "邮件模板")
public class EmailTemplate {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "模板ID")
    private Long id;

    @TableField("template_code")
    @Schema(description = "模板编码")
    private String templateCode;

    @TableField("template_name")
    @Schema(description = "模板名称")
    private String templateName;

    @TableField("template_type")
    @Schema(description = "模板类型")
    private String templateType;

    @TableField("subject")
    @Schema(description = "邮件主题")
    private String subject;

    @TableField("content")
    @Schema(description = "邮件内容")
    private String content;

    @TableField("template_engine")
    @Schema(description = "模板引擎：THYMELEAF、FREEMARKER")
    private String templateEngine;

    @TableField("variables")
    @Schema(description = "模板变量JSON")
    private String variables;

    @TableField("description")
    @Schema(description = "模板描述")
    private String description;

    @TableField("status")
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @TableField("is_default")
    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    @TableField("sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField("created_by")
    @Schema(description = "创建人")
    private Long createdBy;

    @TableField("updated_by")
    @Schema(description = "更新人")
    private Long updatedBy;

    public EmailTemplate() {}

    public EmailTemplate(String templateCode, String templateName, String templateType, String subject, String content) {
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.templateType = templateType;
        this.subject = subject;
        this.content = content;
        this.status = 1;
        this.isDefault = false;
        this.sortOrder = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
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

    public String getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "EmailTemplate{" +
                "id=" + id +
                ", templateCode='" + templateCode + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateType='" + templateType + '\'' +
                ", subject='" + subject + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}