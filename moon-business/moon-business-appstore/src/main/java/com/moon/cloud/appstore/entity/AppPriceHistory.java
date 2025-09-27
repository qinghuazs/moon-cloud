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
 * APP价格历史记录实体类
 * 记录APP的价格变化历史
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app_price_history")
public class AppPriceHistory implements Serializable {

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
     * 应用Bundle ID
     */
    private String bundleId;

    /**
     * 应用名称（冗余字段，便于查询）
     */
    private String appName;

    /**
     * 原价格
     */
    private BigDecimal oldPrice;

    /**
     * 新价格
     */
    private BigDecimal newPrice;

    /**
     * 价格变化量（新价格 - 原价格）
     */
    private BigDecimal priceChange;

    /**
     * 价格变化百分比
     */
    private BigDecimal changePercent;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 价格类型：NORMAL-正常价格, PROMOTION-促销价格, FREE-限免
     */
    private String priceType;

    /**
     * 变化类型：INCREASE-涨价, DECREASE-降价, FREE-限免, RESTORE-恢复原价, INITIAL-初始记录
     */
    private String changeType;

    /**
     * 是否为限免：0-否, 1-是
     */
    private Boolean isFree;

    /**
     * 原始是否免费状态
     */
    private Boolean oldIsFree;

    /**
     * 版本号（价格变化时的版本）
     */
    private String version;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 开发者名称
     */
    private String developerName;

    /**
     * 价格变化时间（检测到变化的时间）
     */
    private LocalDateTime changeTime;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 数据来源：CRAWLER-爬虫, MANUAL-手动, API-接口
     */
    private String source;

    /**
     * 是否已通知：0-未通知, 1-已通知
     */
    private Boolean isNotified;

    /**
     * 通知时间
     */
    private LocalDateTime notifiedAt;
}