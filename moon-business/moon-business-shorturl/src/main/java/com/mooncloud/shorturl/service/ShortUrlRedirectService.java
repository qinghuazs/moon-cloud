package com.mooncloud.shorturl.service;

import com.mooncloud.shorturl.entity.UrlAccessLogEntity;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.enums.UrlStatus;
import com.mooncloud.shorturl.exception.ExpiredException;
import com.mooncloud.shorturl.exception.NotFoundException;
import com.mooncloud.shorturl.mapper.UrlAccessLogMapper;
import com.mooncloud.shorturl.mapper.UrlMappingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 短链重定向服务
 * 
 * @author mooncloud
 */
@Service
@Slf4j
public class ShortUrlRedirectService {
    
    @Autowired
    private UrlMappingMapper urlMappingMapper;

    @Autowired
    private UrlAccessLogMapper urlAccessLogMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String SHORT_URL_PREFIX = "short_url:";
    
    /**
     * 解析短链并获取原始URL
     * 
     * @param shortUrl 短链标识符
     * @param request HTTP请求对象
     * @return 原始URL，如果不存在或已过期则返回null
     */
    @Transactional
    public String resolveShortUrl(String shortUrl, HttpServletRequest request) {
        // 1. 从缓存获取原始URL
        String originalUrl = getOriginalUrlFromCache(shortUrl);
        if (StringUtils.hasText(originalUrl)) {
            // 异步记录访问日志
            recordAccessLogAsync(shortUrl, request);
            // 异步增加点击次数
            incrementClickCountAsync(shortUrl);
            return originalUrl;
        }

        // 2. 从数据库查询
        QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("short_url", shortUrl);
        Optional<UrlMappingEntity> mappingOpt = Optional.ofNullable(urlMappingMapper.selectOne(wrapper));
        if (mappingOpt.isEmpty()) {
            log.warn("短链不存在: {}", shortUrl);
            throw new NotFoundException("短链不存在");
        }

        UrlMappingEntity mapping = mappingOpt.get();

        // 3. 检查URL状态
        if (!isUrlAccessible(mapping)) {
            log.warn("短链不可访问: {}, 状态: {}", shortUrl, mapping.getStatus());
            if (mapping.getStatus() == UrlStatus.EXPIRED) {
                throw new ExpiredException("短链已过期");
            } else {
                throw new NotFoundException("短链不可访问");
            }
        }

        // 4. 更新缓存
        updateCache(shortUrl, mapping.getOriginalUrl());

        // 5. 记录访问日志
        recordAccessLog(shortUrl, request);

        // 6. 增加点击次数
        urlMappingMapper.incrementClickCount(shortUrl);

        log.info("短链解析成功: {} -> {}", shortUrl, mapping.getOriginalUrl());
        return mapping.getOriginalUrl();
    }
    
