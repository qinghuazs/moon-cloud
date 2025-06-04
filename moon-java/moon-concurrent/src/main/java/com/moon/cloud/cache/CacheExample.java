package com.moon.cloud.cache;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存使用示例
 * 展示如何在实际项目中使用GuavaLocalCache
 * 
 * @author moon-cloud
 */
public class CacheExample {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheExample.class);
    
    public static void main(String[] args) {
        CacheExample example = new CacheExample();
        
        try {
            // 基本使用示例
            example.basicUsageExample();
            
            // 用户会话缓存示例
            example.userSessionCacheExample();
            
            // 数据库查询缓存示例
            example.databaseQueryCacheExample();
            
            // 文件内容缓存示例
            example.fileContentCacheExample();
            
            // 缓存管理器示例
            example.cacheManagerExample();
            
            // 权重基础缓存示例
            example.weightBasedCacheExample();
            
        } catch (Exception e) {
            logger.error("Example execution failed", e);
        }
    }
    
    /**
     * 基本使用示例
     */
    public void basicUsageExample() {
        logger.info("=== 基本使用示例 ===");
        
        // 创建一个简单的字符串缓存
        GuavaLocalCache<String, String> cache = GuavaLocalCache.<String, String>builder()
                .cacheName("basic-cache")
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
        
        // 基本操作
        cache.put("user:123", "张三");
        cache.put("user:456", "李四");
        
        String userName = cache.getIfPresent("user:123");
        logger.info("获取用户名: {}", userName);
        
        // 使用函数式接口获取值
        String userInfo = cache.get("user:789", key -> {
            // 模拟从数据库加载用户信息
            logger.info("从数据库加载用户: {}", key);
            return "王五";
        });
        logger.info("加载的用户信息: {}", userInfo);
        
        // 打印统计信息
        cache.printStats();
    }
    
    /**
     * 用户会话缓存示例
     */
    public void userSessionCacheExample() {
        logger.info("=== 用户会话缓存示例 ===");
        
        // 创建会话缓存
        GuavaLocalCache<String, UserSession> sessionCache = CacheConfig.createSessionCache("user-session");
        
        // 模拟用户登录
        String sessionId = "session_" + System.currentTimeMillis();
        UserSession session = new UserSession("user123", "张三", System.currentTimeMillis());
        sessionCache.put(sessionId, session);
        
        // 获取会话信息
        UserSession retrievedSession = sessionCache.getIfPresent(sessionId);
        if (retrievedSession != null) {
            logger.info("会话信息: userId={}, userName={}, loginTime={}", 
                    retrievedSession.userId, retrievedSession.userName, retrievedSession.loginTime);
        }
        
        // 模拟会话过期检查
        logger.info("当前会话数量: {}", sessionCache.size());
    }
    
    /**
     * 数据库查询缓存示例
     */
    public void databaseQueryCacheExample() {
        logger.info("=== 数据库查询缓存示例 ===");
        
        // 创建数据库查询缓存，带自动加载功能
        GuavaLocalCache<String, String> dbCache = GuavaLocalCache.<String, String>builder()
                .cacheName("database-query")
                .maximumSize(2000)
                .expireAfterWrite(45, TimeUnit.MINUTES)
                .refreshAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .cacheLoader(new CacheLoader<String, String>() {
                    @Override
                    public String load(String sql) throws Exception {
                        // 模拟数据库查询
                        logger.info("执行数据库查询: {}", sql);
                        Thread.sleep(100); // 模拟查询耗时
                        return "查询结果_" + System.currentTimeMillis();
                    }
                })
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> notification) {
                        logger.info("数据库查询缓存移除: key={}, cause={}", 
                                notification.getKey(), notification.getCause());
                    }
                })
                .build();
        
        // 执行查询（第一次会触发数据库查询）
        String result1 = dbCache.get("SELECT * FROM users WHERE id = 1");
        logger.info("查询结果1: {}", result1);
        
        // 再次执行相同查询（从缓存获取）
        String result2 = dbCache.get("SELECT * FROM users WHERE id = 1");
        logger.info("查询结果2: {}", result2);
        
        // 手动刷新缓存
        dbCache.refresh("SELECT * FROM users WHERE id = 1");
        
        dbCache.printStats();
    }
    
    /**
     * 文件内容缓存示例
     */
    public void fileContentCacheExample() {
        logger.info("=== 文件内容缓存示例 ===");
        
        // 创建文件内容缓存
        GuavaLocalCache<String, FileContent> fileCache = GuavaLocalCache.<String, FileContent>builder()
                .cacheName("file-content")
                .maximumWeight(10 * 1024 * 1024) // 10MB
                .weigher((path, content) -> content.size)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .recordStats()
                .cacheLoader(new CacheLoader<String, FileContent>() {
                    @Override
                    public FileContent load(String filePath) throws Exception {
                        // 模拟文件读取
                        logger.info("读取文件: {}", filePath);
                        Thread.sleep(50); // 模拟IO耗时
                        String content = "文件内容_" + filePath + "_" + System.currentTimeMillis();
                        return new FileContent(content, content.length());
                    }
                })
                .build();
        
        // 读取文件
        FileContent content1 = fileCache.get("/path/to/config.properties");
        logger.info("文件内容: {}, 大小: {}", content1.content, content1.size);
        
        // 再次读取（从缓存获取）
        FileContent content2 = fileCache.get("/path/to/config.properties");
        logger.info("缓存文件内容: {}", content2.content);
        
        fileCache.printStats();
    }
    
    /**
     * 缓存管理器示例
     */
    public void cacheManagerExample() {
        logger.info("=== 缓存管理器示例 ===");
        
        CacheManager manager = CacheManager.getInstance();
        
        // 创建多个缓存
        GuavaLocalCache<String, String> userCache = manager.createStringCache("users", 1000, 60);
        GuavaLocalCache<String, String> productCache = manager.createStringCache("products", 500, 30);
        
        // 使用缓存
        userCache.put("user1", "张三");
        productCache.put("product1", "iPhone");
        
        // 获取缓存
        GuavaLocalCache<String, String> retrievedUserCache = manager.getCache("users");
        logger.info("从管理器获取用户: {}", retrievedUserCache.getIfPresent("user1"));
        
        // 打印所有缓存统计
        manager.printAllCacheStats();
        
        // 清理
        manager.clearAllCaches();
    }
    
    /**
     * 权重基础缓存示例
     */
    public void weightBasedCacheExample() {
        logger.info("=== 权重基础缓存示例 ===");
        
        // 创建基于权重的缓存
        GuavaLocalCache<String, String> weightCache = CacheConfig.createWeightBasedCache(
                "weight-cache", 
                1000, // 最大权重
                CacheConfig.STRING_WEIGHER
        );
        
        // 添加不同大小的数据
        weightCache.put("small", "a");
        weightCache.put("medium", "这是一个中等长度的字符串");
        weightCache.put("large", "这是一个非常非常非常长的字符串，用来测试权重计算功能，看看缓存是否能正确处理不同大小的数据项");
        
        logger.info("权重缓存大小: {}", weightCache.size());
        weightCache.printStats();
    }
    
    /**
     * 用户会话类
     */
    static class UserSession {
        final String userId;
        final String userName;
        final long loginTime;
        
        UserSession(String userId, String userName, long loginTime) {
            this.userId = userId;
            this.userName = userName;
            this.loginTime = loginTime;
        }
    }
    
    /**
     * 文件内容类
     */
    static class FileContent {
        final String content;
        final int size;
        
        FileContent(String content, int size) {
            this.content = content;
            this.size = size;
        }
    }
}