# Resilience4j 动态配置使用指南

本文档介绍如何在Java代码中实现Resilience4j的动态配置，并支持根据参数动态调整熔断器、限流器、重试等配置。

## 功能特性

- ✅ **动态配置**: 支持运行时动态调整Resilience4j配置参数
- ✅ **配置管理**: 统一管理熔断器、限流器、重试等组件
- ✅ **REST API**: 提供RESTful接口进行配置管理
- ✅ **状态监控**: 实时查看各组件的运行状态和指标
- ✅ **组合保护**: 支持多种保护机制的组合使用
- ✅ **测试接口**: 提供完整的测试接口验证功能

## 核心组件

### 1. 配置属性类 (`Resilience4jProperties`)

负责映射YAML配置文件中的Resilience4j配置到Java对象。

```java
@Component
@ConfigurationProperties(prefix = "resilience4j")
public class Resilience4jProperties {
    // 熔断器、限流器、重试配置
}
```

### 2. 配置管理器 (`Resilience4jConfigManager`)

核心管理类，负责创建、更新和管理Resilience4j实例。

```java
@Component
public class Resilience4jConfigManager {
    // 动态创建和更新配置
    public void updateCircuitBreakerConfig(String name, int slidingWindowSize, float failureRateThreshold, long waitDurationInOpenState)
    public void updateRateLimiterConfig(String name, int limitForPeriod, int limitRefreshPeriod, long timeoutDuration)
    public void updateRetryConfig(String name, int maxAttempts, long waitDuration)
}
```

### 3. 配置控制器 (`Resilience4jConfigController`)

提供REST API接口进行动态配置管理。

### 4. 业务服务 (`DriftBottleResilienceService`)

演示如何在业务代码中使用这些保护机制。

### 5. 测试控制器 (`DriftBottleTestController`)

提供测试接口验证各种保护机制的效果。

## API 接口说明

### 配置管理接口

#### 1. 更新熔断器配置
```http
PUT /api/resilience4j/circuit-breaker/{name}/config
Content-Type: application/json

{
  "slidingWindowSize": 20,
  "failureRateThreshold": 60.0,
  "waitDurationInOpenState": 60
}
```

#### 2. 更新限流器配置
```http
PUT /api/resilience4j/rate-limiter/{name}/config
Content-Type: application/json

{
  "limitForPeriod": 20,
  "limitRefreshPeriod": 1,
  "timeoutDuration": 1000
}
```

#### 3. 更新重试配置
```http
PUT /api/resilience4j/retry/{name}/config
Content-Type: application/json

{
  "maxAttempts": 5,
  "waitDuration": 2000
}
```

#### 4. 获取熔断器状态
```http
GET /api/resilience4j/circuit-breaker/{name}/state
```

#### 5. 获取限流器指标
```http
GET /api/resilience4j/rate-limiter/{name}/metrics
```

#### 6. 重置熔断器
```http
POST /api/resilience4j/circuit-breaker/{name}/reset
```

#### 7. 获取所有状态
```http
GET /api/resilience4j/status
```

### 测试接口

#### 1. 测试熔断器
```http
GET /api/drift-bottle/test/circuit-breaker?operation=测试操作
```

#### 2. 测试限流器
```http
GET /api/drift-bottle/test/rate-limiter?operation=限流测试
```

#### 3. 测试重试机制
```http
GET /api/drift-bottle/test/retry?operation=重试测试
```

#### 4. 测试组合保护
```http
GET /api/drift-bottle/test/combined?operation=组合保护测试
```

#### 5. 批量测试
```http
POST /api/drift-bottle/test/batch?count=50&type=rate-limiter
```

## 使用示例

### 1. 启动应用

```bash
cd /Users/xingleiwang/Documents/Code/Java/moon-cloud/moon-business/moon-business-web-drift-bottle
mvn spring-boot:run
```

应用将在 `http://localhost:8083/drift-bottle` 启动。

### 2. 查看当前配置状态

```bash
curl http://localhost:8083/drift-bottle/api/resilience4j/status
```

### 3. 测试默认配置

```bash
# 测试熔断器
curl "http://localhost:8083/drift-bottle/api/drift-bottle/test/circuit-breaker?operation=测试熔断器"

# 测试限流器
curl "http://localhost:8083/drift-bottle/api/drift-bottle/test/rate-limiter?operation=测试限流器"

# 测试重试机制
curl "http://localhost:8083/drift-bottle/api/drift-bottle/test/retry?operation=测试重试"
```

