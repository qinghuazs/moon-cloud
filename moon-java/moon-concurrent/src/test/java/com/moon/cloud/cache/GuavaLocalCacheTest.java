package com.moon.cloud.cache;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GuavaLocalCache 测试类
 * 
 * @author moon-cloud
 */
class GuavaLocalCacheTest {
    
    private GuavaLocalCache<String, String> cache;
    private CacheManager cacheManager;
    
    @BeforeEach
    void setUp() {
        cacheManager = CacheManager.getInstance();
        cache = GuavaLocalCache.<String, String>builder()
                .cacheName("test-cache")
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .expireAfterAccess(3, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }
    
    @Test
    @DisplayName("测试基本的put和get操作")
    void testBasicPutAndGet() {
        // 测试put和get
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
        
        // 测试getIfPresent
        assertEquals("value1", cache.getIfPresent("key1"));
        assertNull(cache.getIfPresent("nonexistent"));
        
        // 测试size
        assertEquals(1, cache.size());
    }
    
    @Test
    @DisplayName("测试缓存过期功能")
    void testCacheExpiration() throws InterruptedException {
        cache.put("expireKey", "expireValue");
        assertEquals("expireValue", cache.getIfPresent("expireKey"));
        
        // 等待过期
        Thread.sleep(6000); // 超过expireAfterWrite时间
        cache.cleanUp(); // 手动触发清理
        
        assertNull(cache.getIfPresent("expireKey"));
    }
    
    @Test
    @DisplayName("测试访问后过期")
    void testExpireAfterAccess() throws InterruptedException {
        cache.put("accessKey", "accessValue");
        
        // 在过期前访问
        Thread.sleep(2000);
        assertEquals("accessValue", cache.getIfPresent("accessKey"));
        
        // 再次等待，但不访问
        Thread.sleep(4000);
        cache.cleanUp();
        
        assertNull(cache.getIfPresent("accessKey"));
    }
    
    @Test
    @DisplayName("测试缓存大小限制")
    void testMaximumSize() {
        GuavaLocalCache<String, String> smallCache = GuavaLocalCache.<String, String>builder()
                .cacheName("small-cache")
                .maximumSize(2)
                .recordStats()
                .build();
        
        smallCache.put("key1", "value1");
        smallCache.put("key2", "value2");
        smallCache.put("key3", "value3"); // 这应该导致key1被驱逐
        
        assertEquals(2, smallCache.size());
        assertNull(smallCache.getIfPresent("key1")); // 应该被驱逐
        assertEquals("value2", smallCache.getIfPresent("key2"));
        assertEquals("value3", smallCache.getIfPresent("key3"));
    }
    
    @Test
    @DisplayName("测试带加载器的缓存")
    void testCacheWithLoader() {
        AtomicInteger loadCount = new AtomicInteger(0);
        
        GuavaLocalCache<String, String> loaderCache = GuavaLocalCache.<String, String>builder()
                .cacheName("loader-cache")
                .maximumSize(100)
                .recordStats()
                .cacheLoader(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) {
                        loadCount.incrementAndGet();
                        return "loaded-" + key;
                    }
                })
                .build();
        
        // 第一次访问，应该触发加载
        String value1 = loaderCache.get("testKey");
        assertEquals("loaded-testKey", value1);
        assertEquals(1, loadCount.get());
        
