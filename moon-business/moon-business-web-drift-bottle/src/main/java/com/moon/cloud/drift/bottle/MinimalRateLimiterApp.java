package com.moon.cloud.drift.bottle;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 最简化的限流器测试应用
 * 排除数据库相关的自动配置
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@RestController
public class MinimalRateLimiterApp {
    
    public static void main(String[] args) {
        System.setProperty("server.port", "8084");
        SpringApplication.run(MinimalRateLimiterApp.class, args);
    }
    
    @GetMapping("/test-rate-limit")
    @RateLimiter(name = "test-limiter", fallbackMethod = "fallback")
    public Map<String, Object> testRateLimit() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "请求成功！");
        response.put("timestamp", System.currentTimeMillis());
        System.out.println("限流器测试接口被调用，时间：" + System.currentTimeMillis());
        return response;
    }
    
    public Map<String, Object> fallback(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "请求被限流！");
        response.put("timestamp", System.currentTimeMillis());
        response.put("error", ex.getMessage());
        System.out.println("请求被限流，触发fallback：" + ex.getMessage());
        return response;
    }
    
    @GetMapping("/test-simple")
    public Map<String, Object> testSimple() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "简单请求成功！");
        response.put("timestamp", System.currentTimeMillis());
        System.out.println("简单测试接口被调用，时间：" + System.currentTimeMillis());
        return response;
    }
}