# Moon Cloud 用户管理系统 API 文档

## 概述

Moon Cloud 用户管理系统提供完整的用户认证、授权和管理功能，支持 JWT 认证、用户注册登录、Google OAuth 登录、token 刷新和保活等功能。

## 基础信息

- **基础路径**: `/api/user`
- **认证方式**: Bearer Token (JWT)
- **API 版本**: v1.0

## 认证相关接口

### 1. 用户登录

**接口路径**: `POST /api/auth/login`

**接口描述**: 用户使用用户名和密码登录

**请求参数**:
```json
{
  "username": "string",  // 用户名
  "password": "string"   // 密码
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

### 2. 用户注册

**接口路径**: `POST /api/auth/register`

**接口描述**: 新用户注册并自动登录

**请求参数**:
```json
{
  "username": "string",     // 用户名 (3-50字符)
  "email": "string",        // 邮箱
  "password": "string",     // 密码 (8-100字符)
  "nickname": "string",     // 昵称 (可选)
  "phone": "string"         // 手机号 (可选)
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

### 3. Google OAuth 登录

**接口路径**: `POST /api/auth/login/google`

**接口描述**: 使用 Google OAuth 进行登录

**请求参数**:
```json
{
  "idToken": "string"  // Google ID Token
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

### 4. 刷新令牌

**接口路径**: `POST /api/auth/refresh`

**接口描述**: 使用刷新令牌获取新的访问令牌

**请求参数**:
```json
{
  "refreshToken": "string"  // 刷新令牌
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

### 5. 用户登出

**接口路径**: `POST /api/auth/logout`

**接口描述**: 用户登出，令牌加入黑名单

**请求头**:
```
Authorization: Bearer <accessToken>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 6. 验证令牌

**接口路径**: `POST /api/auth/validate`

**接口描述**: 验证访问令牌是否有效

**请求头**:
```
Authorization: Bearer <accessToken>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 7. 获取当前用户信息

**接口路径**: `GET /api/auth/me`

**接口描述**: 根据令牌获取当前登录用户信息

**请求头**:
```
Authorization: Bearer <accessToken>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatarUrl": "https://example.com/avatar.jpg",
    "phone": "13800138000",
    "status": 1,
    "providerType": "LOCAL",
    "isEmailVerified": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
}
```

### 8. Token 保活

**接口路径**: `POST /api/auth/keepalive`

**接口描述**: 延长当前令牌的有效期

**请求头**:
```
Authorization: Bearer <accessToken>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

## 错误响应

所有接口的错误响应格式统一：

```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

常见错误码：
- `400`: 请求参数错误
- `401`: 未认证或令牌无效
- `403`: 无权限访问
- `404`: 资源不存在
- `500`: 服务器内部错误

## 认证流程

### 普通用户注册/登录流程
1. 用户调用注册接口 `POST /api/auth/register` 创建账号
2. 或者用户调用登录接口 `POST /api/auth/login` 使用已有账号登录
3. 服务器返回 `accessToken` 和 `refreshToken`
4. 客户端在后续请求中携带 `accessToken` 进行认证
5. 当 `accessToken` 即将过期时，使用 `refreshToken` 刷新获取新令牌
6. 用户主动登出时调用 `POST /api/auth/logout`

### Google OAuth 登录流程
1. 前端获取 Google ID Token
2. 调用 Google 登录接口 `POST /api/auth/login/google` 传入 ID Token
3. 服务器验证 Google ID Token 的有效性
4. 如果是新用户，自动创建用户账号并关联 Google 账号
5. 如果是已有用户，更新用户信息
6. 返回 JWT 令牌供后续认证使用

### Token 保活机制
1. 客户端定期调用 `POST /api/auth/keepalive` 接口延长令牌有效期
2. 服务器会生成新的令牌并将旧令牌加入黑名单
3. 建议在令牌过期前 10-15 分钟进行保活

## 配置说明

### Google OAuth 配置
在应用配置文件中设置：
```yaml
google:
  oauth:
    client-id: your-google-client-id
```

### JWT 配置
```yaml
jwt:
  secret: moonCloudUserSecretKey2024
  expiration: 86400000      # 24小时 (毫秒)
  refresh-expiration: 604800000  # 7天 (毫秒)
```

## 安全特性

1. **密码加密**: 使用 BCrypt 算法加密用户密码
2. **JWT 黑名单**: 登出时将令牌加入黑名单防止重复使用
3. **登录失败限制**: 连续登录失败5次将锁定IP地址30分钟
4. **令牌验证**: 所有需要认证的接口都会验证JWT令牌的有效性
5. **CORS 支持**: 支持跨域访问配置
6. **用户状态检查**: 禁用用户无法登录

## 数据库表结构

### 用户表 (sys_user)
- `id`: 用户ID (主键)
- `username`: 用户名
- `password_hash`: 密码哈希
- `email`: 邮箱
- `phone`: 手机号
- `nickname`: 昵称
- `avatar_url`: 头像URL
- `status`: 状态 (0-禁用，1-启用)
- `google_id`: Google用户ID
- `provider_type`: 登录提供商类型 (LOCAL/GOOGLE)
- `is_email_verified`: 邮箱是否已验证
- `last_login_at`: 最后登录时间
- `created_at`: 创建时间
- `updated_at`: 更新时间

与短连接 URL 前端的联动通过标准的 JWT 认证实现，前端只需要：
1. 在登录成功后保存返回的 `accessToken`
2. 在每次 API 请求时在 Header 中携带 `Authorization: Bearer <accessToken>`
3. 定期使用 `refreshToken` 刷新令牌或调用保活接口
4. 处理认证失败的情况（如令牌过期、用户被禁用等）