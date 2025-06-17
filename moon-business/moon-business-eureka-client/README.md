# Moon Cloud Eureka Client & Server

## 项目简介

Moon Cloud Eureka Client & Server 是一个集成了 Eureka 客户端和服务端功能的微服务组件。它既可以作为 Eureka 客户端注册到其他注册中心，也可以作为 Eureka 服务端提供注册中心服务，实现了双重角色的灵活切换。

## 功能特性

- ✅ **双重角色**: 同时支持 Eureka 客户端和服务端功能
- ✅ **服务注册**: 可以注册到外部 Eureka 注册中心
- ✅ **服务发现**: 可以发现其他注册的服务
- ✅ **注册中心**: 可以作为注册中心为其他服务提供服务
- ✅ **健康检查**: 提供完整的健康检查和监控接口
- ✅ **配置灵活**: 支持多种配置模式和环境
- ✅ **高可用**: 支持集群部署和故障转移

## 技术栈

- **Spring Boot**: 3.x
- **Spring Cloud**: 2023.x
- **Netflix Eureka**: 服务注册与发现
- **Spring Boot Actuator**: 监控和管理
- **Maven**: 项目构建工具

## 快速开始

### 1. 编译项目

```bash
cd /Users/xingleiwang/Documents/Code/Java/moon-cloud/moon-business/moon-business-eureka-client
mvn clean compile
```

### 2. 运行应用

```bash
# 方式一：使用 Maven 插件
mvn spring-boot:run

# 方式二：使用 exec 插件
mvn exec:java -Dexec.mainClass=com.moon.cloud.eureka.client.EurekaClientServerApplication

# 方式三：打包后运行
mvn clean package -DskipTests
java -jar target/moon-business-eureka-client-1.0.0-SNAPSHOT.jar
```

### 3. 访问服务

- **Eureka 控制台**: http://localhost:8762
- **健康检查**: http://localhost:8762/api/health
- **服务信息**: http://localhost:8762/api/info
- **服务发现状态**: http://localhost:8762/api/discovery
- **监控端点**: http://localhost:8762/actuator

## 配置说明

### 应用配置 (application.yml)

```yaml
server:
  port: 8762  # 服务端口

spring:
  application:
    name: eureka-client-server  # 应用名称

eureka:
  instance:
    hostname: localhost
    prefer-ip-address: true
  client:
    register-with-eureka: true    # 作为客户端注册到注册中心
    fetch-registry: true          # 获取注册信息
    service-url:
      defaultZone: http://localhost:8761/eureka/  # 外部注册中心地址
  server:
    enable-self-preservation: false  # 关闭自我保护机制
```

### 环境变量配置

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| SERVER_PORT | 服务端口 | 8762 |
| EUREKA_SERVER_URL | 外部注册中心地址 | http://localhost:8761/eureka/ |
| EUREKA_INSTANCE_HOSTNAME | 实例主机名 | localhost |

## 部署模式

### 1. 独立模式

作为独立的注册中心运行，不注册到其他注册中心：

```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### 2. 客户端模式

仅作为客户端注册到外部注册中心：

```yaml
eureka:
  server:
    enable-self-preservation: true
```

### 3. 混合模式（默认）

既作为客户端又作为服务端，实现注册中心的高可用。

## API 接口

### 健康检查接口

```http
GET /api/health
```

响应示例：
```json
{
  "status": "UP",
  "timestamp": "2024-01-01T12:00:00",
  "application": "eureka-client-server",
  "port": "8762",
  "role": "EUREKA_CLIENT_SERVER"
}
```

### 服务信息接口

```http
GET /api/info
```

### 服务发现状态接口

```http
GET /api/discovery
```

## 监控指标

应用集成了 Spring Boot Actuator，提供以下监控端点：

- `/actuator/health` - 健康状态
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标
- `/actuator/env` - 环境变量

## 集群部署

### 多实例部署

1. **实例 1** (端口 8762):
```yaml
server:
  port: 8762
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8763/eureka/,http://localhost:8764/eureka/
```

2. **实例 2** (端口 8763):
```yaml
server:
  port: 8763
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8762/eureka/,http://localhost:8764/eureka/
```

## 故障排查

### 常见问题

1. **端口冲突**
   - 检查端口 8762 是否被占用
   - 修改 `server.port` 配置

2. **注册失败**
   - 检查外部注册中心是否可访问
   - 验证 `eureka.client.service-url.defaultZone` 配置

3. **服务发现异常**
   - 检查网络连接
   - 验证服务名称配置

### 日志配置

启用详细日志：
```yaml
logging:
  level:
    com.netflix.eureka: DEBUG
    com.netflix.discovery: DEBUG
```

## 注意事项

1. **版本兼容性**: 确保 Spring Cloud 版本与 Spring Boot 版本兼容
2. **网络配置**: 在生产环境中正确配置主机名和 IP 地址
3. **安全配置**: 生产环境建议启用安全认证
4. **性能调优**: 根据实际负载调整心跳和清理间隔

## 版本历史

- **v1.0.0** - 初始版本，支持双重角色功能

## 许可证

MIT License