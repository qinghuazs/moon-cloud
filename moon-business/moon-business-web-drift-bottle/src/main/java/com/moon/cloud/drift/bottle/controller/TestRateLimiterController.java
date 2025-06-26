package com.moon.cloud.drift.bottle.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试限流器控制器
 * 用于验证Resilience4j限流器功能
 */
@RestController
@RequestMapping("/test")
public class TestRateLimiterController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestRateLimiterController.class);
    
    /**
     * 测试限流器接口
     * 配置：每10秒允许1个请求
     */
    @GetMapping("/rate-limit")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> testRateLimit() {
        logger.info("限流器测试接口被调用，时间：{}", System.currentTimeMillis());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "请求成功！");
        response.put("timestamp", System.currentTimeMillis());
        response.put("data", "限流器测试通过");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 限流器fallback方法
     */
    public ResponseEntity<Map<String, Object>> rateLimitFallback(Exception ex) {
        logger.warn("请求被限流，触发fallback方法，异常：{}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "请求被限流！请稍后再试。");
        response.put("timestamp", System.currentTimeMillis());
        response.put("error", ex.getMessage());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 简单测试接口（无限流）
     */
    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleTest() {
        logger.info("简单测试接口被调用，时间：{}", System.currentTimeMillis());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "简单请求成功！");
        response.put("timestamp", System.currentTimeMillis());
        response.put("data", "无限流测试通过");
        
        return ResponseEntity.ok(response);
    }
}