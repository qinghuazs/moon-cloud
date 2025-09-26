package com.mooncloud.shorturl.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 环境变量配置加载器
 * 在Spring应用启动时加载.env文件中的环境变量
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
public class EnvironmentConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // 加载.env文件
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // 将.env中的变量添加到Spring环境中
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envVars = new HashMap<>();

            dotenv.entries().forEach(entry -> {
                envVars.put(entry.getKey(), entry.getValue());
                // 设置系统属性，确保所有组件都能访问
                System.setProperty(entry.getKey(), entry.getValue());
            });

            // 添加到Spring环境的最高优先级
            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envVars));

            log.info("已加载环境变量配置，共{}个变量", envVars.size());

        } catch (Exception e) {
            log.warn("加载.env文件失败，将使用默认配置: {}", e.getMessage());
        }
    }
}