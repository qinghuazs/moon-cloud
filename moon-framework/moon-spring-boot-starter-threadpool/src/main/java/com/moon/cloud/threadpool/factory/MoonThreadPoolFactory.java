package com.moon.cloud.threadpool.factory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池工厂类，支持创建IO密集型和CPU密集型线程池
 */
public class MoonThreadPoolFactory {

    /**
     * 创建IO密集型线程池
     * IO密集型任务特点：大量的网络请求、文件读写等，线程经常处于阻塞状态
     * 线程数配置：通常设置为 2 * CPU核心数，因为IO操作会阻塞线程
     * 
     * @param threadNamePrefix 线程名称前缀
     * @return ExecutorService
     */
    public static ExecutorService createIOIntensiveThreadPool(String threadNamePrefix) {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maximumPoolSize = corePoolSize * 2;
        long keepAliveTime = 60L;
        
        return new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            createThreadFactory(threadNamePrefix + "-io-"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 创建CPU密集型线程池
     * CPU密集型任务特点：大量的计算操作，线程持续占用CPU资源
     * 线程数配置：通常设置为 CPU核心数 + 1，避免过多线程导致上下文切换开销
     * 
     * @param threadNamePrefix 线程名称前缀
     * @return ExecutorService
     */
    public static ExecutorService createCPUIntensiveThreadPool(String threadNamePrefix) {
        //当某个线程因为页缺失（page fault）或其它短暂阻塞（如系统调用）时，额外的线程可以立即填补这个空档，保持CPU处于忙碌状态。
        int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
        int maximumPoolSize = corePoolSize;
        long keepAliveTime = 60L;
        
        return new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            createThreadFactory(threadNamePrefix + "-cpu-"),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * 创建自定义线程池
     * 
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime 线程空闲时间
     * @param queueCapacity 队列容量
     * @param threadNamePrefix 线程名称前缀
     * @return ExecutorService
     */
    public static ExecutorService createCustomThreadPool(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            int queueCapacity,
            String threadNamePrefix) {
        
        return new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            createThreadFactory(threadNamePrefix + "-custom-"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
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
