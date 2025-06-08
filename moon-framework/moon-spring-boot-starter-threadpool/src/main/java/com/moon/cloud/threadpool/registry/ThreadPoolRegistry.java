package com.moon.cloud.threadpool.registry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.moon.cloud.threadpool.factory.MoonThreadPoolFactory;

public class ThreadPoolRegistry {

    private static final ConcurrentHashMap<String, ThreadPoolExecutor> registry = new ConcurrentHashMap<>();

    /**
     * 注册线程池
     * @param poolName
     * @param executor
     */
    public static void register(String poolName, ThreadPoolExecutor executor) {
        registry.put(poolName, executor);
    }

    /**
     * 获取线程池实例
     * @param poolName
     * @return
     */
    public static ThreadPoolExecutor getExecutor(String poolName) {
        return registry.get(poolName);
    }

    /**
     * 获取所有线程池名称
     * @return
     */
    public static Set<String> getAllPoolNames() {
        return registry.keySet();
    }

    public static void shutdown(String poolName) {
        ThreadPoolExecutor executor = registry.get(poolName);
        if (executor != null) {
            MoonThreadPoolFactory.shutdownGracefully(executor, 120);
        }
    }

    public static void shutdown(ThreadPoolExecutor executor) {
        if (executor != null) {
            MoonThreadPoolFactory.shutdownGracefully(executor, 120);
        }
    }
}
