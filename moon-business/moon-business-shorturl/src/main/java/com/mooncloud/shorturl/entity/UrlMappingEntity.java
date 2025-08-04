package com.mooncloud.shorturl.entity;

import com.mooncloud.shorturl.enums.UrlStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * URL映射实体类
 * 
 * @author mooncloud
 */
@Entity
@Table(name = "url_mapping", indexes = {
    @Index(name = "idx_short_url", columnList = "shortUrl", unique = true),
    @Index(name = "idx_url_hash", columnList = "urlHash"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class UrlMappingEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 短链标识符
     */
    @Column(name = "short_url", nullable = false, unique = true, length = 20)
    private String shortUrl;
    
    /**
     * 原始URL
     */
    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;
    
    /**
     * URL哈希值（用于重复检测）
     */
    @Column(name = "url_hash", nullable = false, length = 32)
    private String urlHash;
    
    /**
     * 用户ID（可为空，表示游客用户）
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 点击次数
     */
    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;
    
    /**
     * URL状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UrlStatus status = UrlStatus.ACTIVE;
    
    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    /**
     * 链接标题（用户可自定义）
     */
    @Column(name = "title", length = 200)
    private String title;
    
    /**
     * 备注信息
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 是否为自定义短链
     */
    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false;
}