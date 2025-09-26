# Moon Spring Boot Starter Captcha

通用验证码生成、存储和验证组件，提供多种验证码类型和灵活的存储方案。

## 功能特性

### 验证码类型
- **数字验证码** - 纯数字（如：123456）
- **字母验证码** - 纯字母（如：ABCDEF）
- **混合验证码** - 字母+数字，排除易混淆字符（如：A2B4C6）
- **算术验证码** - 简单算术题（如：3+5=?）
- **图形验证码** - 图片验证码（开发中）
- **滑块验证码** - 滑动拼图验证（开发中）

### 存储方式
- **内存存储** - 适用于单机应用
- **Redis存储** - 适用于分布式应用
- **Caffeine缓存** - 高性能本地缓存（开发中）

### 安全特性
- ✅ 防暴力破解（失败次数限制）
- ✅ 防重放攻击（一次性使用）
- ✅ 防猜测（SecureRandom生成）
- ✅ IP限流和账号锁定
- ✅ 自定义过期时间
- ✅ 大小写敏感控制

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.mooncloud</groupId>
    <artifactId>moon-spring-boot-starter-captcha</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置文件

```yaml
moon:
  captcha:
    enabled: true                 # 是否启用
    type: MIXED                   # 验证码类型：DIGIT, ALPHA, MIXED, MATH
    length: 6                     # 验证码长度
    expire-time: 300              # 有效期（秒）
    storage: REDIS                # 存储类型：MEMORY, REDIS

    # Redis配置
    redis:
      key-prefix: "captcha:"      # 键前缀
      failure-prefix: "captcha:fail:"  # 失败记录键前缀

    # 安全配置
    security:
      max-retry: 5                # 最大重试次数
      lock-time: 1800             # 锁定时间（秒）
      case-sensitive: false       # 是否大小写敏感
      allow-reuse: false          # 是否允许重复使用

    # 算术验证码配置
    math:
      operators: "+-"             # 运算符：+,-,*,/
      min-number: 1               # 最小数字
      max-number: 10              # 最大数字
      show-equation: true         # 是否显示等式
```

### 3. 使用示例

#### 3.1 基本使用

```java
@Service
public class UserService {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 发送验证码
     */
    public void sendCode(String email) {
        // 生成验证码
        Captcha captcha = captchaService.generate();

        // 保存验证码（默认5分钟有效）
        captchaService.save(email, captcha);

        // 发送邮件/短信
        emailService.send(email, captcha.getCode());
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String email, String code) {
        return captchaService.validate(email, code);
    }
}
```

#### 3.2 指定验证码类型

```java
// 生成6位数字验证码
Captcha digitCaptcha = captchaService.generate(CaptchaType.DIGIT);

// 生成8位混合验证码
Captcha mixedCaptcha = captchaService.generate(CaptchaType.MIXED, 8);

// 生成算术验证码
Captcha mathCaptcha = captchaService.generate(CaptchaType.MATH);
System.out.println("题目: " + mathCaptcha.getCode());    // "3 + 5 = ?"
System.out.println("答案: " + mathCaptcha.getAnswer());  // "8"
```

#### 3.3 自定义存储时间

```java
// 保存验证码，10分钟有效
captchaService.save(key, captcha, 10, TimeUnit.MINUTES);
```

#### 3.4 安全特性使用

```java
// 检查是否被锁定
if (captchaService.isLocked(email)) {
    throw new RuntimeException("验证失败次数过多，请稍后重试");
}

// 获取失败次数
int failCount = captchaService.getFailureCount(email);
if (failCount >= 3) {
    // 需要额外验证
}

// 验证验证码（忽略大小写）
boolean valid = captchaService.validate(email, code, false);
```

### 4. 在 Controller 中使用

```java
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取验证码
     */
    @GetMapping("/generate")
    public Map<String, Object> generate(HttpSession session) {
        Captcha captcha = captchaService.generate();

        // 保存到session或Redis
        String key = session.getId();
        captchaService.save(key, captcha);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaId", captcha.getId());
        result.put("captcha", captcha.getCode());  // 算术题或图片

        return result;
    }

    /**
     * 验证验证码
     */
    @PostMapping("/validate")
    public boolean validate(@RequestParam String code, HttpSession session) {
        String key = session.getId();
        return captchaService.validate(key, code);
    }
}
```

### 5. 整合到用户认证流程

