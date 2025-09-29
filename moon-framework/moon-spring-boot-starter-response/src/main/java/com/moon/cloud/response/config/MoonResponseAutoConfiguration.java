package com.moon.cloud.response.config;

import com.moon.cloud.response.handler.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Moon Response 自动配置类
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(
        prefix = "moon.response",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ComponentScan(basePackages = "com.moon.cloud.response")
public class MoonResponseAutoConfiguration {

    /**
     * 注册全局异常处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        log.info("Moon Response 全局异常处理器已启用");
        return new GlobalExceptionHandler();
    }

    /**
     * 配置信息
     */
    @Bean
    @ConditionalOnMissingBean
    public MoonResponseProperties moonResponseProperties() {
        return new MoonResponseProperties();
    }
}