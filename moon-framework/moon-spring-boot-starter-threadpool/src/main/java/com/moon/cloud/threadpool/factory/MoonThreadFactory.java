package com.moon.cloud.threadpool.factory;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程工厂
 *
 * @author moon
 * @since 1.0.0
 */
@Slf4j
public class MoonThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private final int priority;

    public MoonThreadFactory(String poolName) {
        this(poolName, false, Thread.NORM_PRIORITY);
    }

    public MoonThreadFactory(String poolName, boolean daemon) {
        this(poolName, daemon, Thread.NORM_PRIORITY);
    }

    public MoonThreadFactory(String poolName, boolean daemon, int priority) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = "moon-" + poolName + "-thread-";
        this.daemon = daemon;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

        // 设置是否为守护线程
        if (t.isDaemon() != daemon) {
            t.setDaemon(daemon);
        }

        // 设置线程优先级
        if (t.getPriority() != priority) {
            t.setPriority(priority);
        }

        // 设置未捕获异常处理器
        t.setUncaughtExceptionHandler((thread, ex) -> {
            log.error("线程 {} 发生未捕获异常", thread.getName(), ex);
        });

        if (log.isDebugEnabled()) {
            log.debug("创建新线程: {}, 守护线程: {}, 优先级: {}", t.getName(), daemon, priority);
        }

        return t;
    }

    /**
     * 获取当前线程池创建的线程数
     */
    public int getThreadCount() {
        return threadNumber.get() - 1;
    }
}