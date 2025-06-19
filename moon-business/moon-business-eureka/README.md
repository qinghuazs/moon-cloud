# Moon Cloud Eureka Server

## 项目简介

本项目是基于 Spring Cloud Netflix Eureka 的服务注册中心，用于微服务架构中的服务发现和注册。

## 功能特性

- ✅ 服务注册与发现
- ✅ 服务健康检查
- ✅ 服务实例管理
- ✅ Web 管理界面
- ✅ REST API 接口
- ✅ 监控端点集成

## 技术栈

- Spring Boot 3.4.1
- Spring Cloud 2023.0.0
- Netflix Eureka Server
- Java 21

## 快速开始

### 1. 启动服务

```bash
# 进入项目目录
cd moon-business-eureka

# 编译项目
mvn clean compile

# 启动服务
mvn spring-boot:run
```

### 2. 访问服务

- **Eureka 管理界面**: http://localhost:8761
- **健康检查接口**: http://localhost:8761/api/health
- **服务信息接口**: http://localhost:8761/api/info
- **监控端点**: http://localhost:8761/actuator

## 配置说明

### 核心配置

```yaml
server:
  port: 8761  # 服务端口

eureka:
  client:
    register-with-eureka: false    # 不向自己注册
    fetch-registry: false          # 不从自己获取注册信息
  server:
    enable-self-preservation: false  # 关闭自我保护（开发环境）
    eviction-interval-timer-in-ms: 10000  # 清理间隔
```

### 环境配置

- **开发环境**: 默认配置，关闭自我保护机制
- **生产环境**: 建议开启自我保护机制，调整清理间隔

## 客户端接入

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 2. 配置文件

```yaml
spring:
  application:
    name: your-service-name

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

### 3. 启用客户端

```java
@SpringBootApplication
@EnableEurekaClient
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

## API 接口

### 健康检查

```http
GET /api/health
```

响应示例：
```json
{
  "status": "UP",
  "service": "eureka-server",
  "timestamp": "2024-01-01T12:00:00",
  "message": "Eureka Server is running normally"
}
```

### 服务信息

```http
GET /api/info
```

响应示例：
```json
{
  "name": "Moon Cloud Eureka Server",
  "version": "1.0.0",
  "description": "Spring Cloud Netflix Eureka Server",
  "port": 8761
}
```

## 监控指标

通过 Spring Boot Actuator 提供的监控端点：

- `/actuator/health` - 健康状态
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标
- `/actuator/env` - 环境变量

## 部署说明

### Docker 部署

```dockerfile
FROM openjdk:21-jre-slim
VOLUME /tmp
COPY target/moon-business-eureka-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: moon-cloud/eureka-server:1.0.0
        ports:
        - containerPort: 8761
```

## 注意事项

1. **生产环境**建议开启自我保护机制
2. **集群部署**时需要配置多个 Eureka 实例相互注册
3. **网络分区**时可能出现服务实例不一致的情况
4. **安全配置**生产环境建议启用认证和 HTTPS

## 故障排查

### 常见问题

1. **服务无法注册**
   - 检查网络连接
   - 确认 Eureka 服务地址配置正确
   - 查看客户端日志

2. **服务实例被剔除**
   - 检查心跳配置
   - 确认服务健康状态
   - 调整续约间隔

3. **管理界面无法访问**
   - 确认端口未被占用
   - 检查防火墙设置
   - 查看启动日志

## 版本历史

- **v1.0.0** - 初始版本，基础功能实现

## 许可证

MIT License