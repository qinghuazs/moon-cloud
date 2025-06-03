# Jira MCP API 使用示例

本文档提供了 Jira MCP 服务的详细 API 使用示例。

## 基础信息

- **服务地址**: http://localhost:8080/jira-mcp
- **API 基础路径**: /api/mcp/jira
- **响应格式**: JSON

## 1. 健康检查

检查服务是否正常运行。<!-- Spring AI MCP Server Starter -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- MCP Java SDK -->
<dependency>
    <groupId>io.github.modelcontextprotocol</groupId>
    <artifactId>mcp-java-sdk</artifactId>
    <version>0.8.0</version>
</dependency>

```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/health"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Jira MCP服务运行正常",
  "data": "OK",
  "timestamp": 1704067200000
}
```

## 2. 智能问题匹配

根据问题描述智能匹配相似的历史问题。

```bash
# 基础匹配
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/match?problemDescription=用户登录接口响应很慢"

# 指定服务范围的匹配
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/match?problemDescription=用户登录接口响应很慢&serviceName=user-service"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "匹配成功",
  "data": [
    {
      "id": 1,
      "issueKey": "PROJ-001",
      "summary": "用户登录接口响应超时",
      "description": "在高并发情况下，用户登录接口响应时间超过5秒，导致用户体验差。",
      "issueType": "Bug",
      "status": "Resolved",
      "priority": "High",
      "serviceName": "user-service",
      "resolution": "Fixed",
      "resolutionDescription": "优化数据库连接池配置，增加最大连接数从10调整为50，设置合理的超时时间。",
      "patchInfo": "修复补丁包含：1. 更新application.yml中的数据库连接池配置 2. 添加连接池监控代码",
      "patchUrl": "http://patch.example.com/user-service-v1.0.1.patch",
      "labelList": ["登录", "超时", "数据库", "连接池"],
      "createdTime": "2024-01-10T09:00:00",
      "resolvedTime": "2024-01-15T10:30:00"
    }
  ],
  "timestamp": 1704067200000
}
```

## 3. 根据服务查询问题

查询特定服务的所有问题。

```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/service/user-service"
```

## 4. 关键词搜索

根据关键词搜索相关问题。

```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/search/keyword?keyword=登录"
```

## 5. 服务+关键词搜索

在特定服务范围内根据关键词搜索。

```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/search/service-keyword?serviceName=user-service&keyword=超时"
```

## 6. 高级搜索

使用复杂条件进行搜索。

```bash
curl -X POST "http://localhost:8080/jira-mcp/api/mcp/jira/search" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "user-service",
    "keyword": "登录",
    "issueType": "Bug",
    "status": "Resolved",
    "priority": "High",
    "onlyResolved": true,
    "onlyWithPatch": false,
    "pageNum": 1,
    "pageSize": 10
  }'
```

**请求参数说明**:
- `serviceName`: 服务名称（可选）
- `keyword`: 搜索关键词（可选）
- `issueType`: 问题类型，如 Bug、Task、Story（可选）
- `status`: 问题状态，如 Open、Resolved、Closed（可选）
- `priority`: 优先级，如 High、Medium、Low（可选）
- `onlyResolved`: 是否只查询已解决的问题（可选）
- `onlyWithPatch`: 是否只查询有补丁的问题（可选）
- `pageNum`: 页码，从1开始（默认1）
- `pageSize`: 每页大小（默认10）

## 7. 获取已解决问题

查询所有已解决的问题。

```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/resolved"
```

## 8. 获取有补丁的问题

查询所有包含补丁信息的问题。

```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/with-patch"
```

## 9. 获取问题详情

### 根据ID获取
```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/detail/1"
```

### 根据问题编号获取
```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/detail/key/PROJ-001"
```

**详情响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "issueKey": "PROJ-001",
    "summary": "用户登录接口响应超时",
    "description": "在高并发情况下，用户登录接口响应时间超过5秒，导致用户体验差。经过分析发现是数据库连接池配置不当导致的。",
    "issueType": "Bug",
    "status": "Resolved",
    "priority": "High",
    "serviceName": "user-service",
    "component": "authentication",
    "version": "1.0.0",
    "fixVersion": "1.0.1",
    "reporter": "zhang.san",
    "assignee": "li.si",
    "resolution": "Fixed",
    "resolutionDescription": "优化数据库连接池配置，增加最大连接数从10调整为50，设置合理的超时时间。同时添加了连接池监控。",
    "patchInfo": "修复补丁包含：1. 更新application.yml中的数据库连接池配置 2. 添加连接池监控代码",
    "patchUrl": "http://patch.example.com/user-service-v1.0.1.patch",
    "labelList": ["登录", "超时", "数据库", "连接池"],
    "createdTime": "2024-01-10T09:00:00",
    "updatedTime": "2024-01-15T10:30:00",
    "resolvedTime": "2024-01-15T10:30:00"
  },
  "timestamp": 1704067200000
}
```

## 使用场景示例

### 场景1：用户遇到登录问题

用户反馈："我们的用户登录功能很慢，经常超时"

**步骤1**: 智能匹配相似问题
```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/match?problemDescription=用户登录功能很慢经常超时&serviceName=user-service"
```

**步骤2**: 查看匹配到的问题详情
```bash
curl -X GET "http://localhost:8080/jira-mcp/api/mcp/jira/detail/key/PROJ-001"
```

**步骤3**: 下载相关补丁
根据返回的 `patchUrl` 下载补丁文件。

### 场景2：查询特定服务的所有已解决问题

```bash
# 查询支付服务的已解决问题
curl -X POST "http://localhost:8080/jira-mcp/api/mcp/jira/search" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "payment-service",
    "onlyResolved": true
  }'
```

### 场景3：查找包含补丁的高优先级问题

```bash
curl -X POST "http://localhost:8080/jira-mcp/api/mcp/jira/search" \
  -H "Content-Type: application/json" \
  -d '{
    "priority": "High",
    "onlyWithPatch": true
  }'
```

## 错误处理

### 常见错误响应

**参数错误**:
```json
{
  "code": 400,
  "message": "问题描述不能为空",
  "data": null,
  "timestamp": 1704067200000
}
```

**资源不存在**:
```json
{
  "code": 404,
  "message": "问题不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

**服务器错误**:
```json
{
  "code": 500,
  "message": "查询失败：数据库连接异常",
  "data": null,
  "timestamp": 1704067200000
}
```

## 集成建议

### 1. 在 AI 助手中集成

可以将这些 API 集成到 AI 助手中，当用户咨询技术问题时，自动调用相关接口查询历史问题和解决方案。

### 2. 在监控系统中集成

当监控系统检测到异常时，可以自动调用匹配接口，查找相似的历史问题和解决方案。

### 3. 在工单系统中集成

在创建新工单时，自动匹配相似的历史问题，提供解决方案参考。