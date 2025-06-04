# Guava本地缓存工具类

基于Google Guava实现的高性能本地缓存工具类，支持多种过期策略、淘汰机制、统计功能等。

## 功能特性

### 核心功能
- ✅ **多种过期策略**：支持写入后过期、访问后过期、定时刷新
- ✅ **灵活的淘汰机制**：基于大小限制、权重限制的LRU淘汰
- ✅ **统计功能**：命中率、加载时间、驱逐次数等详细统计
- ✅ **并发安全**：线程安全的缓存操作
- ✅ **自动加载**：支持CacheLoader自动加载缺失数据
- ✅ **监听器**：缓存项移除事件监听
- ✅ **批量操作**：支持批量读取、写入、删除

### 预定义配置
- 🔧 **默认配置**：适用于一般场景的平衡配置
- 🔧 **小型缓存**：适用于少量数据的轻量级配置
- 🔧 **大型缓存**：适用于大量数据的高容量配置
- 🔧 **会话缓存**：专为用户会话设计的配置
- 🔧 **数据库缓存**：适用于数据库查询结果缓存

## 快速开始

### 1. 基本使用

```java
// 创建简单缓存
GuavaLocalCache<String, String> cache = GuavaLocalCache.<String, String>builder()
    .cacheName("my-cache")
    .maximumSize(1000)
    .expireAfterWrite(30, TimeUnit.MINUTES)
    .recordStats()
    .build();

// 基本操作
cache.put("key1", "value1");
String value = cache.getIfPresent("key1");
cache.invalidate("key1");
```

### 2. 使用预定义配置

```java
// 创建会话缓存
GuavaLocalCache<String, UserSession> sessionCache = 
    CacheConfig.createSessionCache("user-sessions");

// 创建数据库查询缓存
GuavaLocalCache<String, QueryResult> dbCache = 
    CacheConfig.createDatabaseCache("db-queries");
```

### 3. 带自动加载的缓存

```java
GuavaLocalCache<String, User> userCache = GuavaLocalCache.<String, User>builder()
    .cacheName("users")
    .maximumSize(1000)
    .expireAfterWrite(1, TimeUnit.HOURS)
    .cacheLoader(new CacheLoader<String, User>() {
        @Override
        public User load(String userId) throws Exception {
            return userService.findById(userId);
        }
    })
    .build();

// 自动加载（如果缓存中不存在会自动调用loader）
User user = userCache.get("user123");
```

### 4. 使用缓存管理器

```java
CacheManager manager = CacheManager.getInstance();

// 创建并注册缓存
GuavaLocalCache<String, String> cache = manager.createStringCache(
    "my-cache", 1000, 30);

// 获取缓存
GuavaLocalCache<String, String> retrievedCache = manager.getCache("my-cache");

// 打印所有缓存统计
manager.printAllCacheStats();
```

## 配置选项

### 大小限制

```java
// 基于条目数量限制
.maximumSize(1000)

// 基于权重限制
.maximumWeight(10 * 1024 * 1024) // 10MB
.weigher((key, value) -> key.length() + value.length())
```

### 过期策略

```java
// 写入后过期
.expireAfterWrite(30, TimeUnit.MINUTES)

// 访问后过期
.expireAfterAccess(10, TimeUnit.MINUTES)

// 定时刷新（需要CacheLoader）
.refreshAfterWrite(5, TimeUnit.MINUTES)
```

### 性能调优

```java
// 初始容量
.initialCapacity(64)

// 并发级别
.concurrencyLevel(8)

// 启用统计
.recordStats()
```

### 监听器

```java
.removalListener(new RemovalListener<String, String>() {
    @Override
    public void onRemoval(RemovalNotification<String, String> notification) {
        logger.info("缓存项被移除: key={}, cause={}", 
            notification.getKey(), notification.getCause());
    }
})
```

## 使用场景

### 1. 用户会话管理

```java
GuavaLocalCache<String, UserSession> sessionCache = 
    CacheConfig.createSessionCache("user-sessions");

// 用户登录时存储会话
sessionCache.put(sessionId, new UserSession(userId, loginTime));

// 验证会话
UserSession session = sessionCache.getIfPresent(sessionId);
if (session != null && !session.isExpired()) {
    // 会话有效
}
```

