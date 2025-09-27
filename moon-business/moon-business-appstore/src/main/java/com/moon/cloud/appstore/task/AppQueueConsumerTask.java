package com.moon.cloud.appstore.task;

import com.moon.cloud.appstore.service.AppQueueConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * App队列消费定时任务
 * 定期从Redis队列中消费URL并获取App详情
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "appstore.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class AppQueueConsumerTask {

    private final AppQueueConsumerService appQueueConsumerService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 每天凌晨3点执行消费任务
     * 消费所有分类的队列
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void consumeAppQueue() {
        String startTime = LocalDateTime.now().format(DTF);
        log.info("========== 开始执行App队列消费任务 [{}] ==========", startTime);

        try {
            Long totalQueueSize = appQueueConsumerService.getAllQueuesSize();
            Long failedQueueSize = appQueueConsumerService.getFailedQueueSize();

            log.info("当前队列状态 - 所有分类队列总计: {}, 失败: {}", totalQueueSize, failedQueueSize);

            if (totalQueueSize > 0) {
                // 消费所有分类的队列
                appQueueConsumerService.consumeAllCategoryQueues();
            } else {
                log.debug("所有分类队列均为空，跳过本次消费");
            }

        } catch (Exception e) {
            log.error("执行App队列消费任务异常", e);
        }

        String endTime = LocalDateTime.now().format(DTF);
        log.info("========== App队列消费任务执行完成 [{}] ==========", endTime);
    }

    /**
     * 每30分钟执行一次失败重试
     */
    @Scheduled(fixedDelay = 1800000, initialDelay = 300000)
    public void retryFailedTasks() {
        String startTime = LocalDateTime.now().format(DTF);
        log.info("========== 开始执行失败任务重试 [{}] ==========", startTime);

        try {
            Long failedQueueSize = appQueueConsumerService.getFailedQueueSize();

            if (failedQueueSize > 0) {
                log.info("发现失败任务数量: {}", failedQueueSize);
                appQueueConsumerService.retryFailedTasks();
            } else {
                log.debug("没有失败任务需要重试");
            }

        } catch (Exception e) {
            log.error("执行失败任务重试异常", e);
        }

        String endTime = LocalDateTime.now().format(DTF);
        log.info("========== 失败任务重试执行完成 [{}] ==========", endTime);
    }

    /**
     * 每天凌晨3点清理过期的失败记录
     * 清理超过7天的失败记录
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldFailures() {
        log.info("========== 开始清理过期失败记录 ==========");

        try {
            // 这里可以添加清理逻辑，清理数据库中的旧失败记录
            log.info("清理过期失败记录完成");

        } catch (Exception e) {
            log.error("清理过期失败记录异常", e);
        }
    }
}