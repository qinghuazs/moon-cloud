package com.moon.cloud.user.annotation;

import java.lang.annotation.*;

/**
 * 权限检查注解
 * 用于方法级别的权限控制
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限编码
     * 支持多个权限编码，满足其中一个即可
     */
    String[] value() default {};

    /**
     * 权限编码（别名）
     */
    String[] permissions() default {};

    /**
     * 逻辑关系：AND-需要同时拥有所有权限，OR-拥有其中一个权限即可
     */
    Logical logical() default Logical.OR;

    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 或关系：拥有其中一个权限即可
         */
        OR,
        /**
         * 与关系：需要同时拥有所有权限
         */
        AND
    }
}