package com.moon.cloud.user.annotation;

import java.lang.annotation.*;

/**
 * 角色检查注解
 * 用于方法级别的角色控制
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 角色编码
     * 支持多个角色编码，满足其中一个即可
     */
    String[] value() default {};

    /**
     * 角色编码（别名）
     */
    String[] roles() default {};

    /**
     * 逻辑关系：AND-需要同时拥有所有角色，OR-拥有其中一个角色即可
     */
    Logical logical() default Logical.OR;

    /**
     * 逻辑关系枚举
     */
    enum Logical {
        /**
         * 或关系：拥有其中一个角色即可
         */
        OR,
        /**
         * 与关系：需要同时拥有所有角色
         */
        AND
    }
}