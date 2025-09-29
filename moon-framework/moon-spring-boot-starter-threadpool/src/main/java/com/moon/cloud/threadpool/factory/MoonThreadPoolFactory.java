package com.moon.cloud.threadpool.factory;

import com.moon.cloud.threadpool.config.ThreadPoolProperties;
import com.moon.cloud.threadpool.rejector.RetryRejectedExecutionHandler;
import com.moon.cloud.threadpool.rejector.RetryRejectedExecutionConfig;
import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Moon 线程池工厂
 * 提供创建不同类型线程池的工厂方法
 *
 * @author moon
 * @since 1.0.0
 */
@Slf4j
@Component
public class MoonThreadPoolFactory {

    private final RetryRejectedExecutionConfig retryConfig;
    private ThreadPoolProperties properties;

    public MoonThreadPoolFactory(RetryRejectedExecutionConfig retryConfig) {
        this.retryConfig = retryConfig;
        // 创建默认配置
        this.properties = new ThreadPoolProperties();
    }

    @Autowired(required = false)
    public void setProperties(ThreadPoolProperties properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }

    /**
     * 创建IO密集型线程池
     * IO密集型任务特点：CPU计算少，等待IO操作多
     * 线程数设置：2 * CPU核心数
     */
    public ThreadPoolExecutor createIoIntensiveThreadPool(String poolName) {
        ThreadPoolProperties.PoolConfig config = properties.getIoIntensive();

        int corePoolSize = config.getCorePoolSize() != null ?
                config.getCorePoolSize() : Runtime.getRuntime().availableProcessors() * 2;
        int maximumPoolSize = config.getMaximumPoolSize() != null ?
                config.getMaximumPoolSize() : corePoolSize * 2;
        long keepAliveTime = config.getKeepAliveTime();
        int queueCapacity = config.getQueueCapacity();

        ThreadPoolExecutor executor = createThreadPool(
                poolName,
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                createRejectedHandler(config.getRejectedExecutionHandler()),
                properties.isDaemon(),
                properties.getThreadPriority()
        );

        // 配置额外参数
        configureThreadPool(executor, config);

        log.info("创建IO密集型线程池 [{}]: 核心线程数={}, 最大线程数={}, 存活时间={}秒, 队列容量={}",
                poolName, corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity);

        return executor;
    }

    /**
     * 创建CPU密集型线程池
     * CPU密集型任务特点：大量计算，CPU使用率高
     * 线程数设置：CPU核心数 + 1
     */
    public ThreadPoolExecutor createCpuIntensiveThreadPool(String poolName) {
        ThreadPoolProperties.PoolConfig config = properties.getCpuIntensive();

        int corePoolSize = config.getCorePoolSize() != null ?
                config.getCorePoolSize() : Runtime.getRuntime().availableProcessors() + 1;
        int maximumPoolSize = config.getMaximumPoolSize() != null ?
                config.getMaximumPoolSize() : corePoolSize;
        long keepAliveTime = config.getKeepAliveTime();
        int queueCapacity = config.getQueueCapacity();

        ThreadPoolExecutor executor = createThreadPool(
                poolName,
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                createRejectedHandler(config.getRejectedExecutionHandler()),
                properties.isDaemon(),
                properties.getThreadPriority()
        );

        // 配置额外参数
        configureThreadPool(executor, config);

        log.info("创建CPU密集型线程池 [{}]: 核心线程数={}, 最大线程数={}, 存活时间={}秒, 队列容量={}",
                poolName, corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity);

        return executor;
    }

