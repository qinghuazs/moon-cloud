package com.moon.cloud.validator.email;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 邮箱地址验证注解
 * 支持标准邮箱格式验证
 */
@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Email {

    String message() default "邮箱地址格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 是否允许子域名邮箱
     */
    boolean allowSubdomain() default true;

    /**
     * 是否允许中文域名
     */
    boolean allowChinese() default false;
}