# Moon Spring Boot Starter Response

## 概述

`moon-spring-boot-starter-response` 是一个全局统一响应处理组件，提供了标准化的 API 响应格式、异常处理和响应码管理功能。

### 主要特性

- ✅ **统一响应格式**：提供标准化的响应结构，支持泛型数据
- ✅ **丰富的响应码**：内置完善的响应码枚举体系（200+ 状态码）
- ✅ **全局异常处理**：自动捕获和处理各类异常，返回统一格式
- ✅ **分页支持**：内置分页响应结构，支持 MyBatis Plus
- ✅ **链式调用**：支持流式 API，便于扩展响应信息
- ✅ **追踪支持**：支持请求追踪 ID，便于日志关联
- ✅ **兼容性**：提供适配器，兼容项目中的旧响应类
- ✅ **Swagger 支持**：完整的 OpenAPI 注解支持

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.mooncloud</groupId>
    <artifactId>moon-spring-boot-starter-response</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基础使用

#### 2.1 控制器中使用统一响应

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 成功响应 - 无数据
    @GetMapping("/test")
    public MoonCloudResponse<Void> test() {
        return MoonCloudResponse.success();
    }

    // 成功响应 - 带数据
    @GetMapping("/{id}")
    public MoonCloudResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return MoonCloudResponse.success(user);
    }

    // 成功响应 - 自定义消息
    @PostMapping
    public MoonCloudResponse<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return MoonCloudResponse.success("用户创建成功", created);
    }

    // 分页响应
    @GetMapping("/list")
    public MoonCloudResponse<List<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        IPage<User> pageResult = userService.page(new Page<>(page, size));

        return MoonCloudResponse.page(
            pageResult.getRecords(),
            pageResult.getCurrent(),
            pageResult.getSize(),
            pageResult.getTotal()
        );
    }

    // 错误响应
    @DeleteMapping("/{id}")
    public MoonCloudResponse<Void> deleteUser(@PathVariable Long id) {
        if (!userService.exists(id)) {
            return MoonCloudResponse.error(ResponseCode.USER_NOT_FOUND);
        }
        userService.delete(id);
        return MoonCloudResponse.success("用户删除成功");
    }
}
```

### 3. 响应码使用

#### 3.1 使用内置响应码

```java
// 成功响应码
MoonCloudResponse.success();                    // 200 - 操作成功
MoonCloudResponse.created(data);                // 201 - 创建成功

// 客户端错误响应码
MoonCloudResponse.badRequest("参数错误");        // 400 - 请求参数错误
MoonCloudResponse.unauthorized();               // 401 - 未认证
MoonCloudResponse.forbidden();                  // 403 - 无权限
MoonCloudResponse.notFound();                   // 404 - 资源不存在

// 服务端错误响应码
MoonCloudResponse.error();                      // 500 - 服务器内部错误
MoonCloudResponse.serviceUnavailable();         // 503 - 服务不可用

// 使用枚举响应码
MoonCloudResponse.error(ResponseCode.USER_NOT_FOUND);
MoonCloudResponse.error(ResponseCode.TOKEN_EXPIRED, "令牌已过期，请重新登录");
```

#### 3.2 响应码分类

响应码遵循以下规范：
- **200-299**：成功状态
- **400-499**：客户端错误
- **500-599**：服务端错误
- **1000-1999**：认证授权相关
- **2000-2999**：用户相关
- **3000-3999**：角色权限相关
- **4000-4999**：数据相关
- **5000-5999**：业务相关
- **6000-6999**：第三方服务相关
- **7000-7999**：系统相关
- **8000-8999**：参数校验相关
- **9000-9999**：网络相关

### 4. 异常处理

#### 4.1 抛出业务异常

```java
@Service
public class UserService {

