package com.moon.cloud.validator.wechat;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 微信号验证注解
 */
@Documented
@Constraint(validatedBy = WeChatIdValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface WeChatId {

    String message() default "微信号格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}