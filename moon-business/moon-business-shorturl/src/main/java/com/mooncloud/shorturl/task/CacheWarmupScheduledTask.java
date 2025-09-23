package com.mooncloud.shorturl.task;

import com.mooncloud.shorturl.dto.CacheWarmupRequest;
import com.mooncloud.shorturl.service.CacheWarmupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 缓存预热定时任务
 *
 * 支持定时执行各种预热策略，确保缓存始终保持热点数据
 *
 * @author mooncloud
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "shorturl.cache.warmup.enabled", havingValue = "true", matchIfMissing = false)
public class CacheWarmupScheduledTask {

    @Autowired
    private CacheWarmupService cacheWarmupService;

    @Value("${shorturl.cache.warmup.hot-links.limit:1000}")
    private Integer hotLinksLimit;

    @Value("${shorturl.cache.warmup.recent.limit:500}")
    private Integer recentLimit;

    @Value("${shorturl.cache.warmup.batch-size:100}")
    private Integer batchSize;

    /**
     * 每小时预热热门链接
     * 在每小时的第0分钟执行
     */
    @Scheduled(cron = "${shorturl.cache.warmup.hot-links.cron:0 0 * * * ?}")
    public void warmupHotLinksHourly() {
        if (!isWarmupEnabled()) {
            return;
        }

        log.info("开始执行定时预热任务: 热门链接");

        try {
            CacheWarmupRequest request = new CacheWarmupRequest();
            request.setStrategy(CacheWarmupRequest.WarmupStrategy.HOT_LINKS);
            request.setLimit(hotLinksLimit);
            request.setAsync(true);
            request.setBatchSize(batchSize);

            cacheWarmupService.executeWarmup(request);

            log.info("热门链接定时预热任务已启动");

        } catch (Exception e) {
            log.error("热门链接定时预热任务执行失败", e);
        }
    }

    /**
     * 每30分钟预热最近创建的链接
     * 在每小时的第0分钟和第30分钟执行
     */
    @Scheduled(cron = "${shorturl.cache.warmup.recent.cron:0 0,30 * * * ?}")
    public void warmupRecentLinksEvery30Minutes() {
        if (!isWarmupEnabled()) {
            return;
        }

        log.info("开始执行定时预热任务: 最近创建链接");

        try {
            CacheWarmupRequest request = new CacheWarmupRequest();
            request.setStrategy(CacheWarmupRequest.WarmupStrategy.RECENT_CREATED);
            request.setLimit(recentLimit);
            request.setAsync(true);
            request.setBatchSize(batchSize);

            cacheWarmupService.executeWarmup(request);

            log.info("最近创建链接定时预热任务已启动");

        } catch (Exception e) {
            log.error("最近创建链接定时预热任务执行失败", e);
        }
    }

    /**
     * 每天凌晨2点执行全量预热（限量版本）
     * 预热当天创建的所有链接
     */
    @Scheduled(cron = "${shorturl.cache.warmup.daily.cron:0 0 2 * * ?}")
    public void warmupDailyLinks() {
        if (!isWarmupEnabled()) {
            return;
        }

        log.info("开始执行定时预热任务: 每日全量预热");

        try {
            CacheWarmupRequest request = new CacheWarmupRequest();
            request.setStrategy(CacheWarmupRequest.WarmupStrategy.RECENT_CREATED);
            request.setLimit(5000); // 每日预热最多5000条
            request.setAsync(true);
            request.setBatchSize(batchSize);

            cacheWarmupService.executeWarmup(request);

            log.info("每日全量预热任务已启动");

        } catch (Exception e) {
            log.error("每日全量预热任务执行失败", e);
        }
    }

    /**
     * 每6小时清理已完成的任务
     */
    @Scheduled(fixedRate = 21600000) // 6小时 = 6 * 60 * 60 * 1000
    public void cleanupCompletedTasks() {
        if (!isWarmupEnabled()) {
            return;
        }

        try {
            log.debug("开始清理已完成的预热任务");
            cacheWarmupService.cleanupCompletedTasks();
            log.debug("已完成预热任务清理完成");
        } catch (Exception e) {
            log.error("清理已完成预热任务失败", e);
        }
    }

    /**
     * 应用启动后延迟5分钟执行初始预热
     */
    @Scheduled(initialDelay = 300000, fixedRate = Long.MAX_VALUE) // 5分钟后执行一次
    public void initialWarmup() {
        if (!isWarmupEnabled()) {
            return;
        }

        log.info("开始执行应用启动初始预热");

        try {
            // 预热热门链接
            CacheWarmupRequest hotLinksRequest = new CacheWarmupRequest();
            hotLinksRequest.setStrategy(CacheWarmupRequest.WarmupStrategy.HOT_LINKS);
            hotLinksRequest.setLimit(500);
            hotLinksRequest.setAsync(true);
            hotLinksRequest.setBatchSize(50);

            cacheWarmupService.executeWarmup(hotLinksRequest);

            // 延迟1分钟后预热最近创建的链接
            Thread.sleep(60000);

            CacheWarmupRequest recentRequest = new CacheWarmupRequest();
            recentRequest.setStrategy(CacheWarmupRequest.WarmupStrategy.RECENT_CREATED);
            recentRequest.setLimit(300);
            recentRequest.setAsync(true);
            recentRequest.setBatchSize(50);

            cacheWarmupService.executeWarmup(recentRequest);

            log.info("应用启动初始预热任务已启动");

        } catch (Exception e) {
            log.error("应用启动初始预热任务执行失败", e);
        }
    }

    /**
     * 检查预热功能是否启用
     */
    private boolean isWarmupEnabled() {
        // 这里可以添加额外的检查逻辑，比如检查系统负载、时间段等
        return true;
    }
}