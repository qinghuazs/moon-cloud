package com.moon.cloud.validator.enumvalue;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 枚举值验证注解
 * 验证提供的值是否在指定的枚举类中定义
 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface EnumValue {

    String message() default "提供的值不在指定的枚举类中";

    /**
     * 指定枚举类
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 是否忽略大小写，默认false
     */
    boolean ignoreCase() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}