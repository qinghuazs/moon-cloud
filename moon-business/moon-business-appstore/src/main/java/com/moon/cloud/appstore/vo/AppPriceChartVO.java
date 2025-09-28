package com.moon.cloud.appstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用价格图表数据视图对象
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "应用价格图表数据")
public class AppPriceChartVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "时间范围（天数）")
    private Integer days;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "当前价格")
    private BigDecimal currentPrice;

    @Schema(description = "原始价格")
    private BigDecimal originalPrice;

    @Schema(description = "历史最低价")
    private BigDecimal lowestPrice;

    @Schema(description = "历史最高价")
    private BigDecimal highestPrice;

    @Schema(description = "平均价格")
    private BigDecimal averagePrice;

    @Schema(description = "价格变化次数")
    private Integer changeCount;

    @Schema(description = "限免次数")
    private Integer freeCount;

    @Schema(description = "价格数据点")
    private List<PricePoint> pricePoints;

    @Schema(description = "价格事件")
    private List<PriceEvent> priceEvents;

    /**
     * 价格数据点
     */
    @Data
    @Schema(description = "价格数据点")
    public static class PricePoint implements Serializable {
        @Schema(description = "时间")
        private LocalDateTime time;

        @Schema(description = "价格")
        private BigDecimal price;

        @Schema(description = "是否限免")
        private Boolean isFree;

        @Schema(description = "变化类型")
        private String changeType;
    }

    /**
     * 价格事件
     */
    @Data
    @Schema(description = "价格事件")
    public static class PriceEvent implements Serializable {
        @Schema(description = "事件时间")
        private LocalDateTime eventTime;

        @Schema(description = "事件类型：FREE, DISCOUNT, INCREASE, RESTORE")
        private String eventType;

        @Schema(description = "原价格")
        private BigDecimal oldPrice;

        @Schema(description = "新价格")
        private BigDecimal newPrice;

        @Schema(description = "变化金额")
        private BigDecimal changeAmount;

        @Schema(description = "变化百分比")
        private BigDecimal changePercent;

        @Schema(description = "事件描述")
        private String description;

        public String getDescription() {
            if (description != null) {
                return description;
            }

            switch (eventType) {
                case "FREE":
                    return String.format("限免：¥%.2f → 免费", oldPrice);
                case "DISCOUNT":
                    return String.format("降价：¥%.2f → ¥%.2f (降%.0f%%)",
                            oldPrice, newPrice, changePercent);
                case "INCREASE":
                    return String.format("涨价：¥%.2f → ¥%.2f", oldPrice, newPrice);
                case "RESTORE":
                    return String.format("恢复原价：¥%.2f", newPrice);
                default:
                    return "价格变化";
            }
        }
    }
}