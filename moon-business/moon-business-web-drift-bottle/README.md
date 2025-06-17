# 漂流瓶应用 (Drift Bottle Application)

## 项目简介

这是一个基于Spring Boot开发的漂流瓶应用，模拟微信漂流瓶功能。用户可以写入纸条投放到海里，随机传递给其他用户，接收者可以选择丢弃或回复。

## 功能特性

### 核心功能
- **投放漂流瓶**：用户可以写入内容并投放漂流瓶到海里
- **捡起漂流瓶**：随机捡起海里漂流的瓶子
- **丢弃漂流瓶**：将捡起的漂流瓶重新丢回海里
- **回复漂流瓶**：对捡起的漂流瓶进行回复，回复后瓶子返回原发送者
- **查看历史**：查看发送和接收的漂流瓶历史记录
- **统计信息**：查看个人的发送、接收、回复统计

### 技术特性
- **熔断保护**：使用Resilience4j实现服务熔断
- **限流控制**：API访问频率限制
- **数据持久化**：使用JPA + H2数据库
- **参数校验**：完整的请求参数验证
- **异常处理**：全局异常处理机制
- **定时任务**：自动清理过期漂流瓶
- **监控端点**：Actuator健康检查和指标监控

## 技术栈

- **框架**：Spring Boot 3.x
- **数据库**：H2 (内存数据库)
- **ORM**：Spring Data JPA
- **熔断限流**：Resilience4j
- **监控**：Spring Boot Actuator
- **测试**：JUnit 5 + Mockito
- **构建工具**：Maven

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+

### 启动应用

```bash
# 进入项目目录
cd moon-business-web-drift-bottle

# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 访问地址

- **应用地址**：http://localhost:8083/drift-bottle
- **H2控制台**：http://localhost:8083/drift-bottle/h2-console
- **健康检查**：http://localhost:8083/drift-bottle/actuator/health
- **指标监控**：http://localhost:8083/drift-bottle/actuator/metrics

## API接口文档

### 基础路径
```
http://localhost:8083/drift-bottle/api/drift-bottle
```

### 接口列表

#### 1. 投放漂流瓶
```http
POST /throw
Content-Type: application/json

{
  "senderUsername": "用户名",
  "content": "漂流瓶内容"
}
```

#### 2. 捡起漂流瓶
```http
POST /pickup?username=用户名
```

#### 3. 丢弃漂流瓶
```http
POST /discard/{bottleId}?username=用户名
```

#### 4. 回复漂流瓶
```http
POST /reply
Content-Type: application/json

{
  "replierUsername": "回复者用户名",
  "content": "回复内容",
  "bottleId": 漂流瓶ID
}
```

#### 5. 查看发送的漂流瓶
```http
GET /sent?username=用户名&page=0&size=10
```

#### 6. 查看接收的漂流瓶
```http
GET /received?username=用户名&page=0&size=10
```

#### 7. 查看漂流瓶详情
```http
GET /detail/{bottleId}?username=用户名
```

#### 8. 查看漂流瓶回复
```http
GET /{bottleId}/replies?username=用户名&page=0&size=10
```

#### 9. 查看用户统计
```http
GET /statistics?username=用户名
```

### 响应格式

#### 成功响应
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    // 具体数据
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

#### 错误响应
```json
{
  "success": false,
  "message": "错误信息",
  "error": "详细错误描述",
  "timestamp": "2024-01-01T12:00:00"
}
```

## 数据模型

### 漂流瓶状态
- **FLOATING**：漂流中
- **PICKED_UP**：已被捡起
- **REPLIED**：已回复
- **COMPLETED**：已完成

### 漂流瓶实体
```java
{
  "id": 1,
  "senderUsername": "发送者用户名",
  "content": "漂流瓶内容",
  "status": "FLOATING",
  "currentHolder": "当前持有者",
  "passCount": 5,
  "createTime": "2024-01-01T12:00:00",
  "lastUpdateTime": "2024-01-01T12:00:00",
  "replies": []
}
```

### 回复实体
```java
{
  "id": 1,
  "replierUsername": "回复者用户名",
  "content": "回复内容",
  "replyTime": "2024-01-01T12:00:00",
  "bottleId": 1
}
```

## 配置说明

### 应用配置
```yaml
# 服务端口和上下文路径
server:
  port: 8083
  servlet:
    context-path: /drift-bottle

