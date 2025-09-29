package com.moon.cloud.threadpool.listener;

import com.moon.cloud.threadpool.config.ThreadPoolProperties;
import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * 线程池关闭监听器
 * 监听应用关闭事件，优雅关闭所有线程池
 *
 * @author moon
 * @since 1.0.0
 */
@Slf4j
public class ThreadPoolShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private final ThreadPoolProperties properties;

    public ThreadPoolShutdownListener(ThreadPoolProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("应用关闭事件触发，开始优雅关闭线程池");

        if (properties.isWaitForTasksToCompleteOnShutdown()) {
            log.info("等待所有任务完成后关闭线程池，超时时间: {}秒", properties.getShutdownTimeout());
            // 设置关闭超时时间
            ThreadPoolRegistry.setDefaultShutdownTimeout(properties.getShutdownTimeout());
        } else {
            log.info("立即关闭线程池，不等待任务完成");
            // 设置较短的超时时间
            ThreadPoolRegistry.setDefaultShutdownTimeout(5);
        }

        // 关闭所有线程池
        ThreadPoolRegistry.shutdownAll();

        log.info("所有线程池已关闭");
    }
}