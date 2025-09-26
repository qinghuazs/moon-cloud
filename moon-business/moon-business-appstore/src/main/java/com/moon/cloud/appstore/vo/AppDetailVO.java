package com.moon.cloud.appstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用详情展示VO
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@Schema(description = "应用详细信息")
public class AppDetailVO {

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "App Store应用ID")
    private String appstoreId;

    @Schema(description = "应用Bundle ID")
    private String bundleId;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "应用副标题")
    private String subtitle;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "开发商名称")
    private String developerName;

    @Schema(description = "开发商ID")
    private String developerId;

    @Schema(description = "开发商网站")
    private String developerUrl;

    @Schema(description = "应用图标URL")
    private String iconUrl;

    @Schema(description = "应用截图列表")
    private List<String> screenshots;

    @Schema(description = "iPad截图列表")
    private List<String> ipadScreenshots;

    @Schema(description = "预览视频URL")
    private String previewVideoUrl;

    @Schema(description = "主分类信息")
    private CategoryInfo primaryCategory;

    @Schema(description = "所有分类信息")
    private List<CategoryInfo> categories;

    @Schema(description = "当前版本号")
    private String version;

    @Schema(description = "首次发布时间")
    private LocalDateTime releaseDate;

    @Schema(description = "最后更新时间")
    private LocalDateTime updatedDate;

    @Schema(description = "版本更新说明")
    private String releaseNotes;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件大小(格式化)")
    private String fileSizeFormatted;

    @Schema(description = "最低系统要求")
    private String minimumOsVersion;

    @Schema(description = "平均评分")
    private BigDecimal rating;

    @Schema(description = "评分总数")
    private Integer ratingCount;

    @Schema(description = "当前版本评分")
    private BigDecimal currentVersionRating;

    @Schema(description = "当前版本评分数")
    private Integer currentVersionRatingCount;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "当前价格")
    private BigDecimal currentPrice;

    @Schema(description = "货币类型")
    private String currency;

    @Schema(description = "是否限免中")
    private Boolean isFreeNow;

    @Schema(description = "限免信息")
    private FreePromotionInfo freePromotion;

    @Schema(description = "内容分级")
    private String contentRating;

    @Schema(description = "支持语言列表")
    private List<String> languages;

    @Schema(description = "支持设备列表")
    private List<String> supportedDevices;

    @Schema(description = "应用特性列表")
    private List<String> features;

    @Schema(description = "是否有内购")
    private Boolean hasInAppPurchase;

    @Schema(description = "是否含广告")
    private Boolean hasAds;

    @Schema(description = "价格历史记录")
    private List<PriceHistoryInfo> priceHistory;

    /**
     * 分类信息
     */
    @Data
    @Schema(description = "分类信息")
    public static class CategoryInfo {
        @Schema(description = "分类ID")
        private String categoryId;

        @Schema(description = "分类名称")
        private String name;
    }

    /**
     * 限免信息
     */
    @Data
    @Schema(description = "限免促销信息")
    public static class FreePromotionInfo {
        @Schema(description = "限免开始时间")
        private LocalDateTime startTime;

        @Schema(description = "限免预计结束时间")
        private LocalDateTime endTime;

        @Schema(description = "节省金额")
        private BigDecimal savingsAmount;

        @Schema(description = "剩余时间(小时)")
        private Integer remainingHours;

        @Schema(description = "是否即将结束")
        private Boolean isEndingSoon;
    }

    /**
     * 价格历史信息
     */
    @Data
    @Schema(description = "价格历史信息")
    public static class PriceHistoryInfo {
        @Schema(description = "价格")
        private BigDecimal price;

        @Schema(description = "记录时间")
        private LocalDateTime recordTime;

        @Schema(description = "价格变化类型")
        private String changeType;
    }
}