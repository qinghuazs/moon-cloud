# Moon Thread Pool Actuator Endpoint 使用指南

## 概述

`MoonThreadPoolEndpoint` 是基于 Spring Boot Actuator 实现的线程池监控和管理端点，提供了标准化的监控接口，可以与其他监控工具（如 Prometheus、Grafana）无缝集成。

## 功能特性

- ✅ 符合 Spring Boot Actuator 标准
- ✅ 自动集成到 `/actuator` 路径下
- ✅ 支持统一的安全配置
- ✅ 支持 JMX 暴露
- ✅ 与监控工具（Micrometer、Prometheus）兼容
- ✅ 提供读取、写入、删除操作

## 配置启用

### 1. 应用配置文件

在 `application.yml` 中添加以下配置：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: threadpools  # 暴露线程池端点
      base-path: /actuator   # 默认路径
  endpoint:
    threadpools:
      enabled: true          # 启用线程池端点
  server:
    port: 8081            # 可选：使用独立端口
```

### 2. 安全配置（可选）

```yaml
management:
  endpoints:
    web:
      exposure:
        include: threadpools
  endpoint:
    threadpools:
      enabled: true
  security:
    enabled: true
    roles: ADMIN          # 需要 ADMIN 角色才能访问
```

### 3. JMX 支持（可选）

```yaml
management:
  endpoints:
    jmx:
      exposure:
        include: threadpools
```

## API 接口

### 1. 获取所有线程池信息

**请求：**
```http
GET /actuator/threadpools
```

**响应示例：**
```json
{
  "total": 2,
  "pools": [
    {
      "poolName": "async-pool",
      "corePoolSize": 5,
      "maximumPoolSize": 10,
      "activeCount": 3,
      "poolSize": 5,
      "largestPoolSize": 8,
      "taskCount": 1000,
      "completedTaskCount": 950,
      "queueSize": 10,
      "queueRemainingCapacity": 90,
      "utilizationRate": "30.00%",
      "queueUtilizationRate": "10.00%",
      "shutdown": false,
      "terminated": false,
      "terminating": false
    }
  ]
}
```

### 2. 获取特定线程池信息

**请求：**
```http
GET /actuator/threadpools/{poolName}
```

**响应示例：**
```json
{
  "info": {
    "poolName": "async-pool",
    "corePoolSize": 5,
    "maximumPoolSize": 10,
    "activeCount": 3,
    "poolSize": 5,
    "largestPoolSize": 8,
    "taskCount": 1000,
    "completedTaskCount": 950,
    "queueSize": 10,
    "queueRemainingCapacity": 90,
    "utilizationRate": "30.00%",
    "queueUtilizationRate": "10.00%",
    "shutdown": false,
    "terminated": false,
    "terminating": false
  }
}
```

### 3. 动态调整线程池参数

**请求：**
```http
POST /actuator/threadpools/{poolName}
Content-Type: application/json

{
  "corePoolSize": 8,
  "maximumPoolSize": 15
}
```

**响应示例：**
```json
{
  "poolName": "async-pool",
  "oldCorePoolSize": 5,
  "newCorePoolSize": 8,
  "oldMaximumPoolSize": 10,
  "newMaximumPoolSize": 15,
  "msg": "线程核心线程数和最大线程数调整成功！"
}
```

### 4. 关闭线程池

**请求：**
```http
DELETE /actuator/threadpools/{poolName}
```

**响应示例：**
```json
{
  "poolName": "async-pool"
}
```

## 监控集成

### 1. Prometheus 集成

添加 Micrometer Prometheus 依赖：

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

配置：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: threadpools,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. Grafana 仪表板

可以基于 Prometheus 指标创建 Grafana 仪表板，监控：
- 线程池使用率
- 队列使用率
- 任务执行统计
- 线程池状态变化

### 3. 健康检查集成

```yaml
management:
  endpoint:
    health:
      show-details: always
  health:
    threadpool:
      enabled: true
```

## 安全最佳实践

### 1. 生产环境配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: threadpools
      base-path: /management  # 使用非标准路径
  server:
    port: 8081             # 使用独立管理端口
  security:
    enabled: true
```

### 2. 网络访问控制

- 限制管理端点只能从内网访问
- 使用防火墙规则保护管理端口
- 配置适当的认证和授权

### 3. 操作审计

所有的写入和删除操作都会记录日志，便于审计：

```
2024-01-15 10:30:00 INFO  线程池 async-pool 核心线程数已从 5 调整为 8
2024-01-15 10:30:00 INFO  线程池 async-pool 最大线程数已从 10 调整为 15
2024-01-15 10:35:00 INFO  线程池 async-pool 已开始关闭
```

## 使用示例

### 1. 监控脚本

```bash
#!/bin/bash
# 获取所有线程池状态
curl -s http://localhost:8080/actuator/threadpools | jq .

# 获取特定线程池状态
curl -s http://localhost:8080/actuator/threadpools/async-pool | jq .

# 调整线程池参数
curl -X POST http://localhost:8080/actuator/threadpools/async-pool \
  -H "Content-Type: application/json" \
  -d '{"corePoolSize": 8, "maximumPoolSize": 15}'
```

### 2. Java 客户端

```java
@Service
public class ThreadPoolMonitorService {
    
    private final RestTemplate restTemplate;
    
    public ThreadPoolInfo getThreadPoolInfo(String poolName) {
        String url = "http://localhost:8080/actuator/threadpools/" + poolName;
        return restTemplate.getForObject(url, ThreadPoolInfo.class);
    }
    
    public void adjustThreadPool(String poolName, int coreSize, int maxSize) {
        String url = "http://localhost:8080/actuator/threadpools/" + poolName;
        Map<String, Integer> params = Map.of(
            "corePoolSize", coreSize,
            "maximumPoolSize", maxSize
        );
        restTemplate.postForObject(url, params, Map.class);
    }
}
```

## 与 REST Controller 的对比

| 特性 | Actuator Endpoint | REST Controller |
|------|------------------|----------------|
| 标准化 | ✅ Spring Boot 标准 | ❌ 自定义实现 |
| 安全集成 | ✅ 统一管理 | ❌ 需要手动配置 |
| 监控工具集成 | ✅ 原生支持 | ❌ 需要额外适配 |
| JMX 支持 | ✅ 自动支持 | ❌ 不支持 |
| 路径管理 | ✅ 统一 `/actuator` | ❌ 自定义路径 |
| 配置灵活性 | ✅ 丰富的配置选项 | ❌ 配置有限 |

## 总结

`MoonThreadPoolEndpoint` 提供了符合 Spring Boot 标准的线程池监控解决方案，特别适合：

1. **生产环境监控** - 与现有监控体系无缝集成
2. **DevOps 场景** - 标准化的运维接口
3. **微服务架构** - 统一的服务治理
4. **企业级应用** - 完善的安全和审计机制

建议在生产环境中优先使用 Actuator Endpoint 方式，在需要高度自定义的场景下可以考虑 REST Controller 方式。