```java
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private CaptchaService captchaService;

    @Override
    public boolean sendPasswordResetCode(String email) {
        // 检查是否被锁定
        if (captchaService.isLocked(email)) {
            throw new RuntimeException("验证失败次数过多，请30分钟后重试");
        }

        // 生成6位数字验证码
        Captcha captcha = captchaService.generate(CaptchaType.DIGIT, 6);

        // 保存验证码，10分钟有效
        captchaService.save(email, captcha, 10, TimeUnit.MINUTES);

        // 发送邮件
        emailService.sendVerificationCode(email, captcha.getCode());

        return true;
    }

    @Override
    public boolean resetPassword(String email, String code, String newPassword) {
        // 验证验证码
        if (!captchaService.validate(email, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 重置密码逻辑
        // ...

        // 删除验证码
        captchaService.remove(email);

        return true;
    }
}
```

## 高级配置

### 自定义验证码生成器

```java
@Component
public class CustomCaptchaGenerator extends AbstractCaptchaGenerator {

    @Override
    protected String generateCode(CaptchaProperties properties) {
        // 自定义生成逻辑
        return "CUSTOM";
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.CUSTOM;  // 自定义类型
    }
}
```

### 自定义存储实现

```java
@Component
public class MongoCaptchaStorage implements CaptchaStorage {

    @Override
    public void save(String key, Captcha captcha, long timeout, TimeUnit unit) {
        // MongoDB存储逻辑
    }

    // 实现其他方法...
}
```

### 自定义验证器

```java
@Component
public class CustomCaptchaValidator implements CaptchaValidator {

    @Override
    public boolean validate(String key, String code) {
        // 自定义验证逻辑
        return true;
    }

    // 实现其他方法...
}
```

## API 文档

### CaptchaService 接口

| 方法 | 说明 |
|------|------|
| `generate()` | 生成默认类型验证码 |
| `generate(CaptchaType type)` | 生成指定类型验证码 |
| `generate(int length)` | 生成指定长度验证码 |
| `generate(CaptchaType type, int length)` | 生成指定类型和长度验证码 |
| `save(String key, Captcha captcha)` | 保存验证码 |
| `save(String key, Captcha captcha, long timeout, TimeUnit unit)` | 保存验证码（自定义过期时间） |
| `validate(String key, String code)` | 验证验证码 |
| `validate(String key, String code, boolean caseSensitive)` | 验证验证码（控制大小写） |
| `get(String key)` | 获取验证码对象 |
| `remove(String key)` | 删除验证码 |
| `isLocked(String key)` | 检查是否被锁定 |
| `getFailureCount(String key)` | 获取失败次数 |

### Captcha 实体

| 属性 | 类型 | 说明 |
|------|------|------|
| id | String | 验证码唯一标识 |
| code | String | 验证码内容 |
| answer | String | 验证码答案（用于算术验证码） |
| type | CaptchaType | 验证码类型 |
| image | String | 图片Base64（用于图形验证码） |
| createTime | LocalDateTime | 创建时间 |
| expireTime | LocalDateTime | 过期时间 |
| failCount | Integer | 失败次数 |
| used | Boolean | 是否已使用 |

## 性能优化

### 内存存储优化
- 定时清理过期验证码
- 使用 ConcurrentHashMap 保证线程安全

### Redis存储优化
- 使用 Pipeline 批量操作
- 合理设置过期时间
- 使用连接池

### 生成器优化
- 使用 SecureRandom 保证安全性
- 预生成字符集，避免重复计算
- 排除易混淆字符

## 常见问题

### 1. 验证码总是验证失败？
检查以下几点：
- 验证码是否过期
- 大小写是否匹配（如果开启了大小写敏感）
- 存储的键是否正确
- Redis连接是否正常

### 2. 如何防止验证码被暴力破解？
- 设置合理的 `max-retry` 值（建议3-5次）
- 设置足够长的 `lock-time`（建议30分钟）
- 使用足够复杂的验证码类型
- 配合IP限流使用

### 3. 分布式环境下如何使用？
- 使用 Redis 存储（`storage: REDIS`）
- 确保所有节点连接同一个 Redis
- 注意时钟同步问题

### 4. 如何自定义验证码长度？
```java
// 方法1：配置文件
moon.captcha.length=8

// 方法2：代码指定
captchaService.generate(8);
```

## 版本历史

### v1.0.0
- 初始版本发布
- 支持数字、字母、混合、算术验证码
- 支持内存和Redis存储
- 完整的安全机制

### 开发中功能
- 图形验证码（带干扰线、噪点）
- 滑块验证码
- Caffeine缓存存储
- 注解驱动验证（@ValidateCaptcha）
- 验证码统计分析

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

Apache License 2.0