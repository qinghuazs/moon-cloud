package com.moon.cloud.validator.mac;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * MAC地址验证注解
 */
@Documented
@Constraint(validatedBy = MacAddressValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface MacAddress {

    String message() default "MAC地址格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * MAC地址分隔符，支持 : 或 -
     */
    String separator() default ":";

    /**
     * 是否允许无分隔符格式
     */
    boolean allowNoSeparator() default false;
}