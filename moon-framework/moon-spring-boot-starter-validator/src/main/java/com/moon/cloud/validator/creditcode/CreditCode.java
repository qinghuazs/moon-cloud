package com.moon.cloud.validator.creditcode;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 统一社会信用代码验证注解
 * 18位统一社会信用代码验证
 */
@Documented
@Constraint(validatedBy = CreditCodeValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface CreditCode {

    String message() default "统一社会信用代码格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}