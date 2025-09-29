package com.moon.cloud.validator.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Moon Validator 自动配置类
 * 自动注册所有自定义验证器
 */
@AutoConfiguration
@ConditionalOnClass(LocalValidatorFactoryBean.class)
@ComponentScan(basePackages = "com.moon.cloud.validator")
public class MoonValidatorAutoConfiguration {

    /**
     * 配置验证器工厂
     *
     * @return LocalValidatorFactoryBean
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}