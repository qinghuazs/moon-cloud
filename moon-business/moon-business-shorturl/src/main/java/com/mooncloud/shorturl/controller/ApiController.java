package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.dto.ApiResponse;
import com.mooncloud.shorturl.dto.CreateShortUrlRequest;
import com.mooncloud.shorturl.dto.CreateShortUrlResponse;
import com.mooncloud.shorturl.entity.UrlAccessLogEntity;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.exception.NotFoundException;
import com.mooncloud.shorturl.mapper.UrlAccessLogMapper;
import com.mooncloud.shorturl.mapper.UrlMappingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mooncloud.shorturl.service.ShortUrlGeneratorService;
import com.mooncloud.shorturl.service.ShortUrlRedirectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
@RequestMapping("/api/v1")
@Slf4j
@Validated
public class ApiController {
    
    @Autowired
    private ShortUrlGeneratorService generatorService;
    
    @Autowired
    private ShortUrlRedirectService redirectService;
    
    @Autowired
    private UrlMappingMapper urlMappingMapper;

    @Autowired
    private UrlAccessLogMapper accessLogMapper;
    
    @Value("${app.domain:http://localhost:8080}")
    private String appDomain;
    
    /**
     * 生成短链API
     *
     * @param request 请求体
     * @return 生成结果
     */
    @PostMapping("/shorturl")
    public ApiResponse<CreateShortUrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        // 添加协议前缀（如果没有）
        String originalUrl = request.getOriginalUrl();
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
            request.setOriginalUrl(originalUrl);
        }

        // 生成短链
        String shortCode = generatorService.createShortUrl(request);

        // 构建响应
        CreateShortUrlResponse response = CreateShortUrlResponse.builder()
                .shortCode(shortCode)
                .shortUrl(appDomain + "/" + shortCode)
                .originalUrl(originalUrl)
                .isNew(true) // TODO: 从service返回
                .createdTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();

        return ApiResponse.success(response);
    }
    
    /**
     * 获取原始URL API
     *
     * @param shortCode 短链标识符
     * @return 原始URL
     */
    @GetMapping("/shorturl/{shortCode}")
    public ApiResponse<Map<String, String>> getOriginalUrl(@PathVariable String shortCode) {
        String originalUrl = redirectService.getOriginalUrlForPreview(shortCode);

        if (originalUrl == null) {
            throw new NotFoundException("短链不存在或已失效");
        }

        Map<String, String> data = new HashMap<>();
        data.put("originalUrl", originalUrl);
        data.put("shortCode", shortCode);

        return ApiResponse.success(data);
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
            QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("short_url", shortUrl);
            Optional<UrlMappingEntity> urlMapping = Optional.ofNullable(urlMappingMapper.selectOne(wrapper));
            
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
                QueryWrapper<UrlAccessLogEntity> accessWrapper = new QueryWrapper<>();
                accessWrapper.eq("short_url", shortUrl);
                Long totalAccess = accessLogMapper.selectCount(accessWrapper);
                Date startOfDay = Date.from(LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
                QueryWrapper<UrlAccessLogEntity> todayWrapper = new QueryWrapper<>();
                todayWrapper.eq("short_url", shortUrl)
                           .between("access_time", startOfDay, now);
                Long todayAccess = accessLogMapper.selectCount(todayWrapper);
                
                response.put("totalAccess", totalAccess);
                response.put("todayAccess", todayAccess);
                
                // 最近访问记录
                QueryWrapper<UrlAccessLogEntity> recentWrapper = new QueryWrapper<>();
                recentWrapper.eq("short_url", shortUrl)
                            .orderByDesc("access_time")
                            .last("LIMIT 10");
                List<UrlAccessLogEntity> recentAccess = accessLogMapper.selectList(recentWrapper);
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
            Long userIdLong = Long.parseLong(userId);
            QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userIdLong)
                  .orderByDesc("created_at");
            Page<UrlMappingEntity> pageParam = new Page<>(page + 1, size);
            Page<UrlMappingEntity> urlPage = urlMappingMapper.selectPage(pageParam, wrapper);

            response.put("success", true);
            response.put("content", urlPage.getRecords());
            response.put("totalElements", urlPage.getTotal());
            response.put("totalPages", urlPage.getPages());
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
            long totalUrls = urlMappingMapper.selectCount(null);
            
            // 今日新增
            Date startOfDay = Date.from(LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
            QueryWrapper<UrlMappingEntity> todayUrlsWrapper = new QueryWrapper<>();
            todayUrlsWrapper.between("created_at", startOfDay, now);
            long todayUrls = urlMappingMapper.selectCount(todayUrlsWrapper);
            
            // 总访问次数
            long totalAccess = accessLogMapper.selectCount(null);

            // 今日访问次数
            QueryWrapper<UrlAccessLogEntity> todayAccessWrapper = new QueryWrapper<>();
            todayAccessWrapper.between("access_time", startOfDay, now);
            long todayAccess = accessLogMapper.selectCount(todayAccessWrapper);
            
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