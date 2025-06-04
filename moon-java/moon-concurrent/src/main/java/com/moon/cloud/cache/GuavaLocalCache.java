package com.moon.cloud.cache;

import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 基于Guava实现的本地缓存工具类
 * 支持过期时间、淘汰策略、统计信息等功能
 * 
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author moon-cloud
 */
public class GuavaLocalCache<K, V> {
    
    private static final Logger logger = LoggerFactory.getLogger(GuavaLocalCache.class);
    
    private final LoadingCache<K, V> cache;
    private final String cacheName;
    
    /**
     * 私有构造函数，通过Builder创建实例
     */
    private GuavaLocalCache(Builder<K, V> builder) {
        this.cacheName = builder.cacheName;
        
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        
        // 设置最大缓存大小
        if (builder.maximumSize > 0) {
            cacheBuilder.maximumSize(builder.maximumSize);
        }
        
        // 设置最大权重
        if (builder.maximumWeight > 0) {
            cacheBuilder.maximumWeight(builder.maximumWeight);
            if (builder.weigher != null) {
                cacheBuilder.weigher(builder.weigher);
            }
        }
        
        // 设置写入后过期时间
        if (builder.expireAfterWrite > 0) {
            cacheBuilder.expireAfterWrite(builder.expireAfterWrite, builder.expireAfterWriteTimeUnit);
        }
        
        // 设置访问后过期时间
        if (builder.expireAfterAccess > 0) {
            cacheBuilder.expireAfterAccess(builder.expireAfterAccess, builder.expireAfterAccessTimeUnit);
        }
        
        // 设置刷新时间
        if (builder.refreshAfterWrite > 0) {
            cacheBuilder.refreshAfterWrite(builder.refreshAfterWrite, builder.refreshAfterWriteTimeUnit);
        }
        
        // 设置并发级别
        if (builder.concurrencyLevel > 0) {
            cacheBuilder.concurrencyLevel(builder.concurrencyLevel);
        }
        
        // 设置初始容量
        if (builder.initialCapacity > 0) {
            cacheBuilder.initialCapacity(builder.initialCapacity);
        }
        
        // 启用统计
        if (builder.recordStats) {
            cacheBuilder.recordStats();
        }
        
        // 设置移除监听器
        if (builder.removalListener != null) {
            cacheBuilder.removalListener(builder.removalListener);
        }
        
        // 构建缓存
        if (builder.cacheLoader != null) {
            this.cache = cacheBuilder.build(builder.cacheLoader);
        } else {
            // 如果没有CacheLoader，创建一个默认的
            this.cache = cacheBuilder.build(new CacheLoader<K, V>() {
                @Override
                public V load(K key) throws Exception {
                    throw new UnsupportedOperationException("CacheLoader not provided");
                }
            });
        }
        
        logger.info("GuavaLocalCache [{}] initialized successfully", cacheName);
    }
    
