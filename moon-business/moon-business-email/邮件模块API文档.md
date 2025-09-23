# Moon Cloud 邮件模块 API 文档

## 概述

Moon Cloud 邮件模块提供完整的邮件发送、模板管理和队列处理功能。支持多种邮件格式（文本、HTML、模板）、批量发送、定时发送等企业级邮件功能。

## 服务端口和路径

- **服务端口**: 8081
- **上下文路径**: `/api/email`
- **Swagger UI**: http://localhost:8081/api/email/swagger-ui.html
- **API 文档**: http://localhost:8081/api/email/v3/api-docs

## 邮件发送接口

### 1. 发送邮件

**接口**: `POST /api/email/send`

**描述**: 发送单个或批量邮件，支持丰富的配置选项

**请求体**: EmailSendRequest
```json
{
  "to": ["user@example.com"],
  "cc": ["cc@example.com"],
  "bcc": ["bcc@example.com"],
  "subject": "邮件主题",
  "content": "邮件内容",
  "htmlContent": "<h1>HTML邮件内容</h1>",
  "templateCode": "WELCOME_EMAIL",
  "templateVariables": {
    "username": "张三",
    "verificationCode": "123456"
  },
  "attachments": [
    {
      "filename": "document.pdf",
      "content": "base64编码的文件内容",
      "contentType": "application/pdf"
    }
  ],
  "sendTime": "2024-12-25T10:00:00",
  "businessType": "USER_REGISTRATION",
  "priority": 1,
  "enableRetry": true
}
```

**响应**: EmailSendResponse
```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "emailRecordId": 12345,
    "success": true,
    "message": "邮件发送成功",
    "sentTime": "2024-01-15T10:30:00"
  }
}
```

### 2. 发送简单文本邮件

**接口**: `POST /api/email/send/simple`

**描述**: 发送简单的文本邮件

**参数**:
- `to` (string): 收件人邮箱
- `subject` (string): 邮件主题
- `content` (string): 邮件内容

**示例**:
```
POST /api/email/send/simple?to=user@example.com&subject=测试邮件&content=这是一封测试邮件
```

### 3. 发送HTML邮件

**接口**: `POST /api/email/send/html`

**描述**: 发送HTML格式的邮件

**参数**:
- `to` (string): 收件人邮箱
- `subject` (string): 邮件主题
- `htmlContent` (string): HTML内容

### 4. 发送模板邮件

**接口**: `POST /api/email/send/template`

**描述**: 使用模板发送邮件

**参数**:
- `to` (string): 收件人邮箱
- `templateCode` (string): 模板编码

**请求体**: 模板变量 (JSON)
```json
{
  "username": "张三",
  "product": "Moon Cloud",
  "verificationUrl": "https://example.com/verify/token123"
}
```

### 5. 批量发送邮件

**接口**: `POST /api/email/send/batch`

**描述**: 批量发送邮件给多个收件人

**参数**:
- `toEmails` (array): 收件人邮箱列表
- `subject` (string): 邮件主题
- `content` (string): 邮件内容

### 6. 定时发送邮件

**接口**: `POST /api/email/send/schedule`

**描述**: 设置邮件定时发送

**请求体**: EmailSendRequest (包含 `sendTime` 字段)

### 7. 重发邮件

**接口**: `POST /api/email/resend/{emailRecordId}`

**描述**: 重新发送失败的邮件

**路径参数**:
- `emailRecordId` (long): 邮件记录ID

### 8. 取消发送邮件

**接口**: `POST /api/email/cancel/{emailRecordId}`

**描述**: 取消待发送或定时发送的邮件

**路径参数**:
- `emailRecordId` (long): 邮件记录ID

## 邮件管理接口

### 9. 获取邮件记录

**接口**: `GET /api/email/record/{emailRecordId}`

**描述**: 根据ID获取邮件发送记录

**响应**: EmailRecord
```json
{
  "code": 200,
  "data": {
    "id": 12345,
    "toEmail": "user@example.com",
    "subject": "邮件主题",
    "content": "邮件内容",
    "status": 2,
    "statusName": "发送成功",
    "sendTime": "2024-01-15T10:30:00",
    "businessType": "USER_REGISTRATION",
    "retryCount": 0,
    "errorMessage": null
  }
}
```

### 10. 获取邮件统计

**接口**: `GET /api/email/statistics`

**描述**: 获取邮件发送统计信息

