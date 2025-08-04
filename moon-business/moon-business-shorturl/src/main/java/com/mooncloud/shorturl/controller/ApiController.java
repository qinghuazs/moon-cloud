package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.dto.ShortUrlResult;
import com.mooncloud.shorturl.entity.UrlAccessLogEntity;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.repository.UrlAccessLogRepository;
import com.mooncloud.shorturl.repository.UrlMappingRepository;
import com.mooncloud.shorturl.service.ShortUrlGeneratorService;
import com.mooncloud.shorturl.service.ShortUrlRedirectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API控制器
 * 
 * @author mooncloud
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    
    @Autowired
    private ShortUrlGeneratorService generatorService;
    
    @Autowired
    private ShortUrlRedirectService redirectService;
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private UrlAccessLogRepository accessLogRepository;
    
    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;
    
    /**
     * 生成短链API
     * 
     * @param request 请求体
     * @return 生成结果
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateShortUrl(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String originalUrl = request.get("originalUrl");
            String customShortUrl = request.get("customShortUrl");
            String userId = request.get("userId");
            
            // 验证输入
            if (!StringUtils.hasText(originalUrl)) {
                response.put("success", false);
                response.put("message", "请输入有效的URL");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 添加协议前缀（如果没有）
            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                originalUrl = "https://" + originalUrl;
            }
            
            // 生成短链
            ShortUrlResult result = generatorService.generateShortUrl(originalUrl, customShortUrl, userId);
            
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            
            if (result.isSuccess()) {
                response.put("shortUrl", result.getShortUrl());
                response.put("fullShortUrl", appDomain + "/" + result.getShortUrl());
                response.put("originalUrl", originalUrl);
                response.put("isNew", result.isNew());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API生成短链异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "系统异常，请稍后重试");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取原始URL API
     * 
     * @param shortUrl 短链标识符
     * @return 原始URL
     */
    @GetMapping("/resolve/{shortUrl}")
    public ResponseEntity<Map<String, Object>> resolveShortUrl(@PathVariable String shortUrl) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String originalUrl = redirectService.getOriginalUrlForPreview(shortUrl);
            
            if (originalUrl != null) {
                response.put("success", true);
                response.put("originalUrl", originalUrl);
                response.put("shortUrl", shortUrl);
            } else {
                response.put("success", false);
                response.put("message", "短链不存在或已失效");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API解析短链异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "系统异常");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取短链统计信息
     * 
     * @param shortUrl 短链标识符
     * @return 统计信息
     */
    @GetMapping("/stats/{shortUrl}")
    public ResponseEntity<Map<String, Object>> getShortUrlStats(@PathVariable String shortUrl) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<UrlMappingEntity> urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
            
            if (urlMapping.isPresent()) {
                UrlMappingEntity entity = urlMapping.get();
                
                // 基本信息
                response.put("success", true);
                response.put("shortUrl", shortUrl);
                response.put("originalUrl", entity.getOriginalUrl());
                response.put("clickCount", entity.getClickCount());
                response.put("status", entity.getStatus().name());
                response.put("createdAt", entity.getCreatedAt());
                response.put("expiresAt", entity.getExpiresAt());
                
                // 访问统计
                Long totalAccess = accessLogRepository.countByShortUrl(shortUrl);
                Long todayAccess = accessLogRepository.countByShortUrlAndAccessTimeBetween(
                    shortUrl, 
                    LocalDateTime.now().toLocalDate().atStartOfDay(),
                    LocalDateTime.now()
                );
                
                response.put("totalAccess", totalAccess);
                response.put("todayAccess", todayAccess);
                
                // 最近访问记录
                List<UrlAccessLogEntity> recentAccess = accessLogRepository.findTop10ByShortUrlOrderByAccessTimeDesc(shortUrl);
                response.put("recentAccess", recentAccess);
                
            } else {
                response.put("success", false);
                response.put("message", "短链不存在");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API获取统计信息异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "系统异常");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取用户的短链列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 页大小
     * @return 短链列表
     */
    @GetMapping("/user/{userId}/urls")
    public ResponseEntity<Map<String, Object>> getUserUrls(@PathVariable String userId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<UrlMappingEntity> urlPage = urlMappingRepository.findByUserId(userId, pageable);
            
            response.put("success", true);
            response.put("content", urlPage.getContent());
            response.put("totalElements", urlPage.getTotalElements());
            response.put("totalPages", urlPage.getTotalPages());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API获取用户短链列表异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "系统异常");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 系统统计信息
     * 
     * @return 系统统计
     */
    @GetMapping("/system/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 总短链数
            long totalUrls = urlMappingRepository.count();
            
            // 今日新增
            long todayUrls = urlMappingRepository.countByCreatedAtBetween(
                LocalDateTime.now().toLocalDate().atStartOfDay(),
                LocalDateTime.now()
            );
            
            // 总访问次数
            long totalAccess = accessLogRepository.count();
            
            // 今日访问次数
            long todayAccess = accessLogRepository.countByAccessTimeBetween(
                LocalDateTime.now().toLocalDate().atStartOfDay(),
                LocalDateTime.now()
            );
            
            response.put("success", true);
            response.put("totalUrls", totalUrls);
            response.put("todayUrls", todayUrls);
            response.put("totalAccess", totalAccess);
            response.put("todayAccess", todayAccess);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API获取系统统计异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "系统异常");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}