# 漂流瓶业务配置
drift-bottle:
  max-pass-count: 10        # 最大传递次数
  random-pass-count: 10     # 随机传递人数
  expire-days: 30           # 过期天数
  max-content-length: 500   # 内容最大长度
  daily-throw-limit: 10     # 每日投放限制
  daily-pickup-limit: 20    # 每日捡起限制
  daily-reply-limit: 15     # 每日回复限制
```

### 熔断器配置
```yaml
resilience4j:
  circuitbreaker:
    instances:
      driftBottleService:
        failure-rate-threshold: 60    # 失败率阈值
        wait-duration-in-open-state: 30s  # 熔断器打开等待时间
        sliding-window-size: 10       # 滑动窗口大小
```

### 限流器配置
```yaml
resilience4j:
  ratelimiter:
    instances:
      driftBottleService:
        limit-for-period: 10         # 时间窗口内允许的请求数
        limit-refresh-period: 1s     # 时间窗口大小
        timeout-duration: 3s         # 等待超时时间
```

## 测试

### 运行单元测试
```bash
mvn test
```

### 测试覆盖率
```bash
mvn jacoco:report
```

### 集成测试
```bash
mvn verify
```

## 监控和运维

### 健康检查
```bash
curl http://localhost:8083/drift-bottle/actuator/health
```

### 查看指标
```bash
# 查看所有指标
curl http://localhost:8083/drift-bottle/actuator/metrics

# 查看熔断器指标
curl http://localhost:8083/drift-bottle/actuator/metrics/resilience4j.circuitbreaker.calls

# 查看限流器指标
curl http://localhost:8083/drift-bottle/actuator/metrics/resilience4j.ratelimiter.calls
```

### 查看应用信息
```bash
curl http://localhost:8083/drift-bottle/actuator/info
```

## 开发指南

### 项目结构
```
src/
├── main/
│   ├── java/com/moon/cloud/drift/bottle/
│   │   ├── controller/     # 控制器层
│   │   ├── service/        # 服务层
│   │   ├── repository/     # 数据访问层
│   │   ├── entity/         # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   ├── exception/      # 异常处理
│   │   └── task/          # 定时任务
│   └── resources/
│       ├── application.yml # 应用配置
│       └── data.sql       # 初始化数据
└── test/
    ├── java/              # 测试代码
    └── resources/
        └── application-test.yml # 测试配置
```

### 代码规范
- 使用Java 17特性
- 遵循Spring Boot最佳实践
- 完整的JavaDoc注释
- 单元测试覆盖率 > 80%
- 使用SLF4J进行日志记录

### 扩展开发
1. **添加新功能**：在service层添加业务逻辑，controller层添加API接口
2. **数据库扩展**：修改entity和repository，更新数据库脚本
3. **监控扩展**：添加自定义指标和健康检查
4. **安全扩展**：集成Spring Security进行认证授权

## 常见问题

### Q: 如何修改数据库配置？
A: 修改`application.yml`中的`spring.datasource`配置，支持MySQL、PostgreSQL等。

### Q: 如何调整熔断器参数？
A: 修改`resilience4j.circuitbreaker`配置，根据实际业务调整阈值。

### Q: 如何查看详细日志？
A: 修改`logging.level`配置，设置为DEBUG或TRACE级别。

### Q: 如何部署到生产环境？
A: 使用`mvn clean package`打包，然后使用`java -jar`运行，或者构建Docker镜像。

## 版本历史

- **v1.0.0** - 初始版本，实现基础漂流瓶功能
- 支持投放、捡起、丢弃、回复功能
- 集成Resilience4j熔断限流
- 完整的测试覆盖

## 许可证

MIT License

## 联系方式

- 作者：Moon Cloud
- 邮箱：moon@example.com
- 项目地址：https://github.com/moon-cloud/drift-bottle