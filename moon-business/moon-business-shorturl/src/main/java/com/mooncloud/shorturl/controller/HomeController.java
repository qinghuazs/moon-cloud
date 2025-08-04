package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.dto.ShortUrlResult;
import com.mooncloud.shorturl.service.ShortUrlGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 主页控制器
 * 
 * @author mooncloud
 */
@Controller
@Slf4j
public class HomeController {
    
    @Autowired
    private ShortUrlGeneratorService shortUrlGeneratorService;
    
    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;
    
    /**
     * 首页
     * 
     * @param model 模型对象
     * @return 首页模板
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appDomain", appDomain);
        return "index";
    }
    
    /**
     * 生成短链
     * 
     * @param originalUrl 原始URL
     * @param customShortUrl 自定义短链（可选）
     * @param model 模型对象
     * @param redirectAttributes 重定向属性
     * @return 结果页面或重定向
     */
    @PostMapping("/generate")
    public String generateShortUrl(@RequestParam("originalUrl") String originalUrl,
                                 @RequestParam(value = "customShortUrl", required = false) String customShortUrl,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            // 验证输入
            if (!StringUtils.hasText(originalUrl)) {
                model.addAttribute("error", "请输入有效的URL");
                model.addAttribute("appDomain", appDomain);
                return "index";
            }
            
            // 添加协议前缀（如果没有）
            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                originalUrl = "https://" + originalUrl;
            }
            
            // 生成短链
            ShortUrlResult result = shortUrlGeneratorService.generateShortUrl(originalUrl, customShortUrl, null);
            
            if (result.isSuccess()) {
                // 成功
                String fullShortUrl = appDomain + "/" + result.getShortUrl();
                model.addAttribute("success", true);
                model.addAttribute("originalUrl", originalUrl);
                model.addAttribute("shortUrl", result.getShortUrl());
                model.addAttribute("fullShortUrl", fullShortUrl);
                model.addAttribute("isNew", result.isNew());
                model.addAttribute("appDomain", appDomain);
                
                log.info("短链生成成功: {} -> {}", originalUrl, result.getShortUrl());
                
            } else {
                // 失败
                model.addAttribute("error", result.getMessage());
                model.addAttribute("originalUrl", originalUrl);
                model.addAttribute("customShortUrl", customShortUrl);
                model.addAttribute("appDomain", appDomain);
                
                log.warn("短链生成失败: {}", result.getMessage());
            }
            
            return "index";
            
        } catch (Exception e) {
            log.error("短链生成异常: {}", e.getMessage(), e);
            model.addAttribute("error", "系统异常，请稍后重试");
            model.addAttribute("originalUrl", originalUrl);
            model.addAttribute("customShortUrl", customShortUrl);
            model.addAttribute("appDomain", appDomain);
            return "index";
        }
    }
    
    /**
     * 关于页面
     * 
     * @return 关于页面模板
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    /**
     * 帮助页面
     * 
     * @return 帮助页面模板
     */
    @GetMapping("/help")
    public String help() {
        return "help";
    }
}