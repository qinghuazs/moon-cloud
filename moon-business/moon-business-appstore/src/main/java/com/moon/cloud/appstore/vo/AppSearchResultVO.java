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
 * 应用搜索结果视图对象
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "应用搜索结果")
public class AppSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private String id;

    @Schema(description = "App Store ID")
    private String appId;

    @Schema(description = "Bundle ID")
    private String bundleId;

    @Schema(description = "应用名称（高亮）")
    private String name;

    @Schema(description = "应用名称（原始）")
    private String nameOriginal;

    @Schema(description = "应用图标")
    private String iconUrl;

    @Schema(description = "应用描述（高亮）")
    private String description;

    @Schema(description = "应用描述（原始）")
    private String descriptionOriginal;

    @Schema(description = "开发者名称（高亮）")
    private String developerName;

    @Schema(description = "开发者名称（原始）")
    private String developerNameOriginal;

    @Schema(description = "主分类名称")
    private String categoryName;

    @Schema(description = "当前价格")
    private BigDecimal currentPrice;

    @Schema(description = "原始价格")
    private BigDecimal originalPrice;

    @Schema(description = "是否免费")
    private Boolean isFree;

    @Schema(description = "是否有限免")
    private Boolean hasPromotion;

    @Schema(description = "限免类型")
    private String promotionType;

    @Schema(description = "节省金额")
    private BigDecimal savingsAmount;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "评分人数")
    private Integer ratingCount;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件大小（格式化）")
    private String fileSizeFormatted;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "最近更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    @Schema(description = "搜索相关度分数")
    private BigDecimal relevanceScore;

    @Schema(description = "匹配字段")
    private List<String> matchedFields;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "截图（前3张）")
    private List<String> screenshots;

    @Schema(description = "支持的设备")
    private List<String> supportedDevices;

    @Schema(description = "App Store URL")
    private String appStoreUrl;

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

    /**
     * 设置高亮文本
     * @param text 原始文本
     * @param keyword 关键词
     * @return 高亮后的文本
     */
    public static String highlightText(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) {
            return text;
        }

        // 简单的高亮实现，实际可以使用更复杂的算法
        String highlightedKeyword = "<em>" + keyword + "</em>";
        return text.replaceAll("(?i)" + keyword, highlightedKeyword);
    }
}