package com.moon.cloud.validator.username;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用户名验证注解
 */
@Documented
@Constraint(validatedBy = UsernameValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Username {

    String message() default "用户名格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 最小长度
     */
    int minLength() default 3;

    /**
     * 最大长度
     */
    int maxLength() default 20;

    /**
     * 是否允许中文
     */
    boolean allowChinese() default false;

    /**
     * 是否允许特殊字符
     */
    boolean allowSpecialChar() default false;

    /**
     * 允许的特殊字符
     */
    String specialChars() default "_-";

    /**
     * 是否必须以字母开头
     */
    boolean mustStartWithLetter() default true;

    /**
     * 保留用户名列表（不允许使用的用户名）
     */
    String[] reservedNames() default {"admin", "root", "administrator", "system", "test"};
}