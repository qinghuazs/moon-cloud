# 统一响应类迁移指南

## 概述

本指南帮助您将项目中的旧响应类（Result、ApiResponse 等）迁移到新的统一响应类 `MoonCloudResponse`。

## 迁移步骤

### 第一步：添加依赖

在需要迁移的模块的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.mooncloud</groupId>
    <artifactId>moon-spring-boot-starter-response</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 第二步：配置文件

在 `application.yml` 中添加配置（可选，默认自动启用）：

```yaml
moon:
  response:
    enabled: true                    # 启用统一响应
    exception-handler-enabled: true  # 启用全局异常处理
    include-trace-id: true          # 包含追踪ID
    include-timestamp: true         # 包含时间戳
    log-exception: true             # 打印异常日志
```

### 第三步：替换控制器返回类型

#### 1. 替换 Result 类

**旧代码：**
```java
import com.moon.cloud.user.common.Result;

@RestController
public class UserController {

    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        return Result.success(user);
    }
}
```

**新代码：**
```java
import com.moon.cloud.response.web.MoonCloudResponse;
import com.moon.cloud.response.enums.ResponseCode;
import com.moon.cloud.response.handler.BusinessException;

@RestController
public class UserController {

    @GetMapping("/user/{id}")
    public MoonCloudResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        return MoonCloudResponse.success(user);
    }
}
```

#### 2. 替换 PageResult 类

**旧代码：**
```java
@GetMapping("/users")
public PageResult<User> listUsers(Page<User> page) {
    IPage<User> result = userService.page(page);
    return PageResult.success(result);
}
```

**新代码：**
```java
@GetMapping("/users")
public MoonCloudResponse<List<User>> listUsers(Page<User> page) {
    IPage<User> result = userService.page(page);
    return ResponseAdapter.fromPage(result);
}
```

#### 3. 替换 ApiResponse 类

**旧代码：**
```java
@PostMapping("/short-url")
public ApiResponse<String> createShortUrl(@RequestBody String longUrl) {
    String shortUrl = service.create(longUrl);
    return ApiResponse.success(shortUrl);
}
```

**新代码：**
```java
@PostMapping("/short-url")
public MoonCloudResponse<String> createShortUrl(@RequestBody String longUrl) {
    String shortUrl = service.create(longUrl);
    return MoonCloudResponse.success(shortUrl);
}
```

### 第四步：替换服务层异常处理

**旧代码：**
```java
@Service
public class UserService {

    public Result<User> updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        // 更新逻辑
        return Result.success(user);
    }
}
```

**新代码：**
```java
@Service
public class UserService {

    public User updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id);
        // 使用异常代替返回错误
        BusinessException.throwIf(user == null, ResponseCode.USER_NOT_FOUND);
        // 更新逻辑
        return user;
    }
}
```

### 第五步：替换响应码枚举

**映射关系：**

| 旧 ResultCode | 新 ResponseCode | 说明 |
|--------------|----------------|------|
| SUCCESS | ResponseCode.SUCCESS | 成功 |
| ERROR | ResponseCode.INTERNAL_SERVER_ERROR | 服务器错误 |
| PARAM_ERROR | ResponseCode.BAD_REQUEST | 参数错误 |
| UNAUTHORIZED | ResponseCode.UNAUTHORIZED | 未认证 |
| FORBIDDEN | ResponseCode.FORBIDDEN | 无权限 |
| USER_NOT_FOUND | ResponseCode.USER_NOT_FOUND | 用户不存在 |
| TOKEN_EXPIRED | ResponseCode.TOKEN_EXPIRED | 令牌过期 |
| ... | ... | ... |

### 第六步：处理特殊情况

#### 1. 渐进式迁移

如果不能一次性替换所有代码，可以使用适配器：

```java
// 在过渡期，可以将旧Result转换为新响应
public MoonCloudResponse<User> getUser(Long id) {
    Result<User> oldResult = oldService.getUser(id);
    return ResponseAdapter.convert(oldResult);
}
```

#### 2. 自定义异常处理

如果有自定义的异常处理需求：

```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(MyBusinessException.class)
    public MoonCloudResponse<?> handleMyException(MyBusinessException e) {
        return MoonCloudResponse.error(e.getCode(), e.getMessage());
    }
}
```

## 迁移检查清单

- [ ] 添加 moon-spring-boot-starter-response 依赖
- [ ] 替换控制器返回类型为 MoonCloudResponse
- [ ] 将 Result.error() 替换为 throw new BusinessException()
- [ ] 将 PageResult 替换为 ResponseAdapter.fromPage()
- [ ] 更新服务层方法，使用异常代替错误返回
- [ ] 替换响应码枚举引用
- [ ] 测试 API 响应格式是否正确
- [ ] 测试异常处理是否生效
- [ ] 更新 API 文档

## 常见问题

### Q1: 是否必须抛出异常？

不是必须的。您可以选择：
- 推荐：抛出 BusinessException，由全局处理器处理
- 可选：直接返回 MoonCloudResponse.error()

### Q2: 如何处理已有的全局异常处理器？

- 如果已有全局异常处理器，可以继承 GlobalExceptionHandler
- 或者禁用自动配置：`moon.response.exception-handler-enabled=false`

### Q3: 如何保持向后兼容？

使用 ResponseAdapter 进行适配，可以在过渡期同时支持新旧响应格式。

### Q4: 分页响应如何处理？

- 使用 MyBatis Plus：`ResponseAdapter.fromPage(page)`
- 手动构建：`MoonCloudResponse.page(data, current, size, total)`

## 迁移示例项目

完整的迁移示例请参考：
`com.moon.cloud.response.example.ExampleController`

该类展示了各种场景的迁移方式。