package com.moon.cloud.threadpool.config;

import com.moon.cloud.threadpool.factory.MoonThreadPoolFactory;
import com.moon.cloud.threadpool.listener.ThreadPoolShutdownListener;
import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import com.moon.cloud.threadpool.rejector.RetryRejectedExecutionConfig;
import com.moon.cloud.threadpool.rejector.RetryRejectedExecutionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Moon 线程池自动配置类
 *
 * @author moon
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({ThreadPoolProperties.class, RetryRejectedExecutionConfig.class})
@ComponentScan(basePackages = "com.moon.cloud.threadpool")
@Import({ThreadPoolActuatorConfiguration.class})
@ConditionalOnProperty(
        prefix = "moon.threadpool",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MoonThreadPoolAutoConfiguration {

    private final ThreadPoolProperties properties;
    private final MoonThreadPoolFactory threadPoolFactory;

    public MoonThreadPoolAutoConfiguration(ThreadPoolProperties properties,
                                          MoonThreadPoolFactory threadPoolFactory) {
        this.properties = properties;
        this.threadPoolFactory = threadPoolFactory;
    }

    @PostConstruct
    public void init() {
        log.info("Moon 线程池自动配置启动");

        // 设置默认关闭超时时间
        ThreadPoolRegistry.setDefaultShutdownTimeout(properties.getShutdownTimeout());

        // 根据配置自动创建线程池
        createCustomThreadPools();

        log.info("Moon 线程池自动配置完成，共注册 {} 个线程池",
                ThreadPoolRegistry.getAllPoolNames().size());
    }

    /**
     * 创建自定义线程池
     */
    private void createCustomThreadPools() {
        Map<String, ThreadPoolProperties.PoolConfig> customPools = properties.getCustom();
        if (customPools != null && !customPools.isEmpty()) {
            customPools.forEach((poolName, config) -> {
                try {
                    ThreadPoolExecutor executor = threadPoolFactory.createThreadPoolFromConfig(poolName, config);
                    log.info("自动创建自定义线程池: {}", poolName);
                } catch (Exception e) {
                    log.error("创建自定义线程池 {} 失败", poolName, e);
                }
            });
        }
    }

    /**
     * 注册线程池工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public MoonThreadPoolFactory moonThreadPoolFactory(RetryRejectedExecutionConfig retryConfig) {
        return new MoonThreadPoolFactory(retryConfig);
    }

    /**
     * 注册重试拒绝策略配置
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryRejectedExecutionConfig retryRejectedExecutionConfig() {
        return new RetryRejectedExecutionConfig();
    }

    /**
     * 注册重试拒绝处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryRejectedExecutionHandler retryRejectedExecutionHandler(RetryRejectedExecutionConfig config) {
        return new RetryRejectedExecutionHandler(config);
    }

    /**
     * 注册线程池关闭监听器
     */
    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolShutdownListener threadPoolShutdownListener() {
        return new ThreadPoolShutdownListener(properties);
    }
}