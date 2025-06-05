package com.moon.cloud.business.gps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GPS服务启动类
 * 
 * @author mooncloud
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
public class GpsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GpsApplication.class, args);
    }
}