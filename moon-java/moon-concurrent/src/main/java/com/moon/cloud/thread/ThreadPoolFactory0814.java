package com.moon.cloud.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolFactory0814 implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group = Thread.currentThread().getThreadGroup();

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(group, r);
        thread.setName("moon-thread-pool-0814-" + threadNumber.getAndIncrement());
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("线程" + t.getName() + "发生异常");
            }
        });
        return thread;
    }
}
