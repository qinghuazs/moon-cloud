package com.mooncloud.shorturl.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 缓存预热响应DTO
 *
 * @author mooncloud
 */
@Data
@Builder
public class CacheWarmupResponse {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 预热策略
     */
    private String strategy;

    /**
     * 预热状态
     */
    private WarmupStatus status;

    /**
     * 总数据量
     */
    private Integer totalCount;

    /**
     * 已预热数量
     */
    private Integer warmedCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 预热状态枚举
     */
    public enum WarmupStatus {
        /**
         * 进行中
         */
        RUNNING,

        /**
         * 已完成
         */
        COMPLETED,

        /**
         * 失败
         */
        FAILED,

        /**
         * 已取消
         */
        CANCELLED
    }
}