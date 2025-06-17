package com.moon.cloud.eureka.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka客户端和服务端应用启动类
 * 既可以作为Eureka客户端注册到其他注册中心，也可以作为Eureka服务端提供注册中心服务
 * 
 * 注意：在Spring Cloud 2020.x及以后版本中，@EnableEurekaClient注解已被弃用
 * 只需要在classpath中包含spring-cloud-starter-netflix-eureka-client依赖即可自动启用客户端功能
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaClientServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientServerApplication.class, args);
    }
}