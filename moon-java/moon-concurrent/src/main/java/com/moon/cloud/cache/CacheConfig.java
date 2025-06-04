package com.moon.cloud.cache;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 提供常用的缓存配置模板和工具方法
 * 
 * @author moon-cloud
 */
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    /**
     * 默认配置
     */
    public static class Default {
        public static final long MAX_SIZE = 1000L;
        public static final long EXPIRE_AFTER_WRITE_MINUTES = 30L;
        public static final long EXPIRE_AFTER_ACCESS_MINUTES = 10L;
        public static final int INITIAL_CAPACITY = 16;
        public static final int CONCURRENCY_LEVEL = 4;
    }
    
    /**
     * 小型缓存配置（适用于少量数据）
     */
    public static class Small {
        public static final long MAX_SIZE = 100L;
        public static final long EXPIRE_AFTER_WRITE_MINUTES = 15L;
        public static final long EXPIRE_AFTER_ACCESS_MINUTES = 5L;
        public static final int INITIAL_CAPACITY = 8;
        public static final int CONCURRENCY_LEVEL = 2;
    }
    
    /**
     * 大型缓存配置（适用于大量数据）
     */
    public static class Large {
        public static final long MAX_SIZE = 10000L;
        public static final long EXPIRE_AFTER_WRITE_MINUTES = 60L;
        public static final long EXPIRE_AFTER_ACCESS_MINUTES = 30L;
        public static final int INITIAL_CAPACITY = 64;
        public static final int CONCURRENCY_LEVEL = 8;
    }
    
    /**
     * 会话缓存配置（适用于用户会话）
     */
    public static class Session {
        public static final long MAX_SIZE = 5000L;
        public static final long EXPIRE_AFTER_WRITE_MINUTES = 120L; // 2小时
        public static final long EXPIRE_AFTER_ACCESS_MINUTES = 30L; // 30分钟无访问过期
        public static final int INITIAL_CAPACITY = 32;
        public static final int CONCURRENCY_LEVEL = 4;
    }
    
    /**
     * 数据库查询缓存配置
     */
    public static class Database {
        public static final long MAX_SIZE = 2000L;
        public static final long EXPIRE_AFTER_WRITE_MINUTES = 45L;
        public static final long REFRESH_AFTER_WRITE_MINUTES = 30L;
        public static final int INITIAL_CAPACITY = 32;
        public static final int CONCURRENCY_LEVEL = 6;
    }
    
    /**
     * 创建默认配置的缓存
     */
    public static <K, V> GuavaLocalCache<K, V> createDefaultCache(String cacheName) {
        return GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumSize(Default.MAX_SIZE)
                .expireAfterWrite(Default.EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(Default.EXPIRE_AFTER_ACCESS_MINUTES, TimeUnit.MINUTES)
                .initialCapacity(Default.INITIAL_CAPACITY)
                .concurrencyLevel(Default.CONCURRENCY_LEVEL)
                .recordStats()
                .removalListener(createDefaultRemovalListener(cacheName))
                .build();
    }
    
    /**
     * 创建小型缓存
     */
    public static <K, V> GuavaLocalCache<K, V> createSmallCache(String cacheName) {
        return GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumSize(Small.MAX_SIZE)
                .expireAfterWrite(Small.EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(Small.EXPIRE_AFTER_ACCESS_MINUTES, TimeUnit.MINUTES)
                .initialCapacity(Small.INITIAL_CAPACITY)
                .concurrencyLevel(Small.CONCURRENCY_LEVEL)
                .recordStats()
                .removalListener(createDefaultRemovalListener(cacheName))
                .build();
    }
    
    /**
     * 创建大型缓存
     */
    public static <K, V> GuavaLocalCache<K, V> createLargeCache(String cacheName) {
        return GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumSize(Large.MAX_SIZE)
                .expireAfterWrite(Large.EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(Large.EXPIRE_AFTER_ACCESS_MINUTES, TimeUnit.MINUTES)
                .initialCapacity(Large.INITIAL_CAPACITY)
                .concurrencyLevel(Large.CONCURRENCY_LEVEL)
                .recordStats()
                .removalListener(createDefaultRemovalListener(cacheName))
                .build();
    }
    
    /**
     * 创建会话缓存
     */
    public static <K, V> GuavaLocalCache<K, V> createSessionCache(String cacheName) {
        return GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumSize(Session.MAX_SIZE)
                .expireAfterWrite(Session.EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(Session.EXPIRE_AFTER_ACCESS_MINUTES, TimeUnit.MINUTES)
                .initialCapacity(Session.INITIAL_CAPACITY)
                .concurrencyLevel(Session.CONCURRENCY_LEVEL)
                .recordStats()
                .removalListener(createSessionRemovalListener(cacheName))
                .build();
    }
    
    /**
     * 创建数据库查询缓存
     */
    public static <K, V> GuavaLocalCache<K, V> createDatabaseCache(String cacheName) {
        return GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumSize(Database.MAX_SIZE)
                .expireAfterWrite(Database.EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .refreshAfterWrite(Database.REFRESH_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .initialCapacity(Database.INITIAL_CAPACITY)
                .concurrencyLevel(Database.CONCURRENCY_LEVEL)
                .recordStats()
                .removalListener(createDefaultRemovalListener(cacheName))
                .build();
    }
    
    /**
     * 创建只读缓存（永不过期，只有大小限制）
     */
    public static <K, V> GuavaLocalCache<K, V> createReadOnlyCache(String cacheName, long maxSize) {
        return GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumSize(maxSize)
                .initialCapacity(32)
                .concurrencyLevel(4)
                .recordStats()
                .removalListener(createDefaultRemovalListener(cacheName))
                .build();
    }
    
    /**
     * 创建基于权重的缓存
     */
    public static <K, V> GuavaLocalCache<K, V> createWeightBasedCache(String cacheName, 
                                                                       long maxWeight,
                                                                       com.google.common.cache.Weigher<K, V> weigher) {
        return GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumWeight(maxWeight)
                .weigher(weigher)
                .expireAfterWrite(Default.EXPIRE_AFTER_WRITE_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(Default.EXPIRE_AFTER_ACCESS_MINUTES, TimeUnit.MINUTES)
                .initialCapacity(Default.INITIAL_CAPACITY)
                .concurrencyLevel(Default.CONCURRENCY_LEVEL)
                .recordStats()
                .removalListener(createDefaultRemovalListener(cacheName))
                .build();
    }
    
    /**
     * 创建默认的移除监听器
     */
    public static <K, V> RemovalListener<K, V> createDefaultRemovalListener(String cacheName) {
        return new RemovalListener<K, V>() {
            @Override
            public void onRemoval(RemovalNotification<K, V> notification) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cache [{}] removed entry: key={}, cause={}", 
                            cacheName, notification.getKey(), notification.getCause());
                }
            }
        };
    }
    
    /**
     * 创建会话专用的移除监听器
     */
    public static <K, V> RemovalListener<K, V> createSessionRemovalListener(String cacheName) {
        return new RemovalListener<K, V>() {
            @Override
            public void onRemoval(RemovalNotification<K, V> notification) {
                switch (notification.getCause()) {
                    case EXPIRED:
                        logger.info("Session [{}] expired: key={}", cacheName, notification.getKey());
                        break;
                    case SIZE:
                        logger.warn("Session [{}] evicted due to size limit: key={}", cacheName, notification.getKey());
                        break;
                    case EXPLICIT:
                        logger.debug("Session [{}] explicitly removed: key={}", cacheName, notification.getKey());
                        break;
                    default:
                        logger.debug("Session [{}] removed: key={}, cause={}", 
                                cacheName, notification.getKey(), notification.getCause());
                        break;
                }
            }
        };
    }
    
    /**
     * 字符串权重计算器（按字符串长度计算权重）
     */
    public static final com.google.common.cache.Weigher<String, String> STRING_WEIGHER = 
            (key, value) -> key.length() + value.length();
    
    /**
     * 对象权重计算器（简单估算）
     */
    public static final com.google.common.cache.Weigher<Object, Object> OBJECT_WEIGHER = 
            (key, value) -> {
                int weight = 1; // 基础权重
                if (key instanceof String) {
                    weight += ((String) key).length();
                }
                if (value instanceof String) {
                    weight += ((String) value).length();
                } else if (value instanceof byte[]) {
                    weight += ((byte[]) value).length;
                }
                return weight;
            };
}