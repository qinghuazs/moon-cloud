package com.moon.cloud.validator.url;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * URL地址验证注解
 */
@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Url {

    String message() default "URL地址格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 允许的协议，默认http和https
     */
    String[] protocols() default {"http", "https"};

    /**
     * 是否允许本地地址（localhost, 127.0.0.1等）
     */
    boolean allowLocal() default false;

    /**
     * 是否必须包含端口号
     */
    boolean requirePort() default false;
}