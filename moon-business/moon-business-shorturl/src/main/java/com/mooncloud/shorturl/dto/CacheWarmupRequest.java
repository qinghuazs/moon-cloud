package com.mooncloud.shorturl.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 缓存预热请求DTO
 *
 * @author mooncloud
 */
@Data
public class CacheWarmupRequest {

    /**
     * 预热策略
     */
    @NotNull(message = "预热策略不能为空")
    private WarmupStrategy strategy;

    /**
     * 预热数据量限制
     */
    @Min(value = 1, message = "预热数据量最少为1")
    @Max(value = 100000, message = "预热数据量最多为100000")
    private Integer limit = 1000;

    /**
     * 开始时间（用于时间范围策略）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（用于时间范围策略）
     */
    private LocalDateTime endTime;

    /**
     * 用户ID（用于用户维度策略）
     */
    private Long userId;

    /**
     * 是否异步执行
     */
    private Boolean async = true;

    /**
     * 批次大小
     */
    @Min(value = 1, message = "批次大小最少为1")
    @Max(value = 1000, message = "批次大小最多为1000")
    private Integer batchSize = 100;

    /**
     * 预热策略枚举
     */
    public enum WarmupStrategy {
        /**
         * 热门短链 - 基于点击次数
         */
        HOT_LINKS,

        /**
         * 最近创建 - 基于创建时间
         */
        RECENT_CREATED,

        /**
         * 最近访问 - 基于最后访问时间
         */
        RECENT_ACCESSED,

        /**
         * 时间范围 - 指定时间范围内的短链
         */
        TIME_RANGE,

        /**
         * 用户维度 - 特定用户的短链
         */
        USER_BASED,

        /**
         * 全量预热 - 所有有效短链
         */
        FULL_WARMUP
    }
}