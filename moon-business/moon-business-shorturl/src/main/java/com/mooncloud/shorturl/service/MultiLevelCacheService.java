package com.mooncloud.shorturl.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.mapper.UrlMappingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存服务
 * L1: 本地缓存 (Caffeine)
 * L2: Redis分布式缓存
 * L3: 数据库
 *
 * @author mooncloud
 */
@Service
@Slf4j
public class MultiLevelCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UrlMappingMapper urlMappingMapper;

    private final Cache<String, String> localCache;
    private final Cache<String, String> negativeCache; // 负缓存

    private static final String SHORT_URL_CACHE_PREFIX = "shorturl:";
    private static final String NEGATIVE_CACHE_VALUE = "NOT_FOUND";

    public MultiLevelCacheService() {
        this.localCache = Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();

        this.negativeCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
    }

    /**
     * 获取原始URL
     *
     * @param shortCode 短码
     * @return 原始URL，不存在返回null
     */
    public String getOriginalUrl(String shortCode) {
        // L1: 本地缓存
        String cached = localCache.getIfPresent(shortCode);
        if (cached != null) {
            log.debug("L1 cache hit: {}", shortCode);
            return cached;
        }

        // 检查负缓存
        if (negativeCache.getIfPresent(shortCode) != null) {
            log.debug("Negative cache hit: {}", shortCode);
            return null;
        }

        // L2: Redis缓存
        String redisKey = SHORT_URL_CACHE_PREFIX + shortCode;
        Object redisValue = redisTemplate.opsForValue().get(redisKey);

        if (redisValue != null) {
            if (redisValue instanceof String) {
                String originalUrl = (String) redisValue;
                if (!NEGATIVE_CACHE_VALUE.equals(originalUrl)) {
                    log.debug("L2 cache hit: {}", shortCode);
                    localCache.put(shortCode, originalUrl);
                    return originalUrl;
                } else {
                    negativeCache.put(shortCode, NEGATIVE_CACHE_VALUE);
                    return null;
                }
            }
        }

        // L3: 数据库查询
        QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("short_url", shortCode);
        Optional<UrlMappingEntity> shortUrl = Optional.ofNullable(urlMappingMapper.selectOne(wrapper));

        if (shortUrl.isPresent() && isUrlAccessible(shortUrl.get())) {
            String originalUrl = shortUrl.get().getOriginalUrl();
            log.debug("L3 database hit: {}", shortCode);

            // 更新各级缓存
            localCache.put(shortCode, originalUrl);
            redisTemplate.opsForValue().set(redisKey, originalUrl, 24, TimeUnit.HOURS);

            return originalUrl;
        } else {
            // 缓存空结果
            negativeCache.put(shortCode, NEGATIVE_CACHE_VALUE);
            redisTemplate.opsForValue().set(redisKey, NEGATIVE_CACHE_VALUE, 5, TimeUnit.MINUTES);

            return null;
        }
    }

    /**
     * 缓存原始URL
     *
     * @param shortCode 短码
     * @param originalUrl 原始URL
     */
    public void cacheOriginalUrl(String shortCode, String originalUrl) {
        if (!StringUtils.hasText(shortCode) || !StringUtils.hasText(originalUrl)) {
            return;
        }

        try {
            // 更新本地缓存
            localCache.put(shortCode, originalUrl);

            // 更新Redis缓存
            String redisKey = SHORT_URL_CACHE_PREFIX + shortCode;
            redisTemplate.opsForValue().set(redisKey, originalUrl, 24, TimeUnit.HOURS);

            log.debug("Cached: {} -> {}", shortCode, originalUrl);
        } catch (Exception e) {
            log.error("Failed to cache: {} -> {}", shortCode, originalUrl, e);
        }
    }

    /**
     * 清除缓存
     *
     * @param shortCode 短码
     */
    public void evictCache(String shortCode) {
        if (!StringUtils.hasText(shortCode)) {
            return;
        }

        try {
            // 清除本地缓存
            localCache.invalidate(shortCode);
            negativeCache.invalidate(shortCode);

            // 清除Redis缓存
            String redisKey = SHORT_URL_CACHE_PREFIX + shortCode;
            redisTemplate.delete(redisKey);

            log.debug("Evicted cache: {}", shortCode);
        } catch (Exception e) {
            log.error("Failed to evict cache: {}", shortCode, e);
        }
    }

    /**
     * 检查URL是否可访问
     */
    private boolean isUrlAccessible(UrlMappingEntity mapping) {
        // 检查是否过期
        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().before(new Date())) {
            return false;
        }

        // 检查状态
        return mapping.getStatus().name().equals("ACTIVE");
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format("Local cache stats: %s", localCache.stats().toString());
    }
}