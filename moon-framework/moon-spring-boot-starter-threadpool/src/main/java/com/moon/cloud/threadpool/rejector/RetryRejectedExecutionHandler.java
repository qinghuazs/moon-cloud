package com.moon.cloud.threadpool.rejector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于Spring Retry的线程池拒绝策略
 * 当线程池无法接受新任务时，会进行重试，最多重试指定次数
 * 重试间隔时间、重试间隔递增因子、最大重试间隔时间等参数可以根据实际情况进行配置
 * 重试完成后扔未提交成功，则抛出异常，并打印日志
 */
@Slf4j
@Component
public class RetryRejectedExecutionHandler implements RejectedExecutionHandler {
    
    private final RetryRejectedExecutionConfig config;
    private final RetryTemplate retryTemplate;
    private final AtomicLong rejectedCount = new AtomicLong(0);
    private final AtomicLong retrySuccessCount = new AtomicLong(0);
    private final AtomicLong retryFailedCount = new AtomicLong(0);
    
    public RetryRejectedExecutionHandler(RetryRejectedExecutionConfig config) {
        this.config = config;
        this.retryTemplate = createRetryTemplate();
    }
    
    /**
     * 创建重试模板
     */
    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        
        // 设置重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(config.getMaxAttempts());
        template.setRetryPolicy(retryPolicy);
        
        // 设置退避策略（指数退避）
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(config.getRetryInterval());
        backOffPolicy.setMultiplier(config.getBackoffMultiplier());
        backOffPolicy.setMaxInterval(config.getMaxRetryInterval());
        template.setBackOffPolicy(backOffPolicy);
        
        return template;
    }
    
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!config.isEnabled()) {
            // 如果未启用重试策略，则直接抛出异常
            log.error("线程池任务被拒绝，重试策略未启用。线程池状态: 核心线程数={}, 最大线程数={}, 当前线程数={}, 队列大小={}",
                    executor.getCorePoolSize(), executor.getMaximumPoolSize(), 
                    executor.getPoolSize(), executor.getQueue().size());
            throw new java.util.concurrent.RejectedExecutionException(
                    "Task " + r.toString() + " rejected from " + executor.toString());
        }
        
        long rejectedNumber = rejectedCount.incrementAndGet();
        log.warn("线程池任务被拒绝，开始重试。拒绝次数: {}, 任务: {}", rejectedNumber, r.toString());
        
        try {
            // 使用Spring Retry进行重试
            retryTemplate.execute(new RetryCallback<Void, Exception>() {
                @Override
                public Void doWithRetry(RetryContext context) throws Exception {
                    try {
                        // 尝试重新提交任务
                        executor.execute(r);
                        if (context.getRetryCount() > 0) {
                            log.info("任务重试成功，重试次数: {}, 任务: {}", context.getRetryCount(), r.toString());
                            retrySuccessCount.incrementAndGet();
                        }
                        return null;
                    } catch (java.util.concurrent.RejectedExecutionException e) {
                        log.warn("任务重试失败，重试次数: {}/{}, 任务: {}, 错误: {}", 
                                context.getRetryCount(), config.getMaxAttempts(), r.toString(), e.getMessage());
                        throw e;
                    }
                }
            });
        } catch (Exception e) {
            // 所有重试都失败了，记录错误日志并打印堆栈
            retryFailedCount.incrementAndGet();
            log.error("任务重试全部失败，已达到最大重试次数: {}。线程池状态: 核心线程数={}, 最大线程数={}, 当前线程数={}, 活跃线程数={}, 队列大小={}, 已完成任务数={}",
                    config.getMaxAttempts(),
                    executor.getCorePoolSize(),
                    executor.getMaximumPoolSize(),
                    executor.getPoolSize(),
                    executor.getActiveCount(),
                    executor.getQueue().size(),
                    executor.getCompletedTaskCount());
            log.error("被拒绝的任务详情: {}", r.toString());
            log.error("重试失败堆栈信息: ", e);
            
            // 最终还是要抛出异常，让调用方知道任务执行失败
            throw new java.util.concurrent.RejectedExecutionException(
                    "Task " + r.toString() + " rejected from " + executor.toString() + 
                    " after " + config.getMaxAttempts() + " retry attempts", e);
        }
    }
    
    /**
     * 获取拒绝统计信息
     */
    public String getStatistics() {
        return String.format("拒绝策略统计 - 总拒绝次数: %d, 重试成功次数: %d, 重试失败次数: %d", 
                rejectedCount.get(), retrySuccessCount.get(), retryFailedCount.get());
    }
    
    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        rejectedCount.set(0);
        retrySuccessCount.set(0);
        retryFailedCount.set(0);
        log.info("重试拒绝策略统计信息已重置");
    }
}