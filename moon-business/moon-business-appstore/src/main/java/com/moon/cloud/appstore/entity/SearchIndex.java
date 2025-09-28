package com.moon.cloud.appstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 搜索索引表实体类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("search_index")
public class SearchIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 应用ID(关联apps表)
     */
    private String appId;

    /**
     * App Store应用ID
     */
    private String appstoreAppId;

    /**
     * App Store ID (别名)
     */
    private String appstoreId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用名称(别名)
     */
    private String name;

    /**
     * 应用名称拼音
     */
    private String appNamePinyin;

    /**
     * 开发商名称
     */
    private String developerName;

    /**
     * 开发商名称拼音
     */
    private String developerNamePinyin;

    /**
     * 关键词(从描述中提取)
     */
    private String keywords;

    /**
     * 分类名称(多个用逗号分隔)
     */
    private String categoryNames;

    /**
     * 分类名称(别名)
     */
    private String categoryName;

    /**
     * 描述摘要
     */
    private String descriptionSnippet;

    /**
     * 描述(完整)
     */
    private String description;

    /**
     * 搜索权重
     */
    private Integer searchWeight;

    /**
     * 热度得分
     */
    private BigDecimal popularityScore;

    /**
     * 质量得分
     */
    private BigDecimal qualityScore;

    /**
     * 搜索次数
     */
    private Integer searchCount;

    /**
     * 点击率
     */
    private BigDecimal clickRate;

    /**
     * Bundle ID
     */
    private String bundleId;

    /**
     * 图标URL
     */
    private String iconUrl;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 下载量
     */
    private Integer downloads;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 原始价格
     */
    private BigDecimal originalPrice;

    /**
     * 是否免费
     */
    private Boolean isFree;

    /**
     * 是否有内购
     */
    private Boolean hasInAppPurchase;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}