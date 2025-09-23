package com.mooncloud.shorturl.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.mooncloud.shorturl.enums.UrlStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * URL映射实体类
 * 
 * 分区策略：按创建时间（月）进行分区
 * - 分区键：YEAR(created_at) * 100 + MONTH(created_at)
 * - 分区类型：RANGE分区
 * - 分区维护：自动创建未来分区，清理过期分区
 * 
 * @author mooncloud
 */
@TableName("url_mapping")
@Data
@EqualsAndHashCode(callSuper = false)
public class UrlMappingEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 短链标识符
     */
    @TableField("short_url")
    private String shortUrl;
    
    /**
     * 原始URL
     */
    @TableField("original_url")
    private String originalUrl;
    
    /**
     * URL哈希值（用于重复检测）
     */
    @TableField("url_hash")
    private String urlHash;
    
    /**
     * 用户ID（可为空，表示游客用户）
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 点击次数
     */
    @TableField("click_count")
    private Long clickCount = 0L;
    
    /**
     * URL状态
     */
    @TableField("status")
    private UrlStatus status = UrlStatus.ACTIVE;
    
    /**
     * 过期时间
     */
    @TableField("expires_at")
    private Date expiresAt;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    
    /**
     * 链接标题（用户可自定义）
     */
    @TableField("title")
    private String title;
    
    /**
     * 备注信息
     */
    @TableField("description")
    private String description;
    
    /**
     * 是否为自定义短链
     */
    @TableField("is_custom")
    private Boolean isCustom = false;
}