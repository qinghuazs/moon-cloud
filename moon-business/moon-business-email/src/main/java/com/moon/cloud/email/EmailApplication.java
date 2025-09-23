package com.moon.cloud.email;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 邮件服务启动类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.moon.cloud.email.mapper")
@EnableAsync
@EnableScheduling
public class EmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailApplication.class, args);
    }
}