### 2. 数据库查询缓存

```java
GuavaLocalCache<String, List<Product>> productCache = 
    GuavaLocalCache.<String, List<Product>>builder()
    .cacheName("product-queries")
    .maximumSize(500)
    .expireAfterWrite(15, TimeUnit.MINUTES)
    .cacheLoader(sql -> productDao.query(sql))
    .build();

// 查询产品（自动缓存）
List<Product> products = productCache.get("SELECT * FROM products WHERE category = 'electronics'");
```

### 3. 配置文件缓存

```java
GuavaLocalCache<String, Properties> configCache = 
    GuavaLocalCache.<String, Properties>builder()
    .cacheName("config-files")
    .maximumSize(100)
    .expireAfterAccess(1, TimeUnit.HOURS)
    .cacheLoader(this::loadConfigFile)
    .build();

// 获取配置
Properties config = configCache.get("application.properties");
```

### 4. API响应缓存

```java
GuavaLocalCache<String, ApiResponse> apiCache = 
    GuavaLocalCache.<String, ApiResponse>builder()
    .cacheName("api-responses")
    .maximumSize(1000)
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .weigher((url, response) -> response.getContentLength())
    .build();

// 缓存API响应
apiCache.put(apiUrl, response);
```

## 最佳实践

### 1. 选择合适的过期策略

- **expireAfterWrite**：适用于数据有明确生命周期的场景
- **expireAfterAccess**：适用于热点数据缓存
- **refreshAfterWrite**：适用于可以异步刷新的场景

### 2. 合理设置缓存大小

```java
// 根据内存情况设置合理的大小限制
.maximumSize(Runtime.getRuntime().maxMemory() / 1024 / 1024 / 10) // 最大堆内存的1/10
```

### 3. 启用统计监控

```java
// 生产环境建议启用统计
.recordStats()

// 定期打印统计信息
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    cache.printStats();
}, 0, 5, TimeUnit.MINUTES);
```

### 4. 处理缓存异常

```java
// 使用getIfPresent避免加载异常
String value = cache.getIfPresent(key);
if (value == null) {
    value = loadFromDatabase(key);
    cache.put(key, value);
}

// 或者使用带默认值的get方法
String value = cache.get(key, () -> loadFromDatabase(key));
```

### 5. 缓存预热

```java
// 应用启动时预热关键数据
@PostConstruct
public void warmUpCache() {
    List<String> hotKeys = getHotKeys();
    hotKeys.forEach(key -> {
        try {
            cache.get(key);
        } catch (Exception e) {
            logger.warn("预热缓存失败: key={}", key, e);
        }
    });
}
```

## 性能监控

### 统计指标

```java
CacheStats stats = cache.stats();
logger.info("缓存统计: hitCount={}, missCount={}, hitRate={:.2f}%, " +
           "evictionCount={}, loadCount={}, averageLoadTime={:.2f}ms",
    stats.hitCount(),
    stats.missCount(), 
    stats.hitRate() * 100,
    stats.evictionCount(),
    stats.loadCount(),
    stats.averageLoadPenalty() / 1_000_000.0);
```

### 关键指标说明

- **hitRate**：命中率，建议保持在80%以上
- **evictionCount**：驱逐次数，过高说明缓存大小不足
- **averageLoadTime**：平均加载时间，反映数据源性能
- **loadExceptionCount**：加载异常次数，需要关注

## 注意事项

1. **内存使用**：合理设置缓存大小，避免OOM
2. **线程安全**：缓存本身是线程安全的，但缓存的值对象需要考虑线程安全
3. **序列化**：如果需要持久化，确保缓存的键值都是可序列化的
4. **异常处理**：CacheLoader中的异常会被包装为ExecutionException
5. **内存泄漏**：注意移除监听器中的资源清理

## 依赖版本

```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.3-jre</version>
</dependency>
```

## 示例代码

完整的使用示例请参考：
- `CacheExample.java` - 各种使用场景的示例
- `GuavaLocalCacheTest.java` - 单元测试示例

## 扩展功能

如需更多功能，可以考虑：
- 分布式缓存：Redis、Hazelcast
- 多级缓存：本地缓存 + 分布式缓存
- 缓存同步：缓存更新通知机制
- 持久化：缓存数据持久化到磁盘