package com.moon.cloud.validator.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 密码强度验证注解
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Password {

    String message() default "密码不符合安全要求";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 最小长度
     */
    int minLength() default 8;

    /**
     * 最大长度
     */
    int maxLength() default 32;

    /**
     * 是否必须包含大写字母
     */
    boolean requireUppercase() default true;

    /**
     * 是否必须包含小写字母
     */
    boolean requireLowercase() default true;

    /**
     * 是否必须包含数字
     */
    boolean requireDigit() default true;

    /**
     * 是否必须包含特殊字符
     */
    boolean requireSpecialChar() default false;

    /**
     * 允许的特殊字符
     */
    String specialChars() default "!@#$%^&*()_+-=[]{}|;:'\",.<>?/`~";

    /**
     * 密码强度等级
     */
    PasswordStrength strength() default PasswordStrength.MEDIUM;

    enum PasswordStrength {
        /**
         * 弱密码：最少6位，至少包含字母或数字
         */
        WEAK,
        /**
         * 中等强度：最少8位，必须包含大小写字母和数字
         */
        MEDIUM,
        /**
         * 强密码：最少10位，必须包含大小写字母、数字和特殊字符
         */
        STRONG
    }
}