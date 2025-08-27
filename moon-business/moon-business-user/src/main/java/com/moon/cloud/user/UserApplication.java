package com.moon.cloud.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 用户管理系统启动类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@MapperScan("com.moon.cloud.user.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}