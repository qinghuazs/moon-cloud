package com.mooncloud.shorturl.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

/**
 * URL访问记录实体类
 * 
 * @author mooncloud
 */
@Entity
@Table(name = "url_access_log", indexes = {
    @Index(name = "idx_short_url", columnList = "shortUrl"),
    @Index(name = "idx_access_time", columnList = "accessTime"),
    @Index(name = "idx_ip_address", columnList = "ipAddress")
})
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class UrlAccessLogEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 短链标识符
     */
    @Column(name = "short_url", nullable = false, length = 20)
    private String shortUrl;
    
    /**
     * 访问者IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * 用户代理字符串
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 来源页面
     */
    @Column(name = "referer", length = 500)
    private String referer;
    
    /**
     * 访问时间
     */
    @CreatedDate
    @Column(name = "access_time", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date accessTime;
    
    /**
     * 地理位置信息（国家）
     */
    @Column(name = "country", length = 100)
    private String country;
    
    /**
     * 地理位置信息（城市）
     */
    @Column(name = "city", length = 100)
    private String city;
    
    /**
     * 设备类型
     */
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    /**
     * 浏览器类型
     */
    @Column(name = "browser", length = 100)
    private String browser;
    
    /**
     * 操作系统
     */
    @Column(name = "operating_system", length = 100)
    private String operatingSystem;
}