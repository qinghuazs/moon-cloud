package com.moon.cloud.drift.bottle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 漂流瓶应用启动类
 * 
 * 功能特性：
 * - 漂流瓶投放与捡取
 * - 随机传递机制
 * - 回复功能
 * - 熔断和限流保护
 * - 数据统计
 * 
 * @author Moon Cloud
 * @since 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class DriftBottleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriftBottleApplication.class, args);
        System.out.println("");
        System.out.println("  ____       _  __ _     ____        _   _   _      ");
        System.out.println(" |  _ \\ _ __(_)/ _| |_  | __ )  ___ | |_| |_| | ___ ");
        System.out.println(" | | | | '__| | |_| __| |  _ \\ / _ \\| __| __| |/ _ \\");
        System.out.println(" | |_| | |  | |  _| |_  | |_) | (_) | |_| |_| |  __/");
        System.out.println(" |____/|_|  |_|_|  \\__| |____/ \\___/ \\__|\\__|_|\\___|\n");
        System.out.println(" :: Moon Cloud Drift Bottle Application :: (v1.0.0)");
        System.out.println("");
        System.out.println(" 🌊 漂流瓶应用已启动");
        System.out.println(" 📱 应用访问地址: http://localhost:8083/drift-bottle");
        System.out.println(" 🗄️  H2数据库控制台: http://localhost:8083/drift-bottle/h2-console");
        System.out.println(" 📊 监控端点: http://localhost:8083/drift-bottle/actuator");
        System.out.println(" 📋 API文档: 请查看 README.md 文件");
        System.out.println("");
    }
}
