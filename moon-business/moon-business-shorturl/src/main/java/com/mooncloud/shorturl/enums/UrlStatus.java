package com.mooncloud.shorturl.enums;

/**
 * URL状态枚举
 * 
 * @author mooncloud
 */
public enum UrlStatus {
    /**
     * 活跃状态
     */
    ACTIVE("活跃"),
    
    /**
     * 已过期
     */
    EXPIRED("已过期"),
    
    /**
     * 已禁用
     */
    DISABLED("已禁用"),
    
    /**
     * 已删除
     */
    DELETED("已删除");
    
    private final String description;
    
    UrlStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}