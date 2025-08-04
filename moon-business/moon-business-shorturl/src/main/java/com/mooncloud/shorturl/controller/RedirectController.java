package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.service.ShortUrlRedirectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 重定向控制器
 * 
 * @author mooncloud
 */
@Controller
@Slf4j
public class RedirectController {
    
    @Autowired
    private ShortUrlRedirectService redirectService;
    
    /**
     * 短链重定向
     * 
     * @param shortUrl 短链标识符
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    @GetMapping("/{shortUrl}")
    public void redirect(@PathVariable String shortUrl,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        
        try {
            log.debug("处理短链重定向: {}", shortUrl);
            
            // 获取原始URL
            String originalUrl = redirectService.getOriginalUrl(shortUrl, request);
            
            if (originalUrl != null) {
                // 重定向到原始URL
                log.info("重定向: {} -> {}", shortUrl, originalUrl);
                response.sendRedirect(originalUrl);
            } else {
                // 短链不存在或已失效
                log.warn("短链不存在或已失效: {}", shortUrl);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "短链不存在或已失效");
            }
            
        } catch (Exception e) {
            log.error("重定向异常: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统异常");
        }
    }
    
    /**
     * 短链预览（显示目标URL信息，不直接跳转）
     * 
     * @param shortUrl 短链标识符
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    @GetMapping("/{shortUrl}/preview")
    public void preview(@PathVariable String shortUrl,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        
        try {
            log.debug("处理短链预览: {}", shortUrl);
            
            // 获取原始URL（不记录访问日志）
            String originalUrl = redirectService.getOriginalUrlForPreview(shortUrl);
            
            if (originalUrl != null) {
                // 返回预览页面
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(buildPreviewHtml(shortUrl, originalUrl));
            } else {
                // 短链不存在或已失效
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "短链不存在或已失效");
            }
            
        } catch (Exception e) {
            log.error("预览异常: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统异常");
        }
    }
    
    /**
     * 构建预览页面HTML
     * 
     * @param shortUrl 短链
     * @param originalUrl 原始URL
     * @return HTML内容
     */
    private String buildPreviewHtml(String shortUrl, String originalUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>链接预览</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 50px; }
                    .container { max-width: 600px; margin: 0 auto; }
                    .url { word-break: break-all; background: #f5f5f5; padding: 10px; border-radius: 5px; }
                    .button { display: inline-block; padding: 10px 20px; margin: 10px 5px; 
                             text-decoration: none; border-radius: 5px; color: white; }
                    .continue { background: #007bff; }
                    .back { background: #6c757d; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>链接预览</h2>
                    <p>您即将访问以下链接：</p>
                    <div class="url">%s</div>
                    <p>
                        <a href="%s" class="button continue">继续访问</a>
                        <a href="javascript:history.back()" class="button back">返回</a>
                    </p>
                    <p><small>短链: %s</small></p>
                </div>
            </body>
            </html>
            """, originalUrl, originalUrl, shortUrl);
    }
}