    /**
     * 从缓存获取原始URL
     * 
     * @param shortUrl 短链标识符
     * @return 原始URL
     */
    private String getOriginalUrlFromCache(String shortUrl) {
        try {
            String cacheKey = SHORT_URL_PREFIX + shortUrl;
            return (String) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.error("缓存查询失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查URL是否可访问
     * 
     * @param mapping URL映射实体
     * @return 是否可访问
     */
    private boolean isUrlAccessible(UrlMappingEntity mapping) {
        // 检查状态
        if (mapping.getStatus() != UrlStatus.ACTIVE) {
            return false;
        }
        
        // 检查是否过期
        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().before(new Date())) {
            // 更新状态为过期
            mapping.setStatus(UrlStatus.EXPIRED);
            urlMappingMapper.updateById(mapping);
            return false;
        }
        
        return true;
    }
    
    /**
     * 更新缓存
     * 
     * @param shortUrl 短链
     * @param originalUrl 原始URL
     */
    private void updateCache(String shortUrl, String originalUrl) {
        try {
            String cacheKey = SHORT_URL_PREFIX + shortUrl;
            redisTemplate.opsForValue().set(cacheKey, originalUrl, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("缓存更新失败: {}", e.getMessage());
        }
    }
    
    /**
     * 记录访问日志
     * 
     * @param shortUrl 短链标识符
     * @param request HTTP请求对象
     */
    private void recordAccessLog(String shortUrl, HttpServletRequest request) {
        try {
            UrlAccessLogEntity accessLog = new UrlAccessLogEntity();
            accessLog.setShortUrl(shortUrl);
            accessLog.setIpAddress(getClientIpAddress(request));
            accessLog.setUserAgent(request.getHeader("User-Agent"));
            accessLog.setReferer(request.getHeader("Referer"));
            accessLog.setAccessTime(new Date());
            
            // 解析用户代理信息
            parseUserAgent(accessLog, request.getHeader("User-Agent"));
            
            urlAccessLogMapper.insert(accessLog);
            
        } catch (Exception e) {
            log.error("访问日志记录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 异步记录访问日志
     * 
     * @param shortUrl 短链标识符
     * @param request HTTP请求对象
     */
    private void recordAccessLogAsync(String shortUrl, HttpServletRequest request) {
        // 这里可以使用异步处理，比如发送到消息队列
        // 为了简化，这里直接调用同步方法
        recordAccessLog(shortUrl, request);
    }
    
    /**
     * 异步增加点击次数
     * 
     * @param shortUrl 短链标识符
     */
    private void incrementClickCountAsync(String shortUrl) {
        try {
            urlMappingMapper.incrementClickCount(shortUrl);
        } catch (Exception e) {
            log.error("点击次数更新失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 解析用户代理信息
     * 
     * @param accessLog 访问日志实体
     * @param userAgent 用户代理字符串
     */
    private void parseUserAgent(UrlAccessLogEntity accessLog, String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return;
        }
        
        // 简单的用户代理解析逻辑
        userAgent = userAgent.toLowerCase();
        
        // 检测设备类型
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            accessLog.setDeviceType("Mobile");
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            accessLog.setDeviceType("Tablet");
        } else {
            accessLog.setDeviceType("Desktop");
        }
        
        // 检测浏览器
        if (userAgent.contains("chrome")) {
            accessLog.setBrowser("Chrome");
        } else if (userAgent.contains("firefox")) {
            accessLog.setBrowser("Firefox");
        } else if (userAgent.contains("safari")) {
            accessLog.setBrowser("Safari");
        } else if (userAgent.contains("edge")) {
            accessLog.setBrowser("Edge");
        } else {
            accessLog.setBrowser("Other");
        }
        
        // 检测操作系统
        if (userAgent.contains("windows")) {
            accessLog.setOperatingSystem("Windows");
        } else if (userAgent.contains("mac")) {
            accessLog.setOperatingSystem("macOS");
        } else if (userAgent.contains("linux")) {
            accessLog.setOperatingSystem("Linux");
        } else if (userAgent.contains("android")) {
            accessLog.setOperatingSystem("Android");
        } else if (userAgent.contains("ios")) {
            accessLog.setOperatingSystem("iOS");
        } else {
            accessLog.setOperatingSystem("Other");
        }
    }
    
    /**
     * 获取原始URL（用于重定向控制器）
     * 
     * @param shortUrl 短链标识符
     * @param request HTTP请求对象
     * @return 原始URL，如果不存在或已过期则返回null
     */
    public String getOriginalUrl(String shortUrl, HttpServletRequest request) {
        return resolveShortUrl(shortUrl, request);
    }
    
    /**
     * 获取原始URL用于预览（不记录访问日志）
     * 
     * @param shortUrl 短链标识符
     * @return 原始URL，如果不存在或已过期则返回null
     */
    public String getOriginalUrlForPreview(String shortUrl) {
        try {
            // 1. 从缓存获取原始URL
            String originalUrl = getOriginalUrlFromCache(shortUrl);
            if (StringUtils.hasText(originalUrl)) {
                return originalUrl;
            }
            
            // 2. 从数据库查询
            QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("short_url", shortUrl);
            Optional<UrlMappingEntity> mappingOpt = Optional.ofNullable(urlMappingMapper.selectOne(wrapper));
            if (mappingOpt.isEmpty()) {
                log.warn("短链不存在: {}", shortUrl);
                return null;
            }
            
            UrlMappingEntity mapping = mappingOpt.get();
            
            // 3. 检查URL状态
            if (!isUrlAccessible(mapping)) {
                log.warn("短链不可访问: {}, 状态: {}", shortUrl, mapping.getStatus());
                return null;
            }
            
            // 4. 更新缓存
            updateCache(shortUrl, mapping.getOriginalUrl());
            
            return mapping.getOriginalUrl();
            
        } catch (Exception e) {
            log.error("短链预览失败: {}", e.getMessage(), e);
            return null;
        }
    }
}