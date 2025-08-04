package com.mooncloud.shorturl.service;

import com.google.common.hash.BloomFilter;
import com.mooncloud.shorturl.dto.ShortUrlResult;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.enums.UrlStatus;
import com.mooncloud.shorturl.repository.UrlMappingRepository;
import com.mooncloud.shorturl.util.Base62Encoder;
import com.mooncloud.shorturl.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 短链生成服务
 * 
 * @author mooncloud
 */
@Service
@Slf4j
public class ShortUrlGeneratorService {
    
    @Autowired
    private Base62Encoder base62Encoder;
    
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private BloomFilter<String> urlBloomFilter;
    
    private static final String URL_HASH_PREFIX = "url_hash:";
    private static final String SHORT_URL_PREFIX = "short_url:";
    private static final int DEFAULT_SHORT_URL_LENGTH = 6;
    private static final int MAX_RETRY_COUNT = 3;
    
    /**
     * 生成短链
     * 
     * @param originalUrl 原始URL
     * @param customShortUrl 自定义短链（可为空）
     * @param userId 用户ID（可为空，表示游客用户）
     * @return 短链生成结果
     */
    @Transactional
    public ShortUrlResult generateShortUrl(String originalUrl, String customShortUrl, Long userId) {
        try {
            // 1. URL标准化
            String normalizedUrl = normalizeUrl(originalUrl);
            
            // 2. 检查是否已存在
            String urlHash = DigestUtils.md5Hex(normalizedUrl);
            Optional<UrlMappingEntity> existingMapping = checkExistingUrl(urlHash);
            if (existingMapping.isPresent()) {
                return ShortUrlResult.success(existingMapping.get().getShortUrl(), false);
            }
            
            // 3. 生成短链
            String shortUrl;
            if (StringUtils.hasText(customShortUrl)) {
                // 自定义短链
                shortUrl = generateCustomShortUrl(customShortUrl);
            } else {
                // 系统生成短链
                shortUrl = generateSystemShortUrl();
            }
            
            // 4. 保存映射关系
            UrlMappingEntity mapping = new UrlMappingEntity();
            mapping.setShortUrl(shortUrl);
            mapping.setOriginalUrl(normalizedUrl);
            mapping.setUrlHash(urlHash);
            mapping.setUserId(userId);
            mapping.setCreatedAt(new Date());
            mapping.setExpiresAt(calculateExpiryDate());
            mapping.setClickCount(0L);
            mapping.setStatus(UrlStatus.ACTIVE);
            mapping.setIsCustom(StringUtils.hasText(customShortUrl));
            
            urlMappingRepository.save(mapping);
            
            // 5. 更新缓存和布隆过滤器
            updateCache(shortUrl, normalizedUrl);
            urlBloomFilter.put(normalizedUrl);
            
            log.info("短链生成成功: {} -> {}", normalizedUrl, shortUrl);
            return ShortUrlResult.success(shortUrl, true);
            
        } catch (Exception e) {
            log.error("短链生成失败: {}", e.getMessage(), e);
            return ShortUrlResult.failure("短链生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查URL是否已存在
     * 
     * @param urlHash URL哈希值
     * @return 存在的URL映射实体
     */
    private Optional<UrlMappingEntity> checkExistingUrl(String urlHash) {
        // 1. 布隆过滤器快速检查
        if (!urlBloomFilter.mightContain(urlHash)) {
            return Optional.empty();
        }
        
        // 2. 缓存检查
        String cacheKey = URL_HASH_PREFIX + urlHash;
        String cachedShortUrl = (String) redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cachedShortUrl)) {
            return urlMappingRepository.findByShortUrl(cachedShortUrl);
        }
        
        // 3. 数据库查询
        return urlMappingRepository.findByUrlHash(urlHash);
    }
    
    /**
     * 生成系统短链
     * 
     * @return 短链标识符
     * @throws RuntimeException 如果超过最大重试次数
     */
    private String generateSystemShortUrl() {
        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {
            try {
                // 使用Snowflake生成唯一ID
                long id = snowflakeIdGenerator.nextId();
                
                // Base62编码
                String shortUrl = base62Encoder.encodeWithPadding(id, DEFAULT_SHORT_URL_LENGTH);
                
                // 检查冲突
                if (!urlMappingRepository.existsByShortUrl(shortUrl)) {
                    return shortUrl;
                }
                
                log.warn("短链冲突，重试: {}", shortUrl);
                
            } catch (Exception e) {
                log.error("生成短链异常，重试: {}", e.getMessage());
            }
        }
        
        throw new RuntimeException("短链生成失败，超过最大重试次数");
    }
    
    /**
     * 生成自定义短链
     * 
     * @param customShortUrl 自定义短链
     * @return 短链标识符
     * @throws IllegalArgumentException 如果自定义短链不合法
     */
    private String generateCustomShortUrl(String customShortUrl) {
        // 1. 验证自定义短链格式
        if (!isValidCustomShortUrl(customShortUrl)) {
            throw new IllegalArgumentException("自定义短链格式不正确");
        }
        
        // 2. 检查是否已被使用
        if (urlMappingRepository.existsByShortUrl(customShortUrl)) {
            throw new IllegalArgumentException("自定义短链已被使用");
        }
        
        // 3. 检查是否为保留字
        if (isReservedShortUrl(customShortUrl)) {
            throw new IllegalArgumentException("自定义短链为系统保留字");
        }
        
        return customShortUrl;
    }
    
    /**
     * 验证自定义短链格式
     * 
     * @param shortUrl 短链
     * @return 是否有效
     */
    private boolean isValidCustomShortUrl(String shortUrl) {
        if (!StringUtils.hasText(shortUrl)) {
            return false;
        }
        
        // 长度限制
        if (shortUrl.length() < 3 || shortUrl.length() > 20) {
            return false;
        }
        
        // 字符限制：只允许字母、数字、下划线、连字符
        return shortUrl.matches("^[a-zA-Z0-9_-]+$");
    }
    
    /**
     * 检查是否为保留字
     * 
     * @param shortUrl 短链
     * @return 是否为保留字
     */
    private boolean isReservedShortUrl(String shortUrl) {
        Set<String> reservedWords = Set.of(
            "api", "admin", "www", "app", "mobile", "web",
            "help", "about", "contact", "privacy", "terms",
            "login", "register", "dashboard", "profile"
        );
        
        return reservedWords.contains(shortUrl.toLowerCase());
    }
    
    /**
     * URL标准化
     * 
     * @param url 原始URL
     * @return 标准化后的URL
     * @throws IllegalArgumentException 如果URL格式错误
     */
    private String normalizeUrl(String url) {
        try {
            URL urlObj = new URL(url);
            
            // 移除fragment
            String normalizedUrl = urlObj.getProtocol() + "://" + 
                                 urlObj.getHost() + 
                                 (urlObj.getPort() != -1 ? ":" + urlObj.getPort() : "") +
                                 urlObj.getPath() +
                                 (StringUtils.hasText(urlObj.getQuery()) ? "?" + urlObj.getQuery() : "");
            
            // 移除末尾斜杠
            if (normalizedUrl.endsWith("/") && normalizedUrl.length() > 1) {
                normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
            }
            
            return normalizedUrl;
            
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL格式错误: " + url);
        }
    }
    
    /**
     * 计算过期时间
     * 
     * @return 过期时间
     */
    private Date calculateExpiryDate() {
        // 默认1年过期
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        return calendar.getTime();
    }
    
    /**
     * 更新缓存
     * 
     * @param shortUrl 短链
     * @param originalUrl 原始URL
     */
    private void updateCache(String shortUrl, String originalUrl) {
        try {
            // 短链 -> 原始URL
            String shortUrlKey = SHORT_URL_PREFIX + shortUrl;
            redisTemplate.opsForValue().set(shortUrlKey, originalUrl, 24, TimeUnit.HOURS);
            
            // URL哈希 -> 短链
            String urlHash = DigestUtils.md5Hex(originalUrl);
            String urlHashKey = URL_HASH_PREFIX + urlHash;
            redisTemplate.opsForValue().set(urlHashKey, shortUrl, 24, TimeUnit.HOURS);
            
        } catch (Exception e) {
            log.error("缓存更新失败: {}", e.getMessage());
        }
    }
}