**参数**:
- `businessType` (string, 可选): 业务类型
- `startDate` (string, 可选): 开始日期 (yyyy-MM-dd)
- `endDate` (string, 可选): 结束日期 (yyyy-MM-dd)

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalSent": 1500,
    "successCount": 1450,
    "failedCount": 30,
    "pendingCount": 20,
    "successRate": "96.67%",
    "dailyStats": [
      {
        "date": "2024-01-15",
        "sent": 150,
        "success": 145,
        "failed": 5
      }
    ]
  }
}
```

### 11. 验证邮箱地址

**接口**: `GET /api/email/validate`

**描述**: 验证邮箱地址格式是否正确

**参数**:
- `email` (string): 邮箱地址

### 12. 测试邮件配置

**接口**: `POST /api/email/test`

**描述**: 测试邮件服务器配置是否正确

## 邮件队列管理接口

### 13. 获取队列状态

**接口**: `GET /api/email/queue/status`

**描述**: 获取邮件队列状态信息

**响应**:
```json
{
  "code": 200,
  "data": {
    "queueSize": 50,
    "processingSize": 5,
    "queueEnabled": true,
    "maxQueueSize": 1000,
    "statusStatistics": [
      {
        "status": "PENDING",
        "count": 20
      },
      {
        "status": "SUCCESS",
        "count": 1450
      },
      {
        "status": "FAILED",
        "count": 30
      }
    ]
  }
}
```

### 14. 处理邮件队列

**接口**: `POST /api/email/queue/process`

**描述**: 手动触发邮件队列处理

### 15. 清空邮件队列

**接口**: `POST /api/email/queue/clear`

**描述**: 清空所有待发送的邮件队列

## 邮件模板管理接口

### 16. 创建邮件模板

**接口**: `POST /api/email/template`

**描述**: 创建新的邮件模板

**请求体**: EmailTemplateCreateRequest
```json
{
  "templateCode": "WELCOME_EMAIL",
  "templateName": "欢迎邮件模板",
  "templateType": "NOTIFICATION",
  "subject": "欢迎使用 ${product}！",
  "content": "<h1>欢迎 ${username}!</h1><p>感谢您注册 ${product}。</p>",
  "templateEngine": "THYMELEAF",
  "variables": [
    {
      "name": "username",
      "type": "STRING",
      "required": true,
      "description": "用户名"
    },
    {
      "name": "product",
      "type": "STRING",
      "required": true,
      "description": "产品名称"
    }
  ],
  "description": "用户注册欢迎邮件",
  "status": 1
}
```

### 17. 更新邮件模板

**接口**: `PUT /api/email/template/{id}`

**描述**: 更新指定的邮件模板

**路径参数**:
- `id` (long): 模板ID

### 18. 删除邮件模板

**接口**: `DELETE /api/email/template/{id}`

**描述**: 删除指定的邮件模板

### 19. 获取邮件模板详情

**接口**: `GET /api/email/template/{id}`

**描述**: 根据ID获取邮件模板详情

### 20. 根据编码获取模板

**接口**: `GET /api/email/template/code/{templateCode}`

**描述**: 根据模板编码获取邮件模板

### 21. 获取模板列表

**接口**: `GET /api/email/template/list`

**描述**: 获取邮件模板列表

**参数**:
- `templateType` (string, 可选): 模板类型
- `status` (integer, 可选): 状态

### 22. 分页查询模板

**接口**: `GET /api/email/template/page`

**描述**: 分页查询邮件模板

**参数**:
- `page` (int): 页码，默认1
- `size` (int): 页大小，默认10
- `templateType` (string, 可选): 模板类型
- `status` (integer, 可选): 状态
- `keyword` (string, 可选): 搜索关键词

### 23. 渲染模板

**接口**: `POST /api/email/template/{templateCode}/render`

**描述**: 渲染邮件模板内容

**路径参数**:
- `templateCode` (string): 模板编码

**请求体**: 模板变量 (JSON)

### 24. 渲染模板主题

**接口**: `POST /api/email/template/{templateCode}/render/subject`

**描述**: 渲染邮件模板主题

### 25. 验证模板语法

**接口**: `POST /api/email/template/validate`

**描述**: 验证邮件模板语法是否正确

**参数**:
- `content` (string): 模板内容
- `templateEngine` (string): 模板引擎，默认THYMELEAF

### 26. 预览模板

**接口**: `POST /api/email/template/{templateCode}/preview`

**描述**: 预览邮件模板效果

### 27. 复制模板

**接口**: `POST /api/email/template/{sourceId}/copy`

**描述**: 复制现有邮件模板

**路径参数**:
- `sourceId` (long): 源模板ID

**参数**:
- `newTemplateCode` (string): 新模板编码
- `newTemplateName` (string): 新模板名称

### 28. 更新模板状态

**接口**: `PUT /api/email/template/{id}/status`

**描述**: 启用或禁用邮件模板

**参数**:
- `status` (integer): 状态 (0-禁用, 1-启用)

### 29. 设置默认模板

**接口**: `PUT /api/email/template/{id}/default`

**描述**: 设置指定类型的默认模板

**参数**:
- `templateType` (string): 模板类型

### 30. 获取模板变量

**接口**: `GET /api/email/template/{templateCode}/variables`

**描述**: 获取模板的变量定义

## 数据模型

### EmailSendRequest
```json
{
  "to": ["string"],                    // 收件人列表 (必填)
  "cc": ["string"],                    // 抄送列表 (可选)
  "bcc": ["string"],                   // 密送列表 (可选)
  "subject": "string",                 // 邮件主题 (必填)
  "content": "string",                 // 文本内容 (可选)
  "htmlContent": "string",             // HTML内容 (可选)
  "templateCode": "string",            // 模板编码 (可选)
  "templateVariables": {},             // 模板变量 (可选)
  "attachments": [],                   // 附件列表 (可选)
  "sendTime": "2024-01-15T10:00:00",   // 发送时间 (可选，为空立即发送)
  "businessType": "string",            // 业务类型 (可选)
  "priority": 1,                       // 优先级 1-5 (可选，默认3)
  "enableRetry": true                  // 是否启用重试 (可选，默认true)
}
```

### EmailRecord
```json
{
  "id": 12345,
  "toEmail": "user@example.com",
  "ccEmail": "cc@example.com",
  "bccEmail": "bcc@example.com",
  "subject": "邮件主题",
  "content": "邮件内容",
  "htmlContent": "<h1>HTML内容</h1>",
  "templateCode": "WELCOME_EMAIL",
  "templateVariables": "{}",
  "attachmentInfo": "[]",
  "status": 2,
  "statusName": "发送成功",
  "businessType": "USER_REGISTRATION",
  "priority": 3,
  "sendTime": "2024-01-15T10:30:00",
  "scheduleTime": null,
  "sentTime": "2024-01-15T10:30:15",
  "retryCount": 0,
  "maxRetryCount": 3,
  "errorMessage": null,
  "createTime": "2024-01-15T10:30:00",
  "updateTime": "2024-01-15T10:30:15"
}
```

### EmailTemplate
```json
{
  "id": 1,
  "templateCode": "WELCOME_EMAIL",
  "templateName": "欢迎邮件模板",
  "templateType": "NOTIFICATION",
  "subject": "欢迎使用 ${product}！",
  "content": "<h1>欢迎 ${username}!</h1>",
  "templateEngine": "THYMELEAF",
  "variables": "[]",
  "description": "用户注册欢迎邮件",
  "status": 1,
  "isDefault": false,
  "createTime": "2024-01-15T09:00:00",
  "updateTime": "2024-01-15T09:00:00"
}
```

## 状态码说明

### 邮件状态 (EmailStatus)
- `0` - DRAFT: 草稿
- `1` - PENDING: 待发送
- `2` - SENDING: 发送中
- `3` - SUCCESS: 发送成功
- `4` - FAILED: 发送失败
- `5` - CANCELLED: 已取消

### 模板状态
- `0` - 禁用
- `1` - 启用

### 模板类型 (TemplateType)
- `NOTIFICATION` - 通知类
- `MARKETING` - 营销类
- `SYSTEM` - 系统类
- `VERIFICATION` - 验证类

### 模板引擎
- `THYMELEAF` - Thymeleaf引擎
- `FREEMARKER` - FreeMarker引擎

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 邮件模板不存在 |
| 1002 | 邮件模板渲染失败 |
| 1003 | 邮件发送失败 |
| 1004 | 邮箱地址格式错误 |
| 1005 | 邮件队列已满 |
| 1006 | 附件大小超限 |

## 配置说明

### 环境变量配置
```bash
# SMTP配置
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM_NAME=Moon Cloud

