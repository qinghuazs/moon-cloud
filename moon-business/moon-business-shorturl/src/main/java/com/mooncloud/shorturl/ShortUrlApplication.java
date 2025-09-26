package com.mooncloud.shorturl;

import com.mooncloud.shorturl.config.EnvironmentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 短URL系统主应用类
 *
 * @author mooncloud
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class ShortUrlApplication {

    public static void main(String[] args) {
        // 创建SpringApplication实例
        SpringApplication application = new SpringApplication(ShortUrlApplication.class);

        // 添加环境变量初始化器
        application.addInitializers(new EnvironmentConfig());

        // 启动应用
        application.run(args);
    }
}