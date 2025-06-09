package com.moon.cloud.validator.isbn;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * ISBN号验证注解
 */
@Documented
@Constraint(validatedBy = ISBNValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface ISBN {

    String message() default "ISBN号格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}