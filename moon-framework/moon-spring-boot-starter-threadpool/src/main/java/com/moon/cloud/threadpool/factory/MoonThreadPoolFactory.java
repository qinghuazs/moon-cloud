package com.moon.cloud.threadpool.factory;

import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import com.moon.cloud.threadpool.rejector.RetryRejectedExecutionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池工厂类，支持创建IO密集型和CPU密集型线程池
 */
@Slf4j
public class MoonThreadPoolFactory {

    /**
     * 创建IO密集型线程池（使用重试拒绝策略）
     * IO密集型任务特点：大量的网络请求、文件读写等，线程经常处于阻塞状态
     * 线程数配置：通常设置为 2 * CPU核心数，因为IO操作会阻塞线程
     * 
     * @param threadNamePrefix 线程名称前缀
     * @param retryHandler 重试拒绝策略处理器
     * @return ExecutorService
     */
    public static ExecutorService createIOIntensiveThreadPoolWithRetry(String threadNamePrefix, 
                                                                       RetryRejectedExecutionHandler retryHandler) {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maximumPoolSize = corePoolSize * 2;
        long keepAliveTime = 60L;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                createThreadFactory(threadNamePrefix + "-io-"),
                retryHandler != null ? retryHandler : new ThreadPoolExecutor.CallerRunsPolicy()
        );
        ThreadPoolRegistry.register(threadNamePrefix, executor);
        return executor;
    }

    /**
     * 创建CPU密集型线程池（使用重试拒绝策略）
     * CPU密集型任务特点：大量的计算操作，线程持续占用CPU资源
     * 线程数配置：通常设置为 CPU核心数 + 1，避免过多线程导致上下文切换开销
     * 
     * @param threadNamePrefix 线程名称前缀
     * @param retryHandler 重试拒绝策略处理器
     * @return ExecutorService
     */
    public static ExecutorService createCPUIntensiveThreadPoolWithRetry(String threadNamePrefix,
                                                                        RetryRejectedExecutionHandler retryHandler) {
        //当某个线程因为页缺失（page fault）或其它短暂阻塞（如系统调用）时，额外的线程可以立即填补这个空档，保持CPU处于忙碌状态。
        int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
        int maximumPoolSize = corePoolSize;
        long keepAliveTime = 60L;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                createThreadFactory(threadNamePrefix + "-cpu-"),
                retryHandler != null ? retryHandler : new ThreadPoolExecutor.AbortPolicy()
        );
        ThreadPoolRegistry.register(threadNamePrefix, executor);
        return executor;
    }

    /**
     * 创建自定义线程池（使用重试拒绝策略）
     * 
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime 线程空闲时间
     * @param queueCapacity 队列容量
     * @param threadNamePrefix 线程名称前缀
     * @param retryHandler 重试拒绝策略处理器
     * @return ExecutorService
     */
    public static ExecutorService createCustomThreadPoolWithRetry(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            int queueCapacity,
            String threadNamePrefix,
            RetryRejectedExecutionHandler retryHandler) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                createThreadFactory(threadNamePrefix + "-custom-"),
                retryHandler != null ? retryHandler : new ThreadPoolExecutor.CallerRunsPolicy()
        );
        ThreadPoolRegistry.register(threadNamePrefix, executor);
        return executor;
    }

    /**
     * 创建线程工厂
     * 
     * @param namePrefix 线程名称前缀
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String namePrefix) {
        return new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                return thread;
            }
        };
    }

    /**
     * 优雅关闭线程池
     * 
     * @param executor 要关闭的线程池
     * @param timeoutSeconds 等待超时时间（秒）
     */
    public static void shutdownGracefully(ExecutorService executor, long timeoutSeconds) {
        if (executor == null || executor.isShutdown()) {
            return;
        }
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    System.err.println("线程池未能正常关闭");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
