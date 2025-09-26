package com.moon.cloud.appstore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * APP Store 限免应用推荐服务启动类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@SpringBootApplication
@MapperScan("com.moon.cloud.appstore.mapper")
public class AppStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppStoreApplication.class, args);
    }
}