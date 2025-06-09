package com.moon.cloud.validator.nickname;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 中文昵称验证注解（支持中文、字母、数字、下划线）
 */
@Documented
@Constraint(validatedBy = ChineseNicknameValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface ChineseNickname {

    String message() default "昵称格式不正确，长度应为2-16位，支持中文、字母、数字、下划线";

    int min() default 2;
    
    int max() default 16;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}