    /**
     * 获取缓存值
     */
    public V get(K key) {
        try {
            return cache.get(key);
        } catch (ExecutionException e) {
            logger.error("Error getting value for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 获取缓存值，如果不存在则使用提供的函数计算
     */
    public V get(K key, Callable<? extends V> valueLoader) {
        try {
            return cache.get(key, valueLoader);
        } catch (ExecutionException e) {
            logger.error("Error getting value for key: {} with loader", key, e);
            return null;
        }
    }
    
    /**
     * 获取缓存值，如果不存在则使用提供的函数计算
     */
    public V get(K key, Function<K, V> mappingFunction) {
        try {
            return cache.get(key, () -> mappingFunction.apply(key));
        } catch (ExecutionException e) {
            logger.error("Error getting value for key: {} with mapping function", key, e);
            return null;
        }
    }
    
    /**
     * 获取缓存值，如果不存在返回null
     */
    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }
    
    /**
     * 批量获取缓存值
     */
    public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
        return cache.getAllPresent(keys);
    }
    
    /**
     * 放入缓存
     */
    public void put(K key, V value) {
        cache.put(key, value);
    }
    
    /**
     * 批量放入缓存
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
    }
    
    /**
     * 移除缓存项
     */
    public void invalidate(K key) {
        cache.invalidate(key);
    }
    
    /**
     * 批量移除缓存项
     */
    public void invalidateAll(Iterable<? extends K> keys) {
        cache.invalidateAll(keys);
    }
    
    /**
     * 清空所有缓存
     */
    public void invalidateAll() {
        cache.invalidateAll();
        logger.info("Cache [{}] cleared", cacheName);
    }
    
    /**
     * 获取缓存大小
     */
    public long size() {
        return cache.size();
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats stats() {
        return cache.stats();
    }
    
    /**
     * 手动触发缓存清理
     */
    public void cleanUp() {
        cache.cleanUp();
    }
    
    /**
     * 刷新缓存项
     */
    public void refresh(K key) {
        cache.refresh(key);
    }
    
    /**
     * 获取缓存的所有键值对
     */
    public Map<K, V> asMap() {
        return cache.asMap();
    }
    
    /**
     * 打印缓存统计信息
     */
    public void printStats() {
        CacheStats stats = cache.stats();
        logger.info("Cache [{}] Stats: hitCount={}, missCount={}, hitRate={:.2f}%, evictionCount={}, loadCount={}, averageLoadTime={:.2f}ms",
                cacheName,
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate() * 100,
                stats.evictionCount(),
                stats.loadCount(),
                stats.averageLoadPenalty() / 1_000_000.0);
    }
    
    /**
     * 获取缓存名称
     */
    public String getCacheName() {
        return cacheName;
    }
    
    /**
     * 创建Builder实例
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }
    
    /**
     * Builder类用于构建GuavaLocalCache实例
     */
    public static class Builder<K, V> {
        private String cacheName = "default";
        private long maximumSize = -1;
        private long maximumWeight = -1;
        private Weigher<? super K, ? super V> weigher;
        private long expireAfterWrite = -1;
        private TimeUnit expireAfterWriteTimeUnit = TimeUnit.MINUTES;
        private long expireAfterAccess = -1;
        private TimeUnit expireAfterAccessTimeUnit = TimeUnit.MINUTES;
        private long refreshAfterWrite = -1;
        private TimeUnit refreshAfterWriteTimeUnit = TimeUnit.MINUTES;
        private int concurrencyLevel = -1;
        private int initialCapacity = -1;
        private boolean recordStats = false;
        private RemovalListener<? super K, ? super V> removalListener;
        private CacheLoader<? super K, V> cacheLoader;
        
        /**
         * 设置缓存名称
         */
        public Builder<K, V> cacheName(String cacheName) {
            this.cacheName = cacheName;
            return this;
        }
        
        /**
         * 设置最大缓存条目数
         */
        public Builder<K, V> maximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }
        
        /**
         * 设置最大权重
         */
        public Builder<K, V> maximumWeight(long maximumWeight) {
            this.maximumWeight = maximumWeight;
            return this;
        }
        
        /**
         * 设置权重计算器
         */
        public Builder<K, V> weigher(Weigher<? super K, ? super V> weigher) {
            this.weigher = weigher;
            return this;
        }
        
        /**
         * 设置写入后过期时间
         */
        public Builder<K, V> expireAfterWrite(long duration, TimeUnit unit) {
            this.expireAfterWrite = duration;
            this.expireAfterWriteTimeUnit = unit;
            return this;
        }
        
        /**
         * 设置访问后过期时间
         */
        public Builder<K, V> expireAfterAccess(long duration, TimeUnit unit) {
            this.expireAfterAccess = duration;
            this.expireAfterAccessTimeUnit = unit;
            return this;
        }
        
        /**
         * 设置刷新时间
         */
        public Builder<K, V> refreshAfterWrite(long duration, TimeUnit unit) {
            this.refreshAfterWrite = duration;
            this.refreshAfterWriteTimeUnit = unit;
            return this;
        }
        
        /**
         * 设置并发级别
         */
        public Builder<K, V> concurrencyLevel(int concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }
        
        /**
         * 设置初始容量
         */
        public Builder<K, V> initialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }
        
        /**
         * 启用统计功能
         */
        public Builder<K, V> recordStats() {
            this.recordStats = true;
            return this;
        }
        
        /**
         * 设置移除监听器
         */
        public Builder<K, V> removalListener(RemovalListener<? super K, ? super V> removalListener) {
            this.removalListener = removalListener;
            return this;
        }
        
        /**
         * 设置缓存加载器
         */
        public Builder<K, V> cacheLoader(CacheLoader<? super K, V> cacheLoader) {
            this.cacheLoader = cacheLoader;
            return this;
        }
        
        /**
         * 构建GuavaLocalCache实例
         */
        public GuavaLocalCache<K, V> build() {
            return new GuavaLocalCache<>(this);
        }
    }
}