# Moon Spring Boot Starter Validator

## 概述

Moon Spring Boot Starter Validator 是一个基于 Spring Boot 3.x 的验证器扩展模块，提供了丰富的自定义验证注解，专门针对中国本地化业务场景进行优化。

## 功能特性

### 核心优势

- ✅ **Spring Boot 3.x 自动配置**：基于最新的 Spring Boot 3.x 自动配置机制，无需手动配置
- ✅ **完整的验证逻辑**：不仅验证格式，还验证逻辑正确性（如身份证校验码、银行卡Luhn算法）
- ✅ **中国本地化优化**：支持最新的手机号段、身份证规则、中文域名等
- ✅ **高性能设计**：使用预编译正则表达式，单例模式，线程安全
- ✅ **灵活可扩展**：支持参数配置，易于扩展新的验证器

### 已实现的验证器

1. **身份信息验证**
   - `@Mobile` - 手机号验证（支持所有运营商最新号段，包括中国广电192）
   - `@IdCard` - 身份证号验证（包含地区码、出生日期、校验码完整验证）
   - `@Email` - 邮箱地址验证（支持中文域名、子域名配置）

2. **金融支付验证**
   - `@BankCard` - 银行卡号验证（Luhn算法校验）
   - `@CVV` - CVV验证码验证（3-4位数字）

3. **网络地址验证**
   - `@Url` - URL地址验证
   - `@IpAddress` - IP地址验证（支持IPv4和IPv6）

4. **其他常用验证**
   - `@ChineseNickname` - 中文昵称验证
   - `@PostCode` - 邮政编码验证（6位数字）
   - `@ISBN` - ISBN号验证（支持ISBN-10和ISBN-13）
   - `@BloodType` - 血型验证（A、B、AB、O型，支持Rh因子）
   - `@Password` - 密码强度验证

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.mooncloud</groupId>
    <artifactId>moon-spring-boot-starter-validator</artifactId>
    <version>${version}</version>
</dependency>
```

### 2. 使用示例

#### 基础使用

```java
import com.moon.cloud.validator.mobile.Mobile;
import com.moon.cloud.validator.email.Email;
import com.moon.cloud.validator.password.Password;

public class UserDTO {

    @Mobile(message = "请输入正确的手机号")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Password(strength = Password.PasswordStrength.STRONG,
              message = "密码必须包含大小写字母、数字和特殊字符")
    private String password;

    // getters and setters
}
```

#### Controller中使用

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        // 验证通过后的业务逻辑
        return ResponseEntity.ok("注册成功");
    }
}
```

## 详细文档

### @Mobile - 手机号验证

支持中国大陆所有运营商的手机号验证，包括：
- 中国移动
- 中国联通
- 中国电信
- 中国广电
- 虚拟运营商

```java
@Mobile
private String phone;

// 支持带国家码
// +8613812345678
// 8613812345678
// 13812345678
```

### @Email - 邮箱验证

```java
@Email(
    allowSubdomain = true,  // 是否允许子域名
    allowChinese = false    // 是否允许中文域名
)
private String email;
```

### @Password - 密码强度验证

```java
@Password(
    minLength = 8,
    maxLength = 32,
    requireUppercase = true,
    requireLowercase = true,
    requireDigit = true,
    requireSpecialChar = true,
    specialChars = "!@#$%^&*",
    strength = Password.PasswordStrength.STRONG
)
private String password;
```

密码强度等级：
- `WEAK`: 最少6位，至少包含字母或数字
- `MEDIUM`: 最少8位，必须包含大小写字母和数字
- `STRONG`: 最少10位，必须包含大小写字母、数字和特殊字符

### @Username - 用户名验证

```java
@Username(
    minLength = 3,
    maxLength = 20,
    allowChinese = false,
    allowSpecialChar = false,
    specialChars = "_-",
    mustStartWithLetter = true,
    reservedNames = {"admin", "root", "system"}
)
private String username;
```

### @Url - URL验证

```java
@Url(
    protocols = {"http", "https"},
    allowLocal = false,
    requirePort = false
)
private String website;
```

### @MacAddress - MAC地址验证

```java
@MacAddress(
    separator = ":",           // 分隔符 : 或 -
    allowNoSeparator = false  // 是否允许无分隔符格式
)
private String macAddress;
```

### @PlateNumber - 车牌号验证

支持的车牌类型：
- 普通车牌
- 新能源车牌
- 警车车牌
- 军车车牌

```java
@PlateNumber(
    type = PlateNumber.PlateType.ALL
)
private String plateNumber;
```

### @CreditCode - 统一社会信用代码验证

验证18位统一社会信用代码，包含校验码验证。

```java
@CreditCode
private String creditCode;
```

### @Telephone - 固定电话验证

支持中国固定电话格式，包括区号、分机号等。

```java
@Telephone(
    requireAreaCode = false,  // 是否必须包含区号
    allowExtension = true     // 是否允许分机号
)
private String telephone;
```

### @QQNumber - QQ号验证

验证5-11位QQ号，首位不能为0。

```java
@QQNumber
private String qqNumber;
```

### @WeChatId - 微信号验证

验证6-20位微信号，必须以字母开头，可包含字母、数字、下划线和减号。

```java
@WeChatId
private String weChatId;
```

## 配置选项

在 `application.yml` 中配置：

```yaml
moon:
  validator:
    enabled: true      # 是否启用验证器，默认true
    enhanced: false    # 是否启用增强功能，默认false
```

## 自定义验证器

如需创建自定义验证器，参考以下步骤：

1. 创建注解

```java
@Documented
@Constraint(validatedBy = CustomValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Custom {
    String message() default "验证失败";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

2. 实现验证器

```java
public class CustomValidator implements ConstraintValidator<Custom, String> {

    @Override
    public void initialize(Custom constraintAnnotation) {
        // 初始化逻辑
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true; // 空值交给@NotNull处理
        }
        // 验证逻辑
        return true;
    }
}
```

## 错误处理

建议配合全局异常处理器使用：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
}
```

## 性能考虑

- 所有验证器都使用预编译的正则表达式，提高验证性能
- 空值默认通过验证，配合 `@NotNull` 或 `@NotEmpty` 使用
- 验证器实例为单例，线程安全

## 版本兼容性

- Spring Boot 3.4.1+
- Java 21+
- Jakarta Validation API 3.0+

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License