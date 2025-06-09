package com.moon.cloud.validator.idcard;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 身份证号验证注解
 */
@Documented
@Constraint(validatedBy = IdCardValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface IdCard {

    String message() default "身份证号格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}