package com.moon.cloud.threadpool.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * 线程池配置属性
 *
 * @author moon
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "moon.threadpool")
public class ThreadPoolProperties {

    /**
     * 是否启用线程池自动配置
     */
    private boolean enabled = true;

    /**
     * 是否启用监控
     */
    private boolean monitorEnabled = true;

    /**
     * 优雅关闭超时时间（秒）
     */
    private int shutdownTimeout = 60;

    /**
     * 是否等待任务完成后再关闭
     */
    private boolean waitForTasksToCompleteOnShutdown = true;

    /**
     * 是否预启动所有核心线程
     */
    private boolean prestartAllCoreThreads = false;

    /**
     * 线程名称前缀
     */
    private String threadNamePrefix = "moon-pool-";

    /**
     * 线程优先级（1-10）
     */
    private int threadPriority = Thread.NORM_PRIORITY;

    /**
     * 是否设置为守护线程
     */
    private boolean daemon = false;

    /**
     * CPU 密集型线程池配置
     */
    private PoolConfig cpuIntensive = new PoolConfig();

    /**
     * IO 密集型线程池配置
     */
    private PoolConfig ioIntensive = new PoolConfig();

    /**
     * 自定义线程池配置
     */
    private Map<String, PoolConfig> custom = new HashMap<>();

    /**
     * 线程池配置
     */
    @Data
    public static class PoolConfig {
        /**
         * 核心线程数，默认为可用处理器数量
         */
        private Integer corePoolSize;

        /**
         * 最大线程数
         */
        private Integer maximumPoolSize;

        /**
         * 空闲线程存活时间（秒）
         */
        private long keepAliveTime = 60L;

        /**
         * 队列容量
         */
        private int queueCapacity = 1000;

        /**
         * 是否允许核心线程超时
         */
        private boolean allowCoreThreadTimeOut = false;

        /**
         * 拒绝策略类型
         * CALLER_RUNS: 调用者运行
         * ABORT: 中止
         * DISCARD: 丢弃
         * DISCARD_OLDEST: 丢弃最老的
         * RETRY: 重试（自定义）
         */
        private RejectedExecutionHandlerType rejectedExecutionHandler = RejectedExecutionHandlerType.RETRY;
    }

    /**
     * 拒绝策略类型枚举
     */
    public enum RejectedExecutionHandlerType {
        CALLER_RUNS,
        ABORT,
        DISCARD,
        DISCARD_OLDEST,
        RETRY
    }
}