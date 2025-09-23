package com.moon.cloud.email.service;

import java.util.Map;

/**
 * 模板引擎服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface TemplateEngineService {

    /**
     * 渲染Thymeleaf模板
     *
     * @param templateContent 模板内容
     * @param variables 变量
     * @return 渲染结果
     */
    String renderThymeleafTemplate(String templateContent, Map<String, Object> variables);

    /**
     * 渲染FreeMarker模板
     *
     * @param templateContent 模板内容
     * @param variables 变量
     * @return 渲染结果
     */
    String renderFreemarkerTemplate(String templateContent, Map<String, Object> variables);

    /**
     * 根据引擎类型渲染模板
     *
     * @param templateEngine 模板引擎类型
     * @param templateContent 模板内容
     * @param variables 变量
     * @return 渲染结果
     */
    String renderTemplate(String templateEngine, String templateContent, Map<String, Object> variables);

    /**
     * 验证模板语法
     *
     * @param templateEngine 模板引擎类型
     * @param templateContent 模板内容
     * @return 验证结果
     */
    Map<String, Object> validateTemplate(String templateEngine, String templateContent);

    /**
     * 提取模板变量
     *
     * @param templateEngine 模板引擎类型
     * @param templateContent 模板内容
     * @return 变量列表
     */
    Map<String, Object> extractVariables(String templateEngine, String templateContent);
}