# 调试配置
MAIL_DEBUG=false
```

### 应用配置 (application.yml)
```yaml
moon:
  email:
    # SMTP服务器配置
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    from-name: ${MAIL_FROM_NAME:Moon Cloud}

    # 安全配置
    enable-tls: true
    enable-ssl: false

    # 发送限制
    max-recipients: 50

    # 队列配置
    enable-queue: true
    queue-max-size: 1000
    async-pool-size: 10

    # 重试配置
    retry-count: 3
    retry-interval: 5000
```

## 使用示例

### 发送简单邮件
```bash
curl -X POST "http://localhost:8081/api/email/send/simple" \
  -d "to=user@example.com" \
  -d "subject=测试邮件" \
  -d "content=这是一封测试邮件"
```

### 发送模板邮件
```bash
curl -X POST "http://localhost:8081/api/email/send/template" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "user@example.com",
    "templateCode": "WELCOME_EMAIL",
    "variables": {
      "username": "张三",
      "product": "Moon Cloud"
    }
  }'
```

### 创建邮件模板
```bash
curl -X POST "http://localhost:8081/api/email/template" \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "WELCOME_EMAIL",
    "templateName": "欢迎邮件模板",
    "templateType": "NOTIFICATION",
    "subject": "欢迎使用 ${product}！",
    "content": "<h1>欢迎 ${username}!</h1><p>感谢您注册 ${product}。</p>",
    "templateEngine": "THYMELEAF"
  }'
```

## 最佳实践

1. **模板管理**: 建议为不同业务场景创建专门的邮件模板，提高复用性
2. **异步发送**: 大批量邮件建议使用队列异步发送，避免阻塞主业务流程
3. **错误处理**: 重要邮件启用重试机制，设置合理的重试次数和间隔
4. **监控统计**: 定期查看邮件发送统计，及时发现和解决问题
5. **安全配置**: 生产环境中使用应用密码而非明文密码，启用TLS加密

## 注意事项

1. Gmail等邮箱需要开启"应用密码"功能
2. 大附件建议使用云存储链接替代直接附件
3. 生产环境建议配置专门的SMTP服务器
4. 注意邮件发送频率限制，避免被识别为垃圾邮件
5. 模板变量支持多级对象访问，如 `${user.profile.name}`