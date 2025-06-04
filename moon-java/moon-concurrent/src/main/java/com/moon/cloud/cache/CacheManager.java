package com.moon.cloud.cache;

import com.google.common.cache.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存管理器
 * 用于管理多个缓存实例，提供统一的缓存操作接口
 * 
 * @author moon-cloud
 */
public class CacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private final Map<String, GuavaLocalCache<?, ?>> caches = new ConcurrentHashMap<>();
    
    private static final CacheManager INSTANCE = new CacheManager();
    
    private CacheManager() {
        logger.info("CacheManager initialized");
    }
    
    /**
     * 获取单例实例
     */
    public static CacheManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册缓存
     */
    public <K, V> void registerCache(String cacheName, GuavaLocalCache<K, V> cache) {
        caches.put(cacheName, cache);
        logger.info("Cache [{}] registered", cacheName);
    }
    
    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <K, V> GuavaLocalCache<K, V> getCache(String cacheName) {
        return (GuavaLocalCache<K, V>) caches.get(cacheName);
    }
    
    /**
     * 移除缓存
     */
    public void removeCache(String cacheName) {
        GuavaLocalCache<?, ?> cache = caches.remove(cacheName);
        if (cache != null) {
            cache.invalidateAll();
            logger.info("Cache [{}] removed", cacheName);
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAllCaches() {
        caches.values().forEach(GuavaLocalCache::invalidateAll);
        logger.info("All caches cleared");
    }
    
    /**
     * 获取所有缓存名称
     */
    public java.util.Set<String> getCacheNames() {
        return caches.keySet();
    }
    
    /**
     * 获取缓存数量
     */
    public int getCacheCount() {
        return caches.size();
    }
    
    /**
     * 打印所有缓存的统计信息
     */
    public void printAllCacheStats() {
        logger.info("=== Cache Statistics ===");
        caches.forEach((name, cache) -> {
            CacheStats stats = cache.stats();
            logger.info("Cache [{}]: size={}, hitRate={:.2f}%, evictionCount={}",
                    name, cache.size(), stats.hitRate() * 100, stats.evictionCount());
        });
    }
    
    /**
     * 创建一个简单的字符串缓存
     */
    public GuavaLocalCache<String, String> createStringCache(String cacheName, 
                                                              long maxSize, 
                                                              long expireAfterWriteMinutes) {
        GuavaLocalCache<String, String> cache = GuavaLocalCache.<String, String>builder()
                .cacheName(cacheName)
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();
        
        registerCache(cacheName, cache);
        return cache;
    }
    
    /**
     * 创建一个带加载器的缓存
     */
    public <K, V> GuavaLocalCache<K, V> createCacheWithLoader(String cacheName,
                                                               long maxSize,
                                                               long expireAfterWriteMinutes,
                                                               Function<K, V> loader) {
        GuavaLocalCache<K, V> cache = GuavaLocalCache.<K, V>builder()
                .cacheName(cacheName)
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                .recordStats()
                .cacheLoader(new com.google.common.cache.CacheLoader<K, V>() {
                    @Override
                    public V load(K key) {
                        return loader.apply(key);
                    }
                })
                .build();
        
        registerCache(cacheName, cache);
        return cache;
    }
    
    /**
     * 关闭缓存管理器
     */
    public void shutdown() {
        clearAllCaches();
        caches.clear();
        logger.info("CacheManager shutdown");
    }
}