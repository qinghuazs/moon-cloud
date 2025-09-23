package com.moon.cloud.email.service.impl;

import com.moon.cloud.email.service.TemplateEngineService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板引擎服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class TemplateEngineServiceImpl implements TemplateEngineService {

    @Autowired
    private TemplateEngine thymeleafEngine;

    private final Configuration freemarkerConfig;

    public TemplateEngineServiceImpl() {
        // 初始化FreeMarker配置
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        this.freemarkerConfig.setDefaultEncoding("UTF-8");
        this.freemarkerConfig.setNumberFormat("0.######");
    }

    @Override
    public String renderThymeleafTemplate(String templateContent, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            return thymeleafEngine.process(templateContent, context);
        } catch (Exception e) {
            throw new RuntimeException("Thymeleaf模板渲染失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String renderFreemarkerTemplate(String templateContent, Map<String, Object> variables) {
        try {
            Template template = new Template("emailTemplate", new StringReader(templateContent), freemarkerConfig);
            StringWriter writer = new StringWriter();
            template.process(variables != null ? variables : new HashMap<>(), writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("FreeMarker模板渲染失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String renderTemplate(String templateEngine, String templateContent, Map<String, Object> variables) {
        if ("THYMELEAF".equalsIgnoreCase(templateEngine)) {
            return renderThymeleafTemplate(templateContent, variables);
        } else if ("FREEMARKER".equalsIgnoreCase(templateEngine)) {
            return renderFreemarkerTemplate(templateContent, variables);
        } else {
            throw new IllegalArgumentException("不支持的模板引擎: " + templateEngine);
        }
    }

    @Override
    public Map<String, Object> validateTemplate(String templateEngine, String templateContent) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", false);
        result.put("errors", new ArrayList<>());

        try {
            if ("THYMELEAF".equalsIgnoreCase(templateEngine)) {
                // Thymeleaf语法验证
                validateThymeleafTemplate(templateContent);
            } else if ("FREEMARKER".equalsIgnoreCase(templateEngine)) {
                // FreeMarker语法验证
                validateFreemarkerTemplate(templateContent);
            } else {
                List<String> errors = (List<String>) result.get("errors");
                errors.add("不支持的模板引擎: " + templateEngine);
                return result;
            }
            result.put("valid", true);
        } catch (Exception e) {
            List<String> errors = (List<String>) result.get("errors");
            errors.add(e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> extractVariables(String templateEngine, String templateContent) {
        Map<String, Object> result = new HashMap<>();
        Set<String> variables = new HashSet<>();

        try {
            if ("THYMELEAF".equalsIgnoreCase(templateEngine)) {
                variables = extractThymeleafVariables(templateContent);
            } else if ("FREEMARKER".equalsIgnoreCase(templateEngine)) {
                variables = extractFreemarkerVariables(templateContent);
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        result.put("variables", new ArrayList<>(variables));
        return result;
    }

    /**
     * 验证Thymeleaf模板语法
     */
    private void validateThymeleafTemplate(String templateContent) {
        try {
            Context context = new Context();
            thymeleafEngine.process(templateContent, context);
        } catch (Exception e) {
            throw new RuntimeException("Thymeleaf模板语法错误: " + e.getMessage());
        }
    }

    /**
     * 验证FreeMarker模板语法
     */
    private void validateFreemarkerTemplate(String templateContent) {
        try {
            new Template("test", new StringReader(templateContent), freemarkerConfig);
        } catch (IOException e) {
            throw new RuntimeException("FreeMarker模板语法错误: " + e.getMessage());
        }
    }

    /**
     * 提取Thymeleaf模板变量
     */
    private Set<String> extractThymeleafVariables(String templateContent) {
        Set<String> variables = new HashSet<>();

        // 匹配 ${variable} 格式的变量
        Pattern pattern1 = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(templateContent);
        while (matcher1.find()) {
            String var = matcher1.group(1);
            if (var.contains(".")) {
                var = var.substring(0, var.indexOf("."));
            }
            variables.add(var);
        }

        // 匹配 th:text="${variable}" 格式的变量
        Pattern pattern2 = Pattern.compile("th:[^=]*=\"\\$\\{([^}]+)\\}\"");
        Matcher matcher2 = pattern2.matcher(templateContent);
        while (matcher2.find()) {
            String var = matcher2.group(1);
            if (var.contains(".")) {
                var = var.substring(0, var.indexOf("."));
            }
            variables.add(var);
        }

        return variables;
    }

    /**
     * 提取FreeMarker模板变量
     */
    private Set<String> extractFreemarkerVariables(String templateContent) {
        Set<String> variables = new HashSet<>();

        // 匹配 ${variable} 格式的变量
        Pattern pattern1 = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher1 = pattern1.matcher(templateContent);
        while (matcher1.find()) {
            String var = matcher1.group(1);
            if (var.contains(".")) {
                var = var.substring(0, var.indexOf("."));
            }
            if (var.contains("(")) {
                var = var.substring(0, var.indexOf("("));
            }
            variables.add(var);
        }

        // 匹配 <#assign> 和 <#if> 等指令中的变量
        Pattern pattern2 = Pattern.compile("<#[^>]*\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher matcher2 = pattern2.matcher(templateContent);
        while (matcher2.find()) {
            String var = matcher2.group(1);
            if (!isFreemarkerKeyword(var)) {
                variables.add(var);
            }
        }

        return variables;
    }

    /**
     * 判断是否为FreeMarker关键字
     */
    private boolean isFreemarkerKeyword(String word) {
        Set<String> keywords = Set.of("assign", "if", "else", "elseif", "list", "break", "continue",
                "return", "include", "import", "macro", "function", "switch", "case", "default");
        return keywords.contains(word.toLowerCase());
    }
}