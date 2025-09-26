package com.moon.cloud.captcha.enums;

import lombok.Getter;

/**
 * 存储类型枚举
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Getter
public enum StorageType {

    /**
     * 内存存储
     */
    MEMORY("MEMORY", "内存存储"),

    /**
     * Redis存储
     */
    REDIS("REDIS", "Redis存储"),

    /**
     * Caffeine缓存存储
     */
    CAFFEINE("CAFFEINE", "Caffeine缓存存储");

    private final String code;
    private final String description;

    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}