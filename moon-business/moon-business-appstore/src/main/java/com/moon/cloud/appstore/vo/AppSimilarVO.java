package com.moon.cloud.appstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 相似应用视图对象
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "相似应用信息")
public class AppSimilarVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private String id;

    @Schema(description = "App Store ID")
    private String appId;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "应用图标")
    private String iconUrl;

    @Schema(description = "Bundle ID")
    private String bundleId;

    @Schema(description = "开发者名称")
    private String developerName;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "当前价格")
    private BigDecimal currentPrice;

    @Schema(description = "原始价格")
    private BigDecimal originalPrice;

    @Schema(description = "是否免费")
    private Boolean isFree;

    @Schema(description = "是否限免")
    private Boolean hasPromotion;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "评分人数")
    private Integer ratingCount;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件大小(格式化)")
    private String fileSizeFormatted;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "简短描述")
    private String shortDescription;

    @Schema(description = "相似度分数")
    private BigDecimal similarityScore;

    @Schema(description = "推荐理由")
    private String recommendReason;

    @Schema(description = "App Store URL")
    private String appStoreUrl;

    @Schema(description = "标签")
    private String[] tags;

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
     * 生成App Store URL
     */
    public String getAppStoreUrl() {
        if (appId != null) {
            return String.format("https://apps.apple.com/cn/app/id%s", appId);
        }
        return null;
    }
}