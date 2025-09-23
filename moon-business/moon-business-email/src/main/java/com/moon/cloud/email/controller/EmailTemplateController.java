package com.moon.cloud.email.controller;

import com.moon.cloud.email.dto.EmailTemplateCreateRequest;
import com.moon.cloud.email.entity.EmailTemplate;
import com.moon.cloud.email.service.EmailTemplateService;
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
 * 邮件模板管理控制器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Tag(name = "邮件模板管理", description = "邮件模板的增删改查和管理功能")
@RestController
@RequestMapping("/api/email/template")
public class EmailTemplateController {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Operation(summary = "创建邮件模板", description = "创建新的邮件模板")
    @PostMapping
    public MoonCloudResponse<Long> createTemplate(@Valid @RequestBody EmailTemplateCreateRequest request) {
        Long templateId = emailTemplateService.createTemplate(request);
        return MoonCloudResponse.success(templateId);
    }

    @Operation(summary = "更新邮件模板", description = "更新指定的邮件模板")
    @PutMapping("/{id}")
    public MoonCloudResponse<Boolean> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Valid @RequestBody EmailTemplateCreateRequest request) {
        boolean result = emailTemplateService.updateTemplate(id, request);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "删除邮件模板", description = "删除指定的邮件模板")
    @DeleteMapping("/{id}")
    public MoonCloudResponse<Boolean> deleteTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        boolean result = emailTemplateService.deleteTemplate(id);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "获取邮件模板详情", description = "根据ID获取邮件模板详情")
    @GetMapping("/{id}")
    public MoonCloudResponse<EmailTemplate> getTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id) {
        EmailTemplate template = emailTemplateService.getTemplateById(id);
        return MoonCloudResponse.success(template);
    }

    @Operation(summary = "根据编码获取模板", description = "根据模板编码获取邮件模板")
    @GetMapping("/code/{templateCode}")
    public MoonCloudResponse<EmailTemplate> getTemplateByCode(
            @Parameter(description = "模板编码") @PathVariable String templateCode) {
        EmailTemplate template = emailTemplateService.getTemplateByCode(templateCode);
        return MoonCloudResponse.success(template);
    }

    @Operation(summary = "获取模板列表", description = "获取邮件模板列表")
    @GetMapping("/list")
    public MoonCloudResponse<List<EmailTemplate>> getTemplateList(
            @Parameter(description = "模板类型") @RequestParam(required = false) String templateType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        List<EmailTemplate> templates = emailTemplateService.getTemplateList(templateType, status);
        return MoonCloudResponse.success(templates);
    }

    @Operation(summary = "分页查询模板", description = "分页查询邮件模板")
    @GetMapping("/page")
    public MoonCloudResponse<Map<String, Object>> getTemplatePageList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "模板类型") @RequestParam(required = false) String templateType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        Map<String, Object> result = emailTemplateService.getTemplatePageList(page, size, templateType, status, keyword);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "渲染模板", description = "渲染邮件模板内容")
    @PostMapping("/{templateCode}/render")
    public MoonCloudResponse<String> renderTemplate(
            @Parameter(description = "模板编码") @PathVariable String templateCode,
            @Parameter(description = "模板变量") @RequestBody(required = false) Map<String, Object> variables) {
        String content = emailTemplateService.renderTemplate(templateCode, variables);
        return MoonCloudResponse.success(content);
    }

    @Operation(summary = "渲染模板主题", description = "渲染邮件模板主题")
    @PostMapping("/{templateCode}/render/subject")
    public MoonCloudResponse<String> renderTemplateSubject(
            @Parameter(description = "模板编码") @PathVariable String templateCode,
            @Parameter(description = "模板变量") @RequestBody(required = false) Map<String, Object> variables) {
        String subject = emailTemplateService.renderTemplateSubject(templateCode, variables);
        return MoonCloudResponse.success(subject);
    }

    @Operation(summary = "验证模板语法", description = "验证邮件模板语法是否正确")
    @PostMapping("/validate")
    public MoonCloudResponse<Map<String, Object>> validateTemplate(
            @Parameter(description = "模板内容") @RequestParam String content,
            @Parameter(description = "模板引擎") @RequestParam(defaultValue = "THYMELEAF") String templateEngine) {
        Map<String, Object> result = emailTemplateService.validateTemplate(content, templateEngine);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "预览模板", description = "预览邮件模板效果")
    @PostMapping("/{templateCode}/preview")
    public MoonCloudResponse<Map<String, Object>> previewTemplate(
            @Parameter(description = "模板编码") @PathVariable String templateCode,
            @Parameter(description = "模板变量") @RequestBody(required = false) Map<String, Object> variables) {
        Map<String, Object> result = emailTemplateService.previewTemplate(templateCode, variables);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "复制模板", description = "复制现有邮件模板")
    @PostMapping("/{sourceId}/copy")
    public MoonCloudResponse<Long> copyTemplate(
            @Parameter(description = "源模板ID") @PathVariable Long sourceId,
            @Parameter(description = "新模板编码") @RequestParam String newTemplateCode,
            @Parameter(description = "新模板名称") @RequestParam String newTemplateName) {
        Long newTemplateId = emailTemplateService.copyTemplate(sourceId, newTemplateCode, newTemplateName);
        return MoonCloudResponse.success(newTemplateId);
    }

    @Operation(summary = "更新模板状态", description = "启用或禁用邮件模板")
    @PutMapping("/{id}/status")
    public MoonCloudResponse<Boolean> updateTemplateStatus(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam Integer status) {
        boolean result = emailTemplateService.updateTemplateStatus(id, status);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "设置默认模板", description = "设置指定类型的默认模板")
    @PutMapping("/{id}/default")
    public MoonCloudResponse<Boolean> setDefaultTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Parameter(description = "模板类型") @RequestParam String templateType) {
        boolean result = emailTemplateService.setDefaultTemplate(id, templateType);
        return MoonCloudResponse.success(result);
    }

    @Operation(summary = "获取模板变量", description = "获取模板的变量定义")
    @GetMapping("/{templateCode}/variables")
    public MoonCloudResponse<Map<String, Object>> getTemplateVariables(
            @Parameter(description = "模板编码") @PathVariable String templateCode) {
        Map<String, Object> variables = emailTemplateService.getTemplateVariables(templateCode);
        return MoonCloudResponse.success(variables);
    }
}