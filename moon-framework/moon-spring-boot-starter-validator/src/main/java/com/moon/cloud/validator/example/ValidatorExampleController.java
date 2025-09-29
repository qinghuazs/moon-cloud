package com.moon.cloud.validator.example;

import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 验证器示例控制器
 * 演示如何在Spring Boot应用中使用自定义验证器
 */
@RestController
@RequestMapping("/api/validator/example")
@ConditionalOnWebApplication
public class ValidatorExampleController {

    /**
     * 验证用户信息
     *
     * @param userDTO 用户信息
     * @param bindingResult 验证结果
     * @return 验证结果
     */
    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existing, replacement) -> existing
                    ));
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "验证失败",
                    "errors", errors
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "验证通过",
                "data", userDTO
        ));
    }

    /**
     * 验证单个手机号
     *
     * @param mobile 手机号
     * @return 验证结果
     */
    @GetMapping("/validate-mobile")
    public ResponseEntity<?> validateMobile(@RequestParam String mobile) {
        com.moon.cloud.validator.mobile.MobileValidator validator = new com.moon.cloud.validator.mobile.MobileValidator();
        boolean isValid = validator.isValid(mobile, null);

        Map<String, Object> result = new HashMap<>();
        result.put("mobile", mobile);
        result.put("isValid", isValid);

        if (isValid) {
            result.put("carrier", com.moon.cloud.validator.mobile.MobileValidator.getCarrier(mobile));
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 验证身份证号
     *
     * @param idCard 身份证号
     * @return 验证结果
     */
    @GetMapping("/validate-idcard")
    public ResponseEntity<?> validateIdCard(@RequestParam String idCard) {
        com.moon.cloud.validator.idcard.IdCardValidator validator = new com.moon.cloud.validator.idcard.IdCardValidator();
        boolean isValid = validator.isValid(idCard, null);

        Map<String, Object> result = new HashMap<>();
        result.put("idCard", idCard);
        result.put("isValid", isValid);

        if (isValid && idCard.length() == 18) {
            // 解析身份证信息
            String birthYear = idCard.substring(6, 10);
            String birthMonth = idCard.substring(10, 12);
            String birthDay = idCard.substring(12, 14);
            int genderCode = Integer.parseInt(idCard.substring(16, 17));

            result.put("birthDate", birthYear + "-" + birthMonth + "-" + birthDay);
            result.put("gender", genderCode % 2 == 1 ? "男" : "女");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 验证邮箱地址
     *
     * @param email 邮箱地址
     * @param allowChinese 是否允许中文域名
     * @return 验证结果
     */
    @GetMapping("/validate-email")
    public ResponseEntity<?> validateEmail(@RequestParam String email,
                                          @RequestParam(defaultValue = "false") boolean allowChinese) {
        com.moon.cloud.validator.email.EmailValidator validator = new com.moon.cloud.validator.email.EmailValidator();

        // 创建一个模拟的注解实例用于初始化验证器
        com.moon.cloud.validator.email.Email emailAnnotation = new com.moon.cloud.validator.email.Email() {
            @Override
            public String message() {
                return "邮箱地址格式不正确";
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @Override
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public boolean allowSubdomain() {
                return true;
            }

            @Override
            public boolean allowChinese() {
                return allowChinese;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return com.moon.cloud.validator.email.Email.class;
            }
        };

        validator.initialize(emailAnnotation);
        boolean isValid = validator.isValid(email, null);

        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("isValid", isValid);
        result.put("allowChinese", allowChinese);

        return ResponseEntity.ok(result);
    }
}