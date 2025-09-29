package com.moon.cloud.validator.plate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 车牌号验证注解
 * 支持普通车牌、新能源车牌等
 */
@Documented
@Constraint(validatedBy = PlateNumberValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface PlateNumber {

    String message() default "车牌号格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 车牌类型
     */
    PlateType type() default PlateType.ALL;

    enum PlateType {
        /**
         * 所有类型
         */
        ALL,
        /**
         * 普通车牌
         */
        NORMAL,
        /**
         * 新能源车牌
         */
        NEW_ENERGY
    }
}