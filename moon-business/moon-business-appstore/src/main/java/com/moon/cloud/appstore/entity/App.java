package com.moon.cloud.appstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用信息表实体类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "apps", autoResultMap = true)
public class App implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * App Store应用ID
     */
    private String appId;

    /**
     * App Store链接URL
     */
    private String appUrl;

    /**
     * 应用Bundle ID
     */
    private String bundleId;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用副标题
     */
    private String subtitle;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 开发商名称
     */
    private String developerName;

    /**
     * 开发商ID
     */
    private String developerId;

    /**
     * 开发商网站
     */
    private String developerUrl;

    /**
     * 主分类ID
     */
    private String primaryCategoryId;

    /**
     * 主分类名称
     */
    private String primaryCategoryName;

    /**
     * 所有分类信息(JSON)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<CategoryInfo> categories;

    /**
     * 当前版本号
     */
    private String version;

    /**
     * 首次发布时间
     */
    private LocalDateTime releaseDate;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedDate;

    /**
     * 版本更新说明
     */
    private String releaseNotes;

    /**
     * 应用大小(字节)
     */
    private Long fileSize;

    /**
     * 最低系统要求
     */
    private String minimumOsVersion;

    /**
     * 应用图标URL
     */
    private String iconUrl;

    /**
     * 截图URLs(JSON数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> screenshots;

    /**
     * iPad截图URLs(JSON数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> ipadScreenshots;

    /**
     * 预览视频URL
     */
    private String previewVideoUrl;

    /**
     * 平均评分
     */
    private BigDecimal rating;

    /**
     * 评分总数
     */
    private Integer ratingCount;

    /**
     * 当前版本评分
     */
    private BigDecimal currentVersionRating;

    /**
     * 当前版本评分数
     */
    private Integer currentVersionRatingCount;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 原价(用于计算优惠)
     */
    private BigDecimal originalPrice;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 是否免费应用
     */
    private Boolean isFree;

    /**
     * 内容分级
     */
    private String contentRating;

    /**
     * 支持语言(JSON数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> languages;

    /**
     * 支持设备(JSON数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> supportedDevices;

    /**
     * 应用特性(JSON数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> features;

    /**
     * 是否有内购
     */
    private Boolean hasInAppPurchase;

    /**
     * 是否含广告
     */
    private Boolean hasAds;

    /**
     * 状态: 1=正常, 0=下架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后爬取时间
     */
    private LocalDateTime lastCrawledAt;

    /**
     * 分类信息内部类
     */
    @Data
    public static class CategoryInfo implements Serializable {
        private String id;
        private String name;
    }
}