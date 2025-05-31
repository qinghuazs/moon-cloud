# Moon AI MCP Jira

## 项目简介

Moon AI MCP Jira 是一个基于 Spring Boot 的 MCP（Model Context Protocol）服务，专门用于读取本地数据库中的 Jira 问题信息，并提供智能问题匹配和解决方案查询功能。

## 主要功能

- **问题查询**：根据服务名称、关键词、问题类型等条件查询 Jira 问题
- **智能匹配**：根据用户描述的问题，智能匹配相似的历史问题
- **解决方案展示**：展示问题的解决方案和相关补丁信息
- **多维度搜索**：支持按服务、状态、优先级等多个维度进行搜索
- **补丁管理**：查询和管理问题相关的补丁信息

## 技术栈

- **Java 21**
- **Spring Boot 3.2.0**
- **MyBatis Plus 3.5.10.1**
- **MySQL 8.0**
- **Maven**
- **Lombok**

## 快速开始

### 1. 环境准备

- JDK 21+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE jira_mcp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p jira_mcp < src/main/resources/sql/init.sql
```

### 3. 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jira_mcp?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

或者通过环境变量设置：
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

### 4. 启动应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

应用启动后，访问：http://localhost:8080/jira-mcp

## API 接口

### 基础路径
```
http://localhost:8080/jira-mcp/api/mcp/jira
```

### 主要接口

#### 1. 健康检查
```http
GET /health
```

#### 2. 智能问题匹配
```http
GET /match?problemDescription=用户登录超时&serviceName=user-service
```

#### 3. 根据服务查询问题
```http
GET /service/{serviceName}
```

#### 4. 关键词搜索
```http
GET /search/keyword?keyword=登录
```

#### 5. 高级搜索
```http
POST /search
Content-Type: application/json

{
  "serviceName": "user-service",
  "keyword": "登录",
  "status": "Resolved",
  "onlyResolved": true,
  "pageNum": 1,
  "pageSize": 10
}
```

#### 6. 获取已解决问题
```http
GET /resolved
```

#### 7. 获取有补丁的问题
```http
GET /with-patch
```

#### 8. 获取问题详情
```http
GET /detail/{id}
GET /detail/key/{issueKey}
```

## 数据模型

### JiraIssue 实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| issueKey | String | Jira问题编号 |
| summary | String | 问题标题 |
| description | String | 问题描述 |
| issueType | String | 问题类型（Bug、Task、Story等） |
| status | String | 问题状态（Open、In Progress、Resolved、Closed等） |
| priority | String | 优先级（High、Medium、Low等） |
| serviceName | String | 所属服务/项目 |
| component | String | 问题组件 |
| version | String | 问题版本 |
| fixVersion | String | 修复版本 |
| reporter | String | 报告人 |
| assignee | String | 经办人 |
| resolution | String | 解决方案描述 |
| resolutionDescription | String | 解决方案详细说明 |
| patchInfo | String | 相关补丁信息 |
| patchUrl | String | 补丁下载链接 |
| labels | String | 问题标签（用逗号分隔） |
| createdTime | LocalDateTime | 创建时间 |
| updatedTime | LocalDateTime | 更新时间 |
| resolvedTime | LocalDateTime | 解决时间 |
| deleted | Integer | 是否已删除 |

## 使用场景

### 1. 问题咨询
当用户遇到问题时，可以通过描述问题来查询是否有类似的历史问题：

```bash
curl "http://localhost:8080/jira-mcp/api/mcp/jira/match?problemDescription=用户登录接口响应很慢&serviceName=user-service"
```

### 2. 解决方案查询
查询特定服务的已解决问题，获取解决方案：

```bash
curl "http://localhost:8080/jira-mcp/api/mcp/jira/service/user-service"
```

### 3. 补丁查询
查询有补丁的问题，获取修复补丁：

```bash
curl "http://localhost:8080/jira-mcp/api/mcp/jira/with-patch"
```

## 配置说明

### 数据库配置
- 支持 MySQL 8.0+
- 使用 HikariCP 连接池
- 支持连接池监控

### 日志配置
- 支持控制台和文件日志
- 可配置日志级别
- 支持日志文件轮转

### 监控配置
- 集成 Spring Boot Actuator
- 提供健康检查端点
- 支持应用指标监控

## 开发指南

### 添加新的查询接口

1. 在 `JiraIssueMapper` 中添加新的查询方法
2. 在 `JiraIssueService` 中添加业务逻辑
3. 在 `JiraMcpController` 中添加 REST 接口

### 扩展智能匹配算法

可以在 `JiraIssueServiceImpl.matchSimilarProblems()` 方法中优化关键词提取和匹配算法。

## 部署说明

### Docker 部署

```dockerfile
FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY target/moon-ai-mcp-jira-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 生产环境配置

1. 配置生产数据库连接
2. 调整 JVM 参数
3. 配置日志输出路径
4. 设置监控告警

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

- 作者：Moon Cloud
- 邮箱：mooncloud@example.com