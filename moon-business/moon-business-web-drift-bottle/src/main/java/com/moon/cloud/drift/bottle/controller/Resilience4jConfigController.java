package com.moon.cloud.drift.bottle.controller;

import com.moon.cloud.drift.bottle.config.Resilience4jConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Resilience4j 配置管理控制器
 * 提供动态调整熔断器、限流器、重试等配置的REST API
 */
@RestController
@RequestMapping("/api/resilience4j")
public class Resilience4jConfigController {

    private final Resilience4jConfigManager configManager;

    @Autowired
    public Resilience4jConfigController(Resilience4jConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 动态更新熔断器配置
     */
    @PutMapping("/circuit-breaker/{name}/config")
    public ResponseEntity<Map<String, Object>> updateCircuitBreakerConfig(
            @PathVariable String name,
            @RequestBody CircuitBreakerConfigRequest request) {
        
        try {
            configManager.updateCircuitBreakerConfig(
                    name,
                    request.getSlidingWindowSize(),
                    request.getFailureRateThreshold(),
                    request.getWaitDurationInOpenState()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "熔断器配置更新成功");
            response.put("name", name);
            response.put("config", request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "熔断器配置更新失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 动态更新限流器配置
     */
    @PutMapping("/rate-limiter/{name}/config")
    public ResponseEntity<Map<String, Object>> updateRateLimiterConfig(
            @PathVariable String name,
            @RequestBody RateLimiterConfigRequest request) {
        
        try {
            configManager.updateRateLimiterConfig(
                    name,
                    request.getLimitForPeriod(),
                    request.getLimitRefreshPeriod(),
                    request.getTimeoutDuration()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "限流器配置更新成功");
            response.put("name", name);
            response.put("config", request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "限流器配置更新失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 动态更新重试配置
     */
    @PutMapping("/retry/{name}/config")
    public ResponseEntity<Map<String, Object>> updateRetryConfig(
            @PathVariable String name,
            @RequestBody RetryConfigRequest request) {
        
        try {
            configManager.updateRetryConfig(
                    name,
                    request.getMaxAttempts(),
                    request.getWaitDuration()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "重试配置更新成功");
            response.put("name", name);
            response.put("config", request);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "重试配置更新失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取熔断器状态
     */
    @GetMapping("/circuit-breaker/{name}/state")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerState(@PathVariable String name) {
        String state = configManager.getCircuitBreakerState(name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("name", name);
        response.put("state", state);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取限流器指标
     */
    @GetMapping("/rate-limiter/{name}/metrics")
    public ResponseEntity<Map<String, Object>> getRateLimiterMetrics(@PathVariable String name) {
        String metrics = configManager.getRateLimiterMetrics(name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("name", name);
        response.put("metrics", metrics);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 重置熔断器状态
     */
    @PostMapping("/circuit-breaker/{name}/reset")
    public ResponseEntity<Map<String, Object>> resetCircuitBreaker(@PathVariable String name) {
        try {
            configManager.resetCircuitBreaker(name);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "熔断器重置成功");
            response.put("name", name);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "熔断器重置失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有配置状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // 熔断器状态
        Map<String, String> circuitBreakerStatus = new HashMap<>();
        circuitBreakerStatus.put("drift-bottle", configManager.getCircuitBreakerState("drift-bottle"));
        
        // 限流器指标
        Map<String, String> rateLimiterMetrics = new HashMap<>();
        rateLimiterMetrics.put("drift-bottle", configManager.getRateLimiterMetrics("drift-bottle"));
        
        response.put("circuitBreakers", circuitBreakerStatus);
        response.put("rateLimiters", rateLimiterMetrics);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 熔断器配置请求对象
     */
    public static class CircuitBreakerConfigRequest {
        private int slidingWindowSize;
        private float failureRateThreshold;
        private long waitDurationInOpenState;

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public long getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        public void setWaitDurationInOpenState(long waitDurationInOpenState) {
            this.waitDurationInOpenState = waitDurationInOpenState;
        }
    }

    /**
     * 限流器配置请求对象
     */
    public static class RateLimiterConfigRequest {
        private int limitForPeriod;
        private int limitRefreshPeriod;
        private long timeoutDuration;

        public int getLimitForPeriod() {
            return limitForPeriod;
        }

        public void setLimitForPeriod(int limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
        }

        public int getLimitRefreshPeriod() {
            return limitRefreshPeriod;
        }

        public void setLimitRefreshPeriod(int limitRefreshPeriod) {
            this.limitRefreshPeriod = limitRefreshPeriod;
        }

        public long getTimeoutDuration() {
            return timeoutDuration;
        }

        public void setTimeoutDuration(long timeoutDuration) {
            this.timeoutDuration = timeoutDuration;
        }
    }

    /**
     * 重试配置请求对象
     */
    public static class RetryConfigRequest {
        private int maxAttempts;
        private long waitDuration;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getWaitDuration() {
            return waitDuration;
        }

        public void setWaitDuration(long waitDuration) {
            this.waitDuration = waitDuration;
        }
    }
}