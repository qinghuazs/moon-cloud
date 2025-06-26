package com.moon.cloud.drift.bottle.controller;

import com.moon.cloud.drift.bottle.service.DriftBottleResilienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 漂流瓶测试控制器
 * 提供测试接口来演示Resilience4j动态配置的效果
 */
//@RestController
//@RequestMapping("/api/drift-bottle/test")
public class DriftBottleTestController {

    private final DriftBottleResilienceService resilienceService;

    @Autowired
    public DriftBottleTestController(DriftBottleResilienceService resilienceService) {
        this.resilienceService = resilienceService;
    }

    /**
     * 测试熔断器保护
     */
    @GetMapping("/circuit-breaker")
    public ResponseEntity<Map<String, Object>> testCircuitBreaker(
            @RequestParam(defaultValue = "测试操作") String operation) {
        
        String result = resilienceService.executeWithCircuitBreaker(operation);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "circuit-breaker");
        response.put("operation", operation);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试限流器保护
     */
    @GetMapping("/rate-limiter")
    public ResponseEntity<Map<String, Object>> testRateLimiter(
            @RequestParam(defaultValue = "限流测试") String operation) {
        
        String result = resilienceService.executeWithRateLimiter(operation);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "rate-limiter");
        response.put("operation", operation);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试重试机制
     */
    @GetMapping("/retry")
    public ResponseEntity<Map<String, Object>> testRetry(
            @RequestParam(defaultValue = "重试测试") String operation) {
        
        String result = resilienceService.executeWithRetry(operation);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "retry");
        response.put("operation", operation);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试组合保护机制
     */
    @GetMapping("/combined")
    public ResponseEntity<Map<String, Object>> testCombinedProtection(
            @RequestParam(defaultValue = "组合保护测试") String operation) {
        
        String result = resilienceService.executeWithCombinedProtection(operation);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "combined-protection");
        response.put("operation", operation);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试发送漂流瓶
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> testSendDriftBottle(
            @RequestBody Map<String, String> request) {
        
        String message = request.getOrDefault("message", "默认漂流瓶消息");
        String result = resilienceService.sendDriftBottle(message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "send-drift-bottle");
        response.put("message", message);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试接收漂流瓶
     */
    @GetMapping("/receive")
    public ResponseEntity<Map<String, Object>> testReceiveDriftBottle(
            @RequestParam(defaultValue = "user123") String userId) {
        
        String result = resilienceService.receiveDriftBottle(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "receive-drift-bottle");
        response.put("userId", userId);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试回复漂流瓶
     */
    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> testReplyDriftBottle(
            @RequestBody Map<String, String> request) {
        
        String bottleId = request.getOrDefault("bottleId", "bottle123");
        String reply = request.getOrDefault("reply", "默认回复消息");
        String result = resilienceService.replyDriftBottle(bottleId, reply);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "reply-drift-bottle");
        response.put("bottleId", bottleId);
        response.put("reply", reply);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试查询漂流瓶列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> testListDriftBottles(
            @RequestParam(defaultValue = "user123") String userId) {
        
        String result = resilienceService.listDriftBottles(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("action", "list-drift-bottles");
        response.put("userId", userId);
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取保护机制状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProtectionStatus() {
        String status = resilienceService.getProtectionStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("protectionStatus", status);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量测试接口 - 用于压力测试
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchTest(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "circuit-breaker") String type) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("batchTest", true);
        response.put("count", count);
        response.put("type", type);
        response.put("results", new java.util.ArrayList<>());
        
        for (int i = 0; i < count; i++) {
            String result;
            switch (type) {
                case "rate-limiter":
                    result = resilienceService.executeWithRateLimiter("批量测试-" + i);
                    break;
                case "retry":
                    result = resilienceService.executeWithRetry("批量测试-" + i);
                    break;
                case "combined":
                    result = resilienceService.executeWithCombinedProtection("批量测试-" + i);
                    break;
                default:
                    result = resilienceService.executeWithCircuitBreaker("批量测试-" + i);
                    break;
            }
            
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("index", i);
            testResult.put("result", result);
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> results = 
                (java.util.List<Map<String, Object>>) response.get("results");
            results.add(testResult);
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}