    /**
     * 创建自定义线程池
     */
    public ThreadPoolExecutor createCustomThreadPool(String poolName,
                                                     int corePoolSize,
                                                     int maximumPoolSize,
                                                     long keepAliveTime,
                                                     TimeUnit unit,
                                                     BlockingQueue<Runnable> workQueue,
                                                     RejectedExecutionHandler handler) {
        ThreadPoolExecutor executor = createThreadPool(
                poolName,
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                handler != null ? handler : new RetryRejectedExecutionHandler(retryConfig),
                properties.isDaemon(),
                properties.getThreadPriority()
        );

        log.info("创建自定义线程池 [{}]: 核心线程数={}, 最大线程数={}, 存活时间={}{}",
                poolName, corePoolSize, maximumPoolSize, keepAliveTime, unit);

        return executor;
    }

    /**
     * 创建定时任务线程池
     */
    public ScheduledThreadPoolExecutor createScheduledThreadPool(String poolName, int corePoolSize) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                corePoolSize,
                new MoonThreadFactory(poolName, properties.isDaemon(), properties.getThreadPriority()),
                new RetryRejectedExecutionHandler(retryConfig)
        );

        // 注册到线程池注册中心
        ThreadPoolRegistry.register(poolName, executor);

        log.info("创建定时任务线程池 [{}]: 核心线程数={}", poolName, corePoolSize);

        return executor;
    }

    /**
     * 根据配置创建线程池
     */
    public ThreadPoolExecutor createThreadPoolFromConfig(String poolName,
                                                         ThreadPoolProperties.PoolConfig config) {
        int corePoolSize = config.getCorePoolSize() != null ?
                config.getCorePoolSize() : Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = config.getMaximumPoolSize() != null ?
                config.getMaximumPoolSize() : corePoolSize * 2;

        ThreadPoolExecutor executor = createThreadPool(
                poolName,
                corePoolSize,
                maximumPoolSize,
                config.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(config.getQueueCapacity()),
                createRejectedHandler(config.getRejectedExecutionHandler()),
                properties.isDaemon(),
                properties.getThreadPriority()
        );

        // 配置额外参数
        configureThreadPool(executor, config);

        log.info("根据配置创建线程池 [{}]: 核心线程数={}, 最大线程数={}, 存活时间={}秒, 队列容量={}",
                poolName, corePoolSize, maximumPoolSize, config.getKeepAliveTime(), config.getQueueCapacity());

        return executor;
    }

    /**
     * 创建线程池核心方法
     */
    private ThreadPoolExecutor createThreadPool(String poolName,
                                               int corePoolSize,
                                               int maximumPoolSize,
                                               long keepAliveTime,
                                               TimeUnit unit,
                                               BlockingQueue<Runnable> workQueue,
                                               RejectedExecutionHandler handler,
                                               boolean daemon,
                                               int priority) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                new MoonThreadFactory(poolName, daemon, priority),
                handler
        );

        // 注册到线程池注册中心
        ThreadPoolRegistry.register(poolName, executor);

        // 预启动所有核心线程
        if (properties.isPrestartAllCoreThreads()) {
            int prestartedThreads = executor.prestartAllCoreThreads();
            log.info("线程池 [{}] 预启动了 {} 个核心线程", poolName, prestartedThreads);
        }

        return executor;
    }

    /**
     * 配置线程池额外参数
     */
    private void configureThreadPool(ThreadPoolExecutor executor, ThreadPoolProperties.PoolConfig config) {
        // 设置核心线程是否允许超时
        executor.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeOut());
    }

    /**
     * 创建拒绝处理器
     */
    private RejectedExecutionHandler createRejectedHandler(ThreadPoolProperties.RejectedExecutionHandlerType type) {
        if (type == null) {
            return new RetryRejectedExecutionHandler(retryConfig);
        }

        switch (type) {
            case CALLER_RUNS:
                return new ThreadPoolExecutor.CallerRunsPolicy();
            case ABORT:
                return new ThreadPoolExecutor.AbortPolicy();
            case DISCARD:
                return new ThreadPoolExecutor.DiscardPolicy();
            case DISCARD_OLDEST:
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            case RETRY:
            default:
                return new RetryRejectedExecutionHandler(retryConfig);
        }
    }
}