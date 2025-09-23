package com.moon.cloud.email.service;

import com.moon.cloud.email.dto.EmailTemplateCreateRequest;
import com.moon.cloud.email.entity.EmailTemplate;

import java.util.List;
import java.util.Map;

/**
 * 邮件模板服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface EmailTemplateService {

    /**
     * 创建邮件模板
     *
     * @param request 创建请求
     * @return 模板ID
     */
    Long createTemplate(EmailTemplateCreateRequest request);

    /**
     * 更新邮件模板
     *
     * @param id 模板ID
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateTemplate(Long id, EmailTemplateCreateRequest request);

    /**
     * 删除邮件模板
     *
     * @param id 模板ID
     * @return 是否成功
     */
    boolean deleteTemplate(Long id);

    /**
     * 根据ID获取模板
     *
     * @param id 模板ID
     * @return 邮件模板
     */
    EmailTemplate getTemplateById(Long id);

    /**
     * 根据编码获取模板
     *
     * @param templateCode 模板编码
     * @return 邮件模板
     */
    EmailTemplate getTemplateByCode(String templateCode);

    /**
     * 获取模板列表
     *
     * @param templateType 模板类型
     * @param status 状态
     * @return 模板列表
     */
    List<EmailTemplate> getTemplateList(String templateType, Integer status);

    /**
     * 分页查询模板
     *
     * @param page 页码
     * @param size 页大小
     * @param templateType 模板类型
     * @param status 状态
     * @param keyword 关键词
     * @return 分页结果
     */
    Map<String, Object> getTemplatePageList(int page, int size, String templateType, Integer status, String keyword);

    /**
     * 渲染模板内容
     *
     * @param templateCode 模板编码
     * @param variables 变量
     * @return 渲染后的内容
     */
    String renderTemplate(String templateCode, Map<String, Object> variables);

    /**
     * 渲染模板主题
     *
     * @param templateCode 模板编码
     * @param variables 变量
     * @return 渲染后的主题
     */
    String renderTemplateSubject(String templateCode, Map<String, Object> variables);

    /**
     * 验证模板语法
     *
     * @param content 模板内容
     * @param templateEngine 模板引擎
     * @return 验证结果
     */
    Map<String, Object> validateTemplate(String content, String templateEngine);

    /**
     * 预览模板
     *
     * @param templateCode 模板编码
     * @param variables 变量
     * @return 预览内容
     */
    Map<String, Object> previewTemplate(String templateCode, Map<String, Object> variables);

    /**
     * 复制模板
     *
     * @param sourceId 源模板ID
     * @param newTemplateCode 新模板编码
     * @param newTemplateName 新模板名称
     * @return 新模板ID
     */
    Long copyTemplate(Long sourceId, String newTemplateCode, String newTemplateName);

    /**
     * 启用/禁用模板
     *
     * @param id 模板ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateTemplateStatus(Long id, Integer status);

    /**
     * 设置默认模板
     *
     * @param id 模板ID
     * @param templateType 模板类型
     * @return 是否成功
     */
    boolean setDefaultTemplate(Long id, String templateType);

    /**
     * 获取模板变量定义
     *
     * @param templateCode 模板编码
     * @return 变量定义
     */
    Map<String, Object> getTemplateVariables(String templateCode);
}