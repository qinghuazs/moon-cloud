# Validator 使用示例

本文档展示了如何使用 moon-spring-boot-starter-validator 中的各种验证器。

## 基础信息验证

### 手机号验证
```java
import com.moon.cloud.validator.mobile.Mobile;

public class UserDto {
    @Mobile
    @NotBlank(message = "手机号不能为空")
    private String mobile;
}
```

### 身份证号验证
```java
import com.moon.cloud.validator.idcard.IdCard;

public class UserDto {
    @IdCard
    @NotBlank(message = "身份证号不能为空")
    private String idCard;
}
```

### IP地址验证
```java
import com.moon.cloud.validator.ip.IpAddress;

public class ServerDto {
    @IpAddress
    @NotBlank(message = "IP地址不能为空")
    private String ipAddress;
}
```

### 邮政编码验证
```java
import com.moon.cloud.validator.postcode.PostCode;

public class AddressDto {
    @PostCode
    private String postCode;
}
```

## 金融相关验证

### 银行卡号验证
```java
import com.moon.cloud.validator.bankcard.BankCard;

public class PaymentDto {
    @BankCard
    @NotBlank(message = "银行卡号不能为空")
    private String bankCardNumber;
}
```

### CVV码验证
```java
import com.moon.cloud.validator.cvv.CVV;

public class PaymentDto {
    @CVV
    @NotBlank(message = "CVV码不能为空")
    private String cvv;
}
```

## 医疗相关验证

### 血型验证
```java
import com.moon.cloud.validator.bloodtype.BloodType;

public class PatientDto {
    @BloodType
    private String bloodType; // 如: A+, B-, AB+, O-
}
```

## 教育相关验证

### ISBN号验证
```java
import com.moon.cloud.validator.isbn.ISBN;

public class BookDto {
    @ISBN
    @NotBlank(message = "ISBN号不能为空")
    private String isbn;
}
```

## 用户信息验证

### 中文昵称验证
```java
import com.moon.cloud.validator.nickname.ChineseNickname;

public class UserDto {
    @ChineseNickname(min = 2, max = 16)
    @NotBlank(message = "昵称不能为空")
    private String nickname; // 支持中文、字母、数字、下划线
}
```

### 枚举值验证
```java
import com.moon.cloud.validator.enumvalue.EnumValue;

// 定义枚举类
public enum UserStatus {
    ACTIVE, INACTIVE, PENDING, SUSPENDED
}

public enum Gender {
    MALE, FEMALE, OTHER
}

public class UserDto {
    @EnumValue(enumClass = UserStatus.class)
    @NotBlank(message = "用户状态不能为空")
    private String status; // 必须是 ACTIVE, INACTIVE, PENDING, SUSPENDED 之一
    
    @EnumValue(enumClass = Gender.class, ignoreCase = true)
    private String gender; // 支持忽略大小写，如 male, MALE, Male 都可以
}
```

## 完整示例

```java
import com.moon.cloud.validator.mobile.Mobile;
import com.moon.cloud.validator.idcard.IdCard;
import com.moon.cloud.validator.bankcard.BankCard;
import com.moon.cloud.validator.nickname.ChineseNickname;
import com.moon.cloud.validator.enumvalue.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public enum UserType {
    INDIVIDUAL, ENTERPRISE, ADMIN, GUEST
}

public class UserRegistrationDto {
    
    @ChineseNickname(min = 2, max = 16)
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    
    @Mobile
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    
    @IdCard
    @NotBlank(message = "身份证号不能为空")
    private String idCard;
    
    @Email
    @NotBlank(message = "邮箱不能为空")
    private String email;
    
    @BankCard
    private String bankCard; // 可选字段
    
    @EnumValue(enumClass = UserType.class, ignoreCase = true)
    @NotBlank(message = "用户类型不能为空")
    private String userType; // 必须是 UserType 枚举中的值之一
    
    // getter and setter methods...
}
```

## 控制器中的使用

```java
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDto userDto) {
        // 如果验证失败，Spring Boot会自动返回400错误和验证失败信息
        // 验证成功的业务逻辑
        return ResponseEntity.ok("注册成功");
    }
}
```

## 注意事项

1. 所有验证器在字段为空时都会返回 `true`，空值验证请使用 `@NotNull`、`@NotBlank` 或 `@NotEmpty`
2. 可以组合使用多个验证注解
3. 验证失败时会返回注解中定义的默认错误消息
4. 可以通过 `message` 属性自定义错误消息
5. `@EnumValue` 验证器会在验证失败时自动显示所有可用的枚举值，便于调试
6. `@EnumValue` 支持 `ignoreCase` 参数来忽略大小写匹配

## 自定义错误消息示例

```java
public class UserDto {
    @Mobile(message = "请输入正确的手机号格式")
    private String mobile;
    
    @IdCard(message = "身份证号格式错误，请检查后重新输入")
    private String idCard;
    
    @EnumValue(enumClass = UserStatus.class, message = "用户状态无效，请选择正确的状态")
    private String status;
}
```