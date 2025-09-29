package com.moon.cloud.validator.telephone;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 固定电话验证注解（中国）
 * 支持带区号和不带区号的格式
 */
@Documented
@Constraint(validatedBy = TelephoneValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Telephone {

    String message() default "固定电话格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 是否必须包含区号
     */
    boolean requireAreaCode() default false;

    /**
     * 是否允许分机号
     */
    boolean allowExtension() default true;
}