    public User getUser(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            // 方式1：直接抛出异常
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 方式2：自定义消息
        if (user.isDeleted()) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND, "用户已被删除");
        }

        // 方式3：静态方法抛出
        BusinessException.throwIf(user.isLocked(), ResponseCode.ACCOUNT_LOCKED);

        return user;
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUser(userId);

        // 条件性抛出异常
        BusinessException.throwIf(
            !passwordEncoder.matches(oldPassword, user.getPassword()),
            ResponseCode.OLD_PASSWORD_ERROR,
            "原密码不正确"
        );

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
```

#### 4.2 全局异常自动处理

全局异常处理器会自动捕获并处理以下异常：

```java
// 参数校验异常
@PostMapping("/register")
public MoonCloudResponse<User> register(@Valid @RequestBody UserDTO userDTO) {
    // 如果参数校验失败，自动返回 400 错误
    return MoonCloudResponse.success(userService.register(userDTO));
}

// 其他异常自动处理
- MethodArgumentNotValidException    // 参数校验失败
- BindException                      // 参数绑定失败
- ConstraintViolationException       // 约束违反
- HttpRequestMethodNotSupportedException  // 请求方法不支持
- NoHandlerFoundException           // 404 错误
- MaxUploadSizeExceededException    // 文件大小超限
- SQLException                      // 数据库异常
- RuntimeException                  // 运行时异常
- Exception                         // 其他异常
```

### 5. 链式调用与扩展

```java
@GetMapping("/complex")
public MoonCloudResponse<Data> complexResponse() {
    Data data = service.getData();

    return MoonCloudResponse.success(data)
        .withTraceId(MDC.get("traceId"))      // 添加追踪ID
        .withExtra(Map.of("version", "1.0"))  // 添加扩展信息
        .withPageInfo(1, 10, 100);            // 添加分页信息
}
```

### 6. 分页响应

#### 6.1 使用 MyBatis Plus 分页

```java
@GetMapping("/page")
public MoonCloudResponse<List<User>> pageUsers(Page<User> page) {
    IPage<User> result = userService.page(page);

    // 自动转换为分页响应
    return ResponseAdapter.fromPage(result);
}
```

#### 6.2 手动构建分页

```java
@GetMapping("/custom-page")
public MoonCloudResponse<List<Data>> customPage(
        @RequestParam int current,
        @RequestParam int size) {

    List<Data> records = service.getPageData(current, size);
    long total = service.count();

    return MoonCloudResponse.page(records, current, size, total);
}
```

### 7. 兼容旧代码

使用 `ResponseAdapter` 适配器兼容旧的响应类：

```java
// 兼容旧的 Result 类
@GetMapping("/old-api")
public MoonCloudResponse<User> oldApi() {
    Result<User> oldResult = oldService.getUser();
    return ResponseAdapter.convert(oldResult);
}

// 快捷方法
@GetMapping("/quick")
public MoonCloudResponse<String> quick() {
    return ResponseAdapter.ok("成功");           // 等同于 MoonCloudResponse.success("成功")
    return ResponseAdapter.fail("失败");         // 等同于 MoonCloudResponse.error("失败")
}
```

### 8. 响应格式示例

#### 8.1 成功响应

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com"
    },
    "timestamp": 1704038400000,
    "time": "2024-01-01 00:00:00"
}
```

#### 8.2 分页响应

```json
{
    "code": 200,
    "message": "查询成功",
    "data": [
        {"id": 1, "name": "Item 1"},
        {"id": 2, "name": "Item 2"}
    ],
    "pageInfo": {
        "current": 1,
        "size": 10,
        "total": 100,
        "pages": 10,
        "hasPrevious": false,
        "hasNext": true
    },
    "timestamp": 1704038400000,
    "time": "2024-01-01 00:00:00"
}
```

#### 8.3 错误响应

```json
{
    "code": 2001,
    "message": "用户不存在",
    "data": null,
    "timestamp": 1704038400000,
    "time": "2024-01-01 00:00:00",
    "traceId": "uuid-12345"
}
```

#### 8.4 参数校验失败响应

```json
{
    "code": 8000,
    "message": "参数校验失败: username - 用户名不能为空; email - 邮箱格式不正确",
    "data": null,
    "extra": {
        "username": "用户名不能为空",
        "email": "邮箱格式不正确"
    },
    "timestamp": 1704038400000,
    "time": "2024-01-01 00:00:00",
    "traceId": "uuid-12345"
}
```