### 4. 动态调整配置

#### 调整熔断器配置（降低失败率阈值）
```bash
curl -X PUT http://localhost:8083/drift-bottle/api/resilience4j/circuit-breaker/drift-bottle/config \
  -H "Content-Type: application/json" \
  -d '{
    "slidingWindowSize": 20,
    "failureRateThreshold": 30.0,
    "waitDurationInOpenState": 60
  }'
```

#### 调整限流器配置（增加限流阈值）
```bash
curl -X PUT http://localhost:8083/drift-bottle/api/resilience4j/rate-limiter/drift-bottle/config \
  -H "Content-Type: application/json" \
  -d '{
    "limitForPeriod": 50,
    "limitRefreshPeriod": 1,
    "timeoutDuration": 1000
  }'
```

#### 调整重试配置（增加重试次数）
```bash
curl -X PUT http://localhost:8083/drift-bottle/api/resilience4j/retry/drift-bottle/config \
  -H "Content-Type: application/json" \
  -d '{
    "maxAttempts": 5,
    "waitDuration": 1500
  }'
```

### 5. 验证配置变更效果

```bash
# 再次测试，观察配置变更的效果
curl "http://localhost:8083/drift-bottle/api/drift-bottle/test/circuit-breaker?operation=验证新配置"

# 批量测试限流效果
curl -X POST "http://localhost:8083/drift-bottle/api/drift-bottle/test/batch?count=100&type=rate-limiter"
```

### 6. 监控状态变化

```bash
# 查看熔断器状态
curl http://localhost:8083/drift-bottle/api/resilience4j/circuit-breaker/drift-bottle/state

# 查看限流器指标
curl http://localhost:8083/drift-bottle/api/resilience4j/rate-limiter/drift-bottle/metrics

# 查看保护机制整体状态
curl http://localhost:8083/drift-bottle/api/drift-bottle/test/status
```

## 业务集成示例

在业务代码中使用这些保护机制：

```java
@Service
public class YourBusinessService {
    
    @Autowired
    private Resilience4jConfigManager configManager;
    
    public String yourBusinessMethod() {
        // 获取熔断器实例
        CircuitBreaker circuitBreaker = configManager.getOrCreateCircuitBreaker("your-service");
        
        // 装饰业务方法
        Supplier<String> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            // 你的业务逻辑
            return "业务执行成功";
        });
        
        return decoratedSupplier.get();
    }
}
```

## 配置参数说明

### 熔断器参数

- `slidingWindowSize`: 滑动窗口大小
- `failureRateThreshold`: 失败率阈值（百分比）
- `waitDurationInOpenState`: 熔断器开启状态持续时间（秒）
- `minimumNumberOfCalls`: 最小调用次数
- `slowCallDurationThreshold`: 慢调用时间阈值（毫秒）
- `slowCallRateThreshold`: 慢调用率阈值（百分比）

### 限流器参数

- `limitForPeriod`: 每个周期内允许的请求数
- `limitRefreshPeriod`: 限流周期（秒）
- `timeoutDuration`: 等待许可的超时时间（毫秒）

### 重试参数

- `maxAttempts`: 最大重试次数
- `waitDuration`: 重试间隔（毫秒）

## 注意事项

1. **配置更新**: 配置更新会创建新的实例，旧的实例会被替换
2. **状态保持**: 熔断器的状态在配置更新后会重置
3. **性能影响**: 频繁的配置更新可能会影响性能
4. **线程安全**: 所有操作都是线程安全的
5. **监控建议**: 建议结合Spring Boot Actuator进行监控

## 扩展功能

- 可以扩展支持更多的Resilience4j组件（如Bulkhead、TimeLimiter）
- 可以集成配置中心（如Nacos、Apollo）实现分布式配置管理
- 可以添加配置变更的审计日志
- 可以实现配置的版本管理和回滚功能

## 总结

通过这套动态配置方案，你可以：

1. **运行时调整**: 无需重启应用即可调整保护机制参数
2. **灵活配置**: 支持多种保护机制的组合使用
3. **实时监控**: 实时查看各组件的运行状态
4. **易于集成**: 简单的API接口，易于集成到现有系统
5. **测试友好**: 提供完整的测试接口验证功能

这种方案特别适合需要根据业务负载动态调整保护策略的场景，如电商促销、突发流量等情况。