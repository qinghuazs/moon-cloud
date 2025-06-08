package com.moon.cloud.threadpool.config;

import com.moon.cloud.threadpool.endpoint.MoonThreadPoolEndpoint;
import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 线程池 Actuator Endpoint 自动配置类
 * 
 * @author moon
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnClass({ThreadPoolRegistry.class})
@ConditionalOnProperty(
    prefix = "management.endpoint.threadpools", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
public class ThreadPoolActuatorConfiguration {

    /**
     * 注册线程池监控端点
     * 
     * @return MoonThreadPoolEndpoint
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public MoonThreadPoolEndpoint moonThreadPoolEndpoint() {
        log.info("启用 Moon 线程池 Actuator Endpoint: /actuator/threadpools");
        return new MoonThreadPoolEndpoint();
    }
}