### 9. 配置自定义异常处理

如需自定义异常处理逻辑，可以扩展 `GlobalExceptionHandler`：

```java
@RestControllerAdvice
public class CustomExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(MyCustomException.class)
    public MoonCloudResponse<?> handleCustomException(
            MyCustomException e, HttpServletRequest request) {
        return MoonCloudResponse.error(e.getCode(), e.getMessage())
                .withTraceId(getTraceId(request));
    }
}
```

### 10. 最佳实践

#### 10.1 统一使用 MoonCloudResponse

```java
// ✅ 推荐
@GetMapping("/api/data")
public MoonCloudResponse<Data> getData() {
    return MoonCloudResponse.success(service.getData());
}

// ❌ 避免
@GetMapping("/api/data")
public Data getData() {
    return service.getData();  // 没有统一响应格式
}
```

#### 10.2 合理使用响应码

```java
// ✅ 使用具体的响应码
throw new BusinessException(ResponseCode.USER_NOT_FOUND);

// ❌ 使用通用响应码
throw new BusinessException(ResponseCode.BUSINESS_ERROR);
```

#### 10.3 提供有意义的错误消息

```java
// ✅ 清晰的错误信息
BusinessException.throwIf(
    balance < amount,
    ResponseCode.BUSINESS_ERROR,
    String.format("余额不足，当前余额: %.2f，需要: %.2f", balance, amount)
);

// ❌ 模糊的错误信息
BusinessException.throwIf(balance < amount, "操作失败");
```

#### 10.4 使用链式调用添加上下文

```java
// ✅ 添加追踪信息
return MoonCloudResponse.success(data)
    .withTraceId(MDC.get("traceId"))
    .withExtra(Map.of("requestTime", System.currentTimeMillis()));
```

## 迁移指南

### 从旧的 Result 类迁移

1. **替换返回类型**
```java
// 旧代码
public Result<User> getUser(Long id) {
    return Result.success(userService.getById(id));
}

// 新代码
public MoonCloudResponse<User> getUser(Long id) {
    return MoonCloudResponse.success(userService.getById(id));
}
```

2. **替换异常处理**
```java
// 旧代码
if (user == null) {
    return Result.error(ResultCode.USER_NOT_FOUND);
}

// 新代码
if (user == null) {
    throw new BusinessException(ResponseCode.USER_NOT_FOUND);
}
```

3. **使用适配器过渡**
```java
// 过渡期可以使用适配器
public MoonCloudResponse<User> getUser(Long id) {
    Result<User> oldResult = oldService.getUser(id);
    return ResponseAdapter.convert(oldResult);
}
```

## API 参考

### MoonCloudResponse 核心方法

| 方法 | 描述 |
|------|------|
| `success()` | 成功响应（无数据） |
| `success(T data)` | 成功响应（带数据） |
| `success(String message)` | 成功响应（自定义消息） |
| `success(String message, T data)` | 成功响应（自定义消息和数据） |
| `error()` | 失败响应（默认错误） |
| `error(String message)` | 失败响应（自定义消息） |
| `error(ResponseCode code)` | 失败响应（指定响应码） |
| `error(Integer code, String message)` | 失败响应（自定义码和消息） |
| `page(T data, long current, long size, long total)` | 分页响应 |
| `withTraceId(String traceId)` | 设置追踪ID |
| `withExtra(Object extra)` | 设置扩展信息 |
| `isSuccess()` | 判断是否成功 |
| `isError()` | 判断是否失败 |

### BusinessException 核心方法

| 方法 | 描述 |
|------|------|
| `throwException(String message)` | 抛出业务异常 |
| `throwException(ResponseCode code)` | 抛出业务异常（响应码） |
| `throwIf(boolean condition, String message)` | 条件性抛出异常 |
| `throwIf(boolean condition, ResponseCode code)` | 条件性抛出异常（响应码） |

## License

MIT License