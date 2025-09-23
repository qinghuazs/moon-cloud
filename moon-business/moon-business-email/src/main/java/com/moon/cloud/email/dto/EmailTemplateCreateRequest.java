package com.moon.cloud.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * 邮件模板创建请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Schema(description = "邮件模板创建请求")
public class EmailTemplateCreateRequest {

    @Schema(description = "模板编码")
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 100, message = "模板编码长度不能超过100字符")
    private String templateCode;

    @Schema(description = "模板名称")
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 200, message = "模板名称长度不能超过200字符")
    private String templateName;

    @Schema(description = "模板类型")
    @NotBlank(message = "模板类型不能为空")
    private String templateType;

    @Schema(description = "邮件主题")
    @NotBlank(message = "邮件主题不能为空")
    @Size(max = 500, message = "邮件主题长度不能超过500字符")
    private String subject;

    @Schema(description = "邮件内容")
    @NotBlank(message = "邮件内容不能为空")
    private String content;

    @Schema(description = "模板引擎：THYMELEAF、FREEMARKER")
    private String templateEngine = "THYMELEAF";

    @Schema(description = "模板变量定义")
    private Map<String, Object> variables;

    @Schema(description = "模板描述")
    @Size(max = 1000, message = "模板描述长度不能超过1000字符")
    private String description;

    @Schema(description = "是否默认模板")
    private Boolean isDefault = false;

    @Schema(description = "排序顺序")
    private Integer sortOrder = 0;

    public EmailTemplateCreateRequest() {}

    // Getters and Setters
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

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}