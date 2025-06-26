package com.moon.cloud.drift.bottle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

/**
 * 简单的限流器测试应用
 */
@SpringBootApplication
@RestController
public class SimpleRateLimiterTest {
    
    public static void main(String[] args) {
        SpringApplication.run(SimpleRateLimiterTest.class, args);
    }
    
    @GetMapping("/test")
    @RateLimiter(name = "test-limiter", fallbackMethod = "fallback")
    public String test() {
        return "请求成功！时间：" + System.currentTimeMillis();
    }
    
    public String fallback(Exception ex) {
        return "请求被限流了！" + ex.getMessage();
    }
    
    @GetMapping("/simple")
    public String simple() {
        return "简单请求，无限流";
    }
}