package com.moon.cloud.appstore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 限免推广视图对象
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "限免推广信息")
public class FreePromotionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "推广记录ID")
    private String id;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "App Store应用ID")
    private String appstoreAppId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用图标URL")
    private String iconUrl;

    @Schema(description = "Bundle ID")
    private String bundleId;

    @Schema(description = "开发者名称")
    private String developerName;

    @Schema(description = "应用分类")
    private String categoryName;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "推广类型: FREE=限免, DISCOUNT=打折")
    private String promotionType;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "推广价格")
    private BigDecimal promotionPrice;

    @Schema(description = "折扣率(%)")
    private BigDecimal discountRate;

    @Schema(description = "节省金额")
    private BigDecimal savingsAmount;

    @Schema(description = "应用评分")
    private BigDecimal rating;

    @Schema(description = "评分人数")
    private Integer ratingCount;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件大小(格式化)")
    private String fileSizeFormatted;

    @Schema(description = "应用版本")
    private String version;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "应用截图")
    private List<String> screenshots;

    @Schema(description = "支持的设备")
    private List<String> supportedDevices;

    @Schema(description = "支持的语言")
    private List<String> languages;

    @Schema(description = "限免开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "限免结束时间(预估)")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;

    @Schema(description = "限免持续时长(小时)")
    private Integer durationHours;

    @Schema(description = "距离结束剩余时间(小时)")
    private Integer remainingHours;

    @Schema(description = "发现时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime discoveredAt;

    @Schema(description = "发现来源: AUTO=自动, USER=用户提交, EDITOR=编辑添加")
    private String discoverySource;

    @Schema(description = "状态: ACTIVE=进行中, ENDED=已结束, INVALID=无效")
    private String status;

    @Schema(description = "状态标签")
    private List<String> tags;

    @Schema(description = "是否新发现(6小时内)")
    private Boolean isNew;

    @Schema(description = "是否即将结束(6小时内)")
    private Boolean isEndingSoon;

    @Schema(description = "是否热门")
    private Boolean isHot;

    @Schema(description = "是否编辑推荐")
    private Boolean isFeatured;

    @Schema(description = "优先级得分")
    private Integer priorityScore;

    @Schema(description = "查看次数")
    private Integer viewCount;

    @Schema(description = "点击次数")
    private Integer clickCount;

    @Schema(description = "分享次数")
    private Integer shareCount;

    @Schema(description = "热度分数")
    private Integer hotScore;

    @Schema(description = "是否有内购")
    private Boolean hasInAppPurchase;

    @Schema(description = "年龄分级")
    private String contentRating;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 格式化文件大小
     */
    public String getFileSizeFormatted() {
        if (fileSize == null || fileSize == 0) {
            return "0 MB";
        }

        double size = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * 计算剩余时间（小时）
     */
    public Integer getRemainingHours() {
        if (endTime == null || status == null || !"ACTIVE".equals(status)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) {
            return 0;
        }

        long hours = java.time.Duration.between(now, endTime).toHours();
        return (int) hours;
    }

    /**
     * 判断是否为新发现（6小时内）
     */
    public Boolean getIsNew() {
        if (discoveredAt == null) {
            return false;
        }

        LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
        return discoveredAt.isAfter(sixHoursAgo);
    }

    /**
     * 判断是否即将结束（6小时内）
     */
    public Boolean getIsEndingSoon() {
        Integer remaining = getRemainingHours();
        return remaining != null && remaining <= 6 && remaining > 0;
    }

    /**
     * 计算热度分数
     */
    public Integer getHotScore() {
        int score = 0;

        // 基于查看次数
        if (viewCount != null) {
            score += viewCount;
        }

        // 基于点击次数（权重更高）
        if (clickCount != null) {
            score += clickCount * 3;
        }

        // 基于分享次数（权重最高）
        if (shareCount != null) {
            score += shareCount * 5;
        }

        // 基于评分和评分人数
        if (rating != null && ratingCount != null) {
            score += rating.multiply(new BigDecimal(ratingCount)).intValue() / 100;
        }

        // 基于节省金额
        if (savingsAmount != null) {
            score += savingsAmount.intValue() * 2;
        }

        return score;
    }
}