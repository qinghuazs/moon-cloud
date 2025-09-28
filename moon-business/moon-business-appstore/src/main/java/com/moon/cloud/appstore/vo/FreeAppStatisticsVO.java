package com.moon.cloud.appstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 限免应用统计信息视图对象
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "限免应用统计信息")
public class FreeAppStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "统计时间")
    private LocalDateTime statisticsTime;

    @Schema(description = "今日限免总数")
    private Integer todayFreeCount;

    @Schema(description = "今日打折总数")
    private Integer todayDiscountCount;

    @Schema(description = "今日新增限免数")
    private Integer todayNewFreeCount;

    @Schema(description = "活跃限免总数")
    private Integer activeFreeCount;

    @Schema(description = "活跃打折总数")
    private Integer activeDiscountCount;

    @Schema(description = "即将结束数量(6小时内)")
    private Integer endingSoonCount;

    @Schema(description = "今日总节省金额")
    private BigDecimal todayTotalSavings;

    @Schema(description = "平均折扣率")
    private BigDecimal averageDiscountRate;

    @Schema(description = "最高节省金额")
    private BigDecimal maxSavingsAmount;

    @Schema(description = "最高节省应用名称")
    private String maxSavingsAppName;

    @Schema(description = "最高节省应用ID")
    private String maxSavingsAppId;

    @Schema(description = "分类分布统计")
    private Map<String, Integer> categoryDistribution;

    @Schema(description = "价格区间分布")
    private Map<String, Integer> priceRangeDistribution;

    @Schema(description = "热门分类Top5")
    private Map<String, Integer> topCategories;

    @Schema(description = "热门开发商Top5")
    private Map<String, Integer> topDevelopers;

    @Schema(description = "24小时趋势数据")
    private Map<String, Integer> hourlyTrend;

    @Schema(description = "7天趋势数据")
    private Map<String, Integer> dailyTrend;

    @Schema(description = "本周限免总数")
    private Integer weeklyFreeCount;

    @Schema(description = "本月限免总数")
    private Integer monthlyFreeCount;

    @Schema(description = "本周总节省金额")
    private BigDecimal weeklyTotalSavings;

    @Schema(description = "本月总节省金额")
    private BigDecimal monthlyTotalSavings;

    @Schema(description = "最受欢迎限免应用Top10")
    private Map<String, Integer> popularApps;

    @Schema(description = "数据更新时间")
    private LocalDateTime updateTime;

    public FreeAppStatisticsVO() {
        this.statisticsTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}