        // 第二次访问，应该从缓存获取
        String value2 = loaderCache.get("testKey");
        assertEquals("loaded-testKey", value2);
        assertEquals(1, loadCount.get()); // 加载次数不应该增加
    }
    
    @Test
    @DisplayName("测试缓存统计功能")
    void testCacheStats() {
        cache.put("statsKey", "statsValue");
        
        // 命中
        cache.get("statsKey");
        cache.getIfPresent("statsKey");
        
        // 未命中
        cache.getIfPresent("nonexistent");
        
        CacheStats stats = cache.stats();
        assertEquals(2, stats.hitCount());
        assertEquals(1, stats.missCount());
        assertTrue(stats.hitRate() > 0.5);
    }
    
    @Test
    @DisplayName("测试缓存清理操作")
    void testCacheInvalidation() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        
        assertEquals(3, cache.size());
        
        // 单个删除
        cache.invalidate("key1");
        assertEquals(2, cache.size());
        assertNull(cache.getIfPresent("key1"));
        
        // 全部清空
        cache.invalidateAll();
        assertEquals(0, cache.size());
        assertNull(cache.getIfPresent("key2"));
        assertNull(cache.getIfPresent("key3"));
    }
    
    @Test
    @DisplayName("测试CacheManager功能")
    void testCacheManager() {
        // 注册缓存
        cacheManager.registerCache("test-manager-cache", cache);
        
        // 获取缓存
        GuavaLocalCache<String, String> retrievedCache = cacheManager.getCache("test-manager-cache");
        assertNotNull(retrievedCache);
        assertEquals(cache, retrievedCache);
        
        // 测试缓存操作
        retrievedCache.put("managerKey", "managerValue");
        assertEquals("managerValue", cache.getIfPresent("managerKey"));
        
        // 移除缓存
        cacheManager.removeCache("test-manager-cache");
        assertNull(cacheManager.getCache("test-manager-cache"));
    }
    
    @Test
    @DisplayName("测试预定义配置")
    void testPredefinedConfigs() {
        // 测试默认配置
        GuavaLocalCache<String, String> defaultCache = CacheConfig.createDefaultCache("default-test");
        assertNotNull(defaultCache);
        assertEquals("default-test", defaultCache.getCacheName());
        
        // 测试小型缓存配置
        GuavaLocalCache<String, String> smallCache = CacheConfig.createSmallCache("small-test");
        assertNotNull(smallCache);
        
        // 测试大型缓存配置
        GuavaLocalCache<String, String> largeCache = CacheConfig.createLargeCache("large-test");
        assertNotNull(largeCache);
        
        // 测试会话缓存配置
        GuavaLocalCache<String, String> sessionCache = CacheConfig.createSessionCache("session-test");
        assertNotNull(sessionCache);
        
        // 测试数据库缓存配置
        GuavaLocalCache<String, String> dbCache = CacheConfig.createDatabaseCache("db-test");
        assertNotNull(dbCache);
    }
    
    @Test
    @DisplayName("测试权重基础的缓存")
    void testWeightBasedCache() {
        GuavaLocalCache<String, String> weightCache = CacheConfig.createWeightBasedCache(
                "weight-test", 
                100, 
                CacheConfig.STRING_WEIGHER
        );
        
        weightCache.put("short", "a");
        weightCache.put("medium", "medium-value");
        weightCache.put("very-long-key", "very-long-value-that-should-have-high-weight");
        
        // 权重缓存应该正常工作
        assertEquals("a", weightCache.getIfPresent("short"));
        assertEquals("medium-value", weightCache.getIfPresent("medium"));
    }
    
    @Test
    @DisplayName("测试缓存刷新功能")
    void testCacheRefresh() {
        AtomicInteger loadCount = new AtomicInteger(0);
        
        GuavaLocalCache<String, String> refreshCache = GuavaLocalCache.<String, String>builder()
                .cacheName("refresh-cache")
                .maximumSize(100)
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .recordStats()
                .cacheLoader(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) {
                        int count = loadCount.incrementAndGet();
                        return "loaded-" + key + "-" + count;
                    }
                })
                .build();
        
        // 初始加载
        String value1 = refreshCache.get("refreshKey");
        assertEquals("loaded-refreshKey-1", value1);
        
        // 手动刷新
        refreshCache.refresh("refreshKey");
        
        // 再次获取，应该是新值
        String value2 = refreshCache.get("refreshKey");
        assertEquals("loaded-refreshKey-2", value2);
    }
    
    @Test
    @DisplayName("测试并发访问")
    void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "thread-" + threadId + "-key-" + j;
                    String value = "thread-" + threadId + "-value-" + j;
                    cache.put(key, value);
                    assertEquals(value, cache.getIfPresent(key));
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证缓存大小（可能小于总数，因为有大小限制）
        assertTrue(cache.size() <= 100);
        
        // 打印统计信息
        cache.printStats();
    }
}