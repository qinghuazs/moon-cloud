package com.moon.cloud.threadpool.registry;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 线程池注册表
 * 统一管理所有线程池，便于监控和优雅关闭
 *
 * @author moon
 * @since 1.0.0
 */
@Slf4j
public class ThreadPoolRegistry {

    private static final Map<String, ThreadPoolExecutor> POOL_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<String, ThreadPoolMetrics> POOL_METRICS = new ConcurrentHashMap<>();
    private static long defaultShutdownTimeout = 60L; // 默认关闭超时时间（秒）

    /**
     * 设置默认关闭超时时间
     */
    public static void setDefaultShutdownTimeout(long timeout) {
        defaultShutdownTimeout = timeout;
    }

    /**
     * 注册线程池
     */
    public static void register(String poolName, ThreadPoolExecutor executor) {
        if (POOL_REGISTRY.containsKey(poolName)) {
            log.warn("线程池 {} 已存在，将被覆盖", poolName);
        }
        POOL_REGISTRY.put(poolName, executor);
        POOL_METRICS.put(poolName, new ThreadPoolMetrics(poolName));
        log.info("注册线程池: {}", poolName);
    }

    /**
     * 获取线程池
     */
    public static ThreadPoolExecutor getExecutor(String poolName) {
        return POOL_REGISTRY.get(poolName);
    }

    /**
     * 获取线程池指标
     */
    public static ThreadPoolMetrics getMetrics(String poolName) {
        return POOL_METRICS.get(poolName);
    }

    /**
     * 获取所有线程池名称
     */
    public static Set<String> getAllPoolNames() {
        return new HashSet<>(POOL_REGISTRY.keySet());
    }

    /**
     * 获取所有线程池
     */
    public static Map<String, ThreadPoolExecutor> getAllPools() {
        return new HashMap<>(POOL_REGISTRY);
    }

    /**
     * 检查线程池是否存在
     */
    public static boolean exists(String poolName) {
        return POOL_REGISTRY.containsKey(poolName);
    }

    /**
     * 优雅关闭指定线程池
     */
    public static void shutdown(ThreadPoolExecutor executor) {
        shutdown(executor, defaultShutdownTimeout);
    }

    /**
     * 优雅关闭指定线程池（自定义超时）
     */
    public static void shutdown(ThreadPoolExecutor executor, long timeoutSeconds) {
        if (executor == null || executor.isShutdown()) {
            return;
        }

        String poolName = getPoolName(executor);
        log.info("开始关闭线程池: {}", poolName != null ? poolName : "unknown");

        try {
            executor.shutdown();
            if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("线程池 {} 在 {} 秒内未能正常关闭，尝试强制关闭", poolName, timeoutSeconds);
                List<Runnable> pendingTasks = executor.shutdownNow();
                log.warn("线程池 {} 强制关闭，丢弃 {} 个待执行任务", poolName, pendingTasks.size());

                if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    log.error("线程池 {} 未能正常关闭", poolName);
                }
            } else {
                log.info("线程池 {} 已优雅关闭", poolName);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("关闭线程池 {} 时被中断", poolName);
        }
    }

    /**
     * 根据线程池实例获取名称
     */
    private static String getPoolName(ThreadPoolExecutor executor) {
        return POOL_REGISTRY.entrySet().stream()
                .filter(entry -> entry.getValue() == executor)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 关闭指定名称的线程池
     */
    public static void shutdown(String poolName) {
        ThreadPoolExecutor executor = POOL_REGISTRY.get(poolName);
        if (executor != null) {
            shutdown(executor);
            POOL_REGISTRY.remove(poolName);
            POOL_METRICS.remove(poolName);
        } else {
            log.warn("线程池 {} 不存在", poolName);
        }
    }

    /**
     * 批量关闭线程池
     */
    public static void shutdownBatch(Collection<String> poolNames) {
        log.info("批量关闭线程池: {}", poolNames);
        poolNames.forEach(ThreadPoolRegistry::shutdown);
    }

    /**
     * 关闭所有线程池
     */
    public static void shutdownAll() {
        log.info("开始关闭所有线程池，共 {} 个", POOL_REGISTRY.size());
        List<String> poolNames = new ArrayList<>(POOL_REGISTRY.keySet());
        poolNames.forEach(ThreadPoolRegistry::shutdown);
        POOL_REGISTRY.clear();
        POOL_METRICS.clear();
        log.info("所有线程池已关闭");
    }

    /**
     * 获取线程池统计信息
     */
    public static Map<String, ThreadPoolStats> getStatistics() {
        return POOL_REGISTRY.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> ThreadPoolStats.from(entry.getValue())
                ));
    }

    /**
     * 线程池统计信息
     */
    public static class ThreadPoolStats {
        public final int corePoolSize;
        public final int maximumPoolSize;
        public final int poolSize;
        public final int activeCount;
        public final long taskCount;
        public final long completedTaskCount;
        public final int queueSize;
        public final double utilizationRate;

        private ThreadPoolStats(ThreadPoolExecutor executor) {
            this.corePoolSize = executor.getCorePoolSize();
            this.maximumPoolSize = executor.getMaximumPoolSize();
            this.poolSize = executor.getPoolSize();
            this.activeCount = executor.getActiveCount();
            this.taskCount = executor.getTaskCount();
            this.completedTaskCount = executor.getCompletedTaskCount();
            this.queueSize = executor.getQueue().size();
            this.utilizationRate = maximumPoolSize > 0 ?
                    (double) activeCount / maximumPoolSize * 100 : 0;
        }

        public static ThreadPoolStats from(ThreadPoolExecutor executor) {
            return new ThreadPoolStats(executor);
        }
    }

    /**
     * 线程池指标
     */
    public static class ThreadPoolMetrics {
        private final String poolName;
        private long totalExecutionTime = 0;
        private long totalTaskCount = 0;
        private long maxExecutionTime = 0;
        private long minExecutionTime = Long.MAX_VALUE;

        public ThreadPoolMetrics(String poolName) {
            this.poolName = poolName;
        }

        public synchronized void recordTaskExecution(long executionTime) {
            totalExecutionTime += executionTime;
            totalTaskCount++;
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
            minExecutionTime = Math.min(minExecutionTime, executionTime);
        }

        public double getAverageExecutionTime() {
            return totalTaskCount > 0 ? (double) totalExecutionTime / totalTaskCount : 0;
        }

        public long getMaxExecutionTime() {
            return maxExecutionTime;
        }

        public long getMinExecutionTime() {
            return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
        }

        public long getTotalTaskCount() {
            return totalTaskCount;
        }

        public String getPoolName() {
            return poolName;
        }
    }
}