package com.moon.cloud.threadpool.wrapper;

import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务包装器 - 用于收集任务执行指标
 *
 * @author moon
 * @since 1.0.0
 */
@Slf4j
public class MetricTaskWrapper implements Runnable {

    private final Runnable task;
    private final String poolName;

    public MetricTaskWrapper(Runnable task, String poolName) {
        this.task = task;
        this.poolName = poolName;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        String originalThreadName = Thread.currentThread().getName();

        try {
            // 设置线程名称包含池名称
            Thread.currentThread().setName(originalThreadName + "-[" + poolName + "]");

            // 执行任务
            task.run();

            // 记录执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            ThreadPoolRegistry.ThreadPoolMetrics metrics = ThreadPoolRegistry.getMetrics(poolName);
            if (metrics != null) {
                metrics.recordTaskExecution(executionTime);
            }

            if (log.isDebugEnabled()) {
                log.debug("线程池 [{}] 任务执行完成，耗时: {}ms", poolName, executionTime);
            }

        } catch (Exception e) {
            log.error("线程池 [{}] 任务执行异常", poolName, e);
            throw e;
        } finally {
            // 恢复原始线程名称
            Thread.currentThread().setName(originalThreadName);
        }
    }

    /**
     * 创建带指标收集的任务包装器
     */
    public static Runnable wrap(Runnable task, String poolName) {
        if (task instanceof MetricTaskWrapper) {
            return task; // 避免重复包装
        }
        return new MetricTaskWrapper(task, poolName);
    }
}