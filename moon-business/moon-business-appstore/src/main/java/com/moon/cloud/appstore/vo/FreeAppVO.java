package com.moon.cloud.appstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 限免应用列表展示VO
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@Schema(description = "限免应用信息")
public class FreeAppVO {

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "App Store应用ID")
    private String appstoreId;

    @Schema(description = "App Store链接URL")
    private String appUrl;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "应用副标题")
    private String subtitle;

    @Schema(description = "开发商名称")
    private String developerName;

    @Schema(description = "应用图标URL")
    private String iconUrl;

    @Schema(description = "主分类名称")
    private String categoryName;

    @Schema(description = "文件大小(MB)")
    private String fileSizeFormatted;

    @Schema(description = "当前版本号")
    private String version;

    @Schema(description = "平均评分")
    private BigDecimal rating;

    @Schema(description = "评分数量")
    private Integer ratingCount;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "当前价格(0表示限免)")
    private BigDecimal currentPrice;

    @Schema(description = "节省金额")
    private BigDecimal savingsAmount;

    @Schema(description = "限免开始时间")
    private LocalDateTime freeStartTime;

    @Schema(description = "限免预计结束时间")
    private LocalDateTime freeEndTime;

    @Schema(description = "限免剩余时间(小时)")
    private Integer remainingHours;

    @Schema(description = "限免状态标签")
    private List<String> statusTags;

    @Schema(description = "是否编辑推荐")
    private Boolean isFeatured;

    @Schema(description = "是否热门")
    private Boolean isHot;

    @Schema(description = "是否即将结束(6小时内)")
    private Boolean isEndingSoon;

    @Schema(description = "是否新发现(6小时内)")
    private Boolean isNewFound;

    @Schema(description = "支持设备列表")
    private List<String> supportedDevices;

    @Schema(description = "是否有内购")
    private Boolean hasInAppPurchase;

    @Schema(description = "是否含广告")
    private Boolean hasAds;
}