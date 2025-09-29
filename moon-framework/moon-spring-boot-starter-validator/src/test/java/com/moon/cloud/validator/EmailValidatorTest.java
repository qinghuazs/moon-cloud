package com.moon.cloud.validator;

import com.moon.cloud.validator.email.Email;
import com.moon.cloud.validator.email.EmailValidator;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邮箱验证器测试类
 */
@DisplayName("邮箱验证器测试")
class EmailValidatorTest {

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    @Test
    @DisplayName("验证空值应返回true")
    void testNullValue() {
        Email annotation = createEmailAnnotation(true, false);
        validator.initialize(annotation);
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @Test
    @DisplayName("验证标准邮箱格式")
    void testStandardEmailFormat() {
        Email annotation = createEmailAnnotation(true, false);
        validator.initialize(annotation);

        // 有效的邮箱
        assertTrue(validator.isValid("user@example.com", null));
        assertTrue(validator.isValid("user.name@example.com", null));
        assertTrue(validator.isValid("user+tag@example.com", null));
        assertTrue(validator.isValid("user_123@example.com", null));
        assertTrue(validator.isValid("123user@example.com", null));
        assertTrue(validator.isValid("user@sub.example.com", null));
        assertTrue(validator.isValid("user@example.co.uk", null));
    }

    @Test
    @DisplayName("验证无效的邮箱格式")
    void testInvalidEmailFormat() {
        Email annotation = createEmailAnnotation(true, false);
        validator.initialize(annotation);

        // 无效的邮箱
        assertFalse(validator.isValid("plainaddress", null));
        assertFalse(validator.isValid("@missinglocal.com", null));
        assertFalse(validator.isValid("missing@domain", null));
        assertFalse(validator.isValid("missing.domain@.com", null));
        assertFalse(validator.isValid("two@@example.com", null));
        assertFalse(validator.isValid("dotdot..@example.com", null));
        assertFalse(validator.isValid("user@", null));
        assertFalse(validator.isValid("@example.com", null));
        assertFalse(validator.isValid("user name@example.com", null));
        assertFalse(validator.isValid("user@exam ple.com", null));
    }

    @Test
    @DisplayName("验证子域名邮箱")
    void testSubdomainEmail() {
        // 允许子域名
        Email allowSubdomain = createEmailAnnotation(true, false);
        validator.initialize(allowSubdomain);
        assertTrue(validator.isValid("user@mail.example.com", null));
        assertTrue(validator.isValid("user@deep.sub.example.com", null));

        // 不允许子域名
        Email noSubdomain = createEmailAnnotation(false, false);
        validator.initialize(noSubdomain);
        assertFalse(validator.isValid("user@mail.example.com", null));
        assertFalse(validator.isValid("user@deep.sub.example.com", null));
        assertTrue(validator.isValid("user@example.com", null));
    }

    @Test
    @DisplayName("验证中文域名邮箱")
    void testChineseDomainEmail() {
        // 允许中文域名
        Email allowChinese = createEmailAnnotation(true, true);
        validator.initialize(allowChinese);
        assertTrue(validator.isValid("用户@示例.中国", null));
        assertTrue(validator.isValid("测试@公司.中文", null));
        assertTrue(validator.isValid("user@中文.com", null));
        assertTrue(validator.isValid("user@example.中国", null));

        // 不允许中文域名
        Email noChinese = createEmailAnnotation(true, false);
        validator.initialize(noChinese);
        assertFalse(validator.isValid("用户@示例.中国", null));
        assertFalse(validator.isValid("user@中文.com", null));
    }

    @Test
    @DisplayName("验证特殊字符邮箱")
    void testSpecialCharacterEmail() {
        Email annotation = createEmailAnnotation(true, false);
        validator.initialize(annotation);

        // 允许的特殊字符
        assertTrue(validator.isValid("user+tag@example.com", null));
        assertTrue(validator.isValid("user_name@example.com", null));
        assertTrue(validator.isValid("user-name@example.com", null));
        assertTrue(validator.isValid("user.name@example.com", null));
        assertTrue(validator.isValid("user&name@example.com", null));
        assertTrue(validator.isValid("user*name@example.com", null));
    }

    @Test
    @DisplayName("验证各种顶级域名")
    void testVariousTLDs() {
        Email annotation = createEmailAnnotation(true, false);
        validator.initialize(annotation);

        assertTrue(validator.isValid("user@example.com", null));
        assertTrue(validator.isValid("user@example.org", null));
        assertTrue(validator.isValid("user@example.net", null));
        assertTrue(validator.isValid("user@example.edu", null));
        assertTrue(validator.isValid("user@example.gov", null));
        assertTrue(validator.isValid("user@example.io", null));
        assertTrue(validator.isValid("user@example.tech", null));
        assertTrue(validator.isValid("user@example.xyz", null));
        assertTrue(validator.isValid("user@example.co", null));
        assertTrue(validator.isValid("user@example.uk", null));
    }

    /**
     * 创建 Email 注解的模拟实例
     */
    private Email createEmailAnnotation(boolean allowSubdomain, boolean allowChinese) {
        return new Email() {
            @Override
            public String message() {
                return "邮箱地址格式不正确";
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public boolean allowSubdomain() {
                return allowSubdomain;
            }

            @Override
            public boolean allowChinese() {
                return allowChinese;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Email.class;
            }
        };
    }
}