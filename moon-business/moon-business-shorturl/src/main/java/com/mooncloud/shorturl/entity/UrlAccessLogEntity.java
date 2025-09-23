package com.mooncloud.shorturl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * URL访问记录实体类
 * 
 * 分区策略：按访问时间（天）进行分区
 * - 分区键：TO_DAYS(access_time)
 * - 分区类型：RANGE分区
 * - 分区维护：自动创建未来分区，清理过期分区
 * - 数据保留：默认保留90天的访问日志
 * 
 * @author mooncloud
 */
@TableName("url_access_log")
@Data
@EqualsAndHashCode(callSuper = false)
public class UrlAccessLogEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 短链标识符
     */
    @TableField("short_url")
    private String shortUrl;
    
    /**
     * 访问者IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 用户代理字符串
     */
    @TableField("user_agent")
    private String userAgent;
    
    /**
     * 来源页面
     */
    @TableField("referer")
    private String referer;
    
    /**
     * 访问时间
     */
    @TableField(value = "access_time", fill = FieldFill.INSERT)
    private Date accessTime;
    
    /**
     * 地理位置信息（国家）
     */
    @TableField("country")
    private String country;
    
    /**
     * 地理位置信息（城市）
     */
    @TableField("city")
    private String city;
    
    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;
    
    /**
     * 浏览器类型
     */
    @TableField("browser")
    private String browser;
    
    /**
     * 操作系统
     */
    @TableField("operating_system")
    private String operatingSystem;
}