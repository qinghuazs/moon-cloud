package com.mooncloud.shorturl.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存预热监控指标
 *
 * 提供详细的预热任务监控数据：
 * 1. 任务执行次数和耗时
 * 2. 成功/失败统计
 * 3. 预热数据量统计
 * 4. 活跃任务数量
 *
 * @author mooncloud
 */
@Component
@Slf4j
public class CacheWarmupMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    // 计数器
    private Counter warmupTaskStartedCounter;
    private Counter warmupTaskCompletedCounter;
    private Counter warmupTaskFailedCounter;
    private Counter warmupRecordsSuccessCounter;
    private Counter warmupRecordsFailedCounter;

    // 计时器
    private Timer warmupTaskDurationTimer;

    // 原子计数器（用于Gauge）
    private final AtomicInteger activeTasksCount = new AtomicInteger(0);
    private final AtomicLong totalWarmedRecords = new AtomicLong(0);
    private final AtomicLong lastWarmupDuration = new AtomicLong(0);

    @PostConstruct
    public void initMetrics() {
        // 任务启动计数器
        warmupTaskStartedCounter = Counter.builder("cache_warmup_tasks_started_total")
                .description("Total number of cache warmup tasks started")
                .register(meterRegistry);

        // 任务完成计数器
        warmupTaskCompletedCounter = Counter.builder("cache_warmup_tasks_completed_total")
                .description("Total number of cache warmup tasks completed")
                .register(meterRegistry);

        // 任务失败计数器
        warmupTaskFailedCounter = Counter.builder("cache_warmup_tasks_failed_total")
                .description("Total number of cache warmup tasks failed")
                .register(meterRegistry);

        // 预热记录成功计数器
        warmupRecordsSuccessCounter = Counter.builder("cache_warmup_records_success_total")
                .description("Total number of successfully warmed up records")
                .register(meterRegistry);

        // 预热记录失败计数器
        warmupRecordsFailedCounter = Counter.builder("cache_warmup_records_failed_total")
                .description("Total number of failed to warm up records")
                .register(meterRegistry);

        // 任务执行时间计时器
        warmupTaskDurationTimer = Timer.builder("cache_warmup_task_duration_seconds")
                .description("Duration of cache warmup tasks")
                .register(meterRegistry);

        // 活跃任务数量
        Gauge.builder("cache_warmup_active_tasks")
                .description("Number of currently active warmup tasks")
                .register(meterRegistry, this, CacheWarmupMetrics::getActiveTasksCount);

        // 总预热记录数
        Gauge.builder("cache_warmup_total_records")
                .description("Total number of records warmed up")
                .register(meterRegistry, this, CacheWarmupMetrics::getTotalWarmedRecords);

        // 最后一次预热耗时
        Gauge.builder("cache_warmup_last_duration_seconds")
                .description("Duration of the last completed warmup task")
                .register(meterRegistry, this, CacheWarmupMetrics::getLastWarmupDurationSeconds);

        log.info("缓存预热监控指标初始化完成");
    }

    /**
     * 记录任务启动
     */
    public void recordTaskStarted(String strategy) {
        warmupTaskStartedCounter.increment();
        activeTasksCount.incrementAndGet();
        log.debug("记录预热任务启动: {}", strategy);
    }

    /**
     * 记录任务完成
     */
    public void recordTaskCompleted(String strategy, long durationMs, int successCount, int failedCount) {
        warmupTaskCompletedCounter.increment();
        activeTasksCount.decrementAndGet();

        // 记录执行时间
        warmupTaskDurationTimer.record(durationMs / 1000.0, java.util.concurrent.TimeUnit.SECONDS);
        lastWarmupDuration.set(durationMs);

        // 记录预热记录数
        warmupRecordsSuccessCounter.increment(successCount);
        if (failedCount > 0) {
            warmupRecordsFailedCounter.increment(failedCount);
        }

        // 更新总预热记录数
        totalWarmedRecords.addAndGet(successCount);

        log.info("记录预热任务完成: 策略={}, 耗时={}ms, 成功={}, 失败={}",
                strategy, durationMs, successCount, failedCount);
    }

    /**
     * 记录任务失败
     */
    public void recordTaskFailed(String strategy, String errorMessage) {
        warmupTaskFailedCounter.increment();
        activeTasksCount.decrementAndGet();
        log.warn("记录预热任务失败: 策略={}, 错误={}", strategy, errorMessage);
    }

    /**
     * 记录任务取消
     */
    public void recordTaskCancelled(String strategy) {
        activeTasksCount.decrementAndGet();
        log.info("记录预热任务取消: {}", strategy);
    }

    /**
     * 记录单条记录预热成功
     */
    public void recordRecordWarmedSuccess() {
        warmupRecordsSuccessCounter.increment();
        totalWarmedRecords.incrementAndGet();
    }

    /**
     * 记录单条记录预热失败
     */
    public void recordRecordWarmedFailed() {
        warmupRecordsFailedCounter.increment();
    }

    /**
     * 获取活跃任务数量
     */
    public int getActiveTasksCount() {
        return activeTasksCount.get();
    }

    /**
     * 获取总预热记录数
     */
    public long getTotalWarmedRecords() {
        return totalWarmedRecords.get();
    }

    /**
     * 获取最后一次预热耗时（秒）
     */
    public double getLastWarmupDurationSeconds() {
        return lastWarmupDuration.get() / 1000.0;
    }

    /**
     * 重置指标（谨慎使用）
     */
    public void resetMetrics() {
        activeTasksCount.set(0);
        totalWarmedRecords.set(0);
        lastWarmupDuration.set(0);
        log.warn("缓存预热监控指标已重置");
    }
}