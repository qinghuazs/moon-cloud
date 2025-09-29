package com.moon.cloud.validator.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 验证器增强器
 * 用于增强验证器功能，如添加日志、统计等
 */
@Component
public class ValidatorEnhancer implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 可以在这里对验证器进行前置处理
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 可以在这里对验证器进行后置处理
        return bean;
    }
}