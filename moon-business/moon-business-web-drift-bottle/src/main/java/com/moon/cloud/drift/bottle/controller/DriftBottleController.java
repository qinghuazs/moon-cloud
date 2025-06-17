package com.moon.cloud.drift.bottle.controller;

import com.moon.cloud.drift.bottle.dto.BottleReplyDTO;
import com.moon.cloud.drift.bottle.dto.DriftBottleDTO;
import com.moon.cloud.drift.bottle.service.DriftBottleService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 漂流瓶控制器
 * 支持resilience4j的熔断和限流
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/drift-bottle")
@CrossOrigin(origins = "*")
public class DriftBottleController {

    private static final Logger logger = LoggerFactory.getLogger(DriftBottleController.class);

    @Autowired
    private DriftBottleService driftBottleService;

    /**
     * 创建并投放漂流瓶
     *
     * @param bottleDTO 漂流瓶DTO
     * @return 响应结果
     */
    @PostMapping("/throw")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "throwBottleFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> throwBottle(@Valid @RequestBody DriftBottleDTO bottleDTO) {
        try {
            logger.info("接收到投放漂流瓶请求: {}", bottleDTO);
            
            DriftBottleDTO createdBottle = driftBottleService.createAndThrowBottle(bottleDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "漂流瓶投放成功");
            response.put("data", createdBottle);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("投放漂流瓶失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "投放漂流瓶失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 捡漂流瓶
     *
     * @param username 用户名
     * @return 响应结果
     */
    @PostMapping("/pickup")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "pickupBottleFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> pickupBottle(@RequestParam String username) {
        try {
            logger.info("用户 {} 尝试捡漂流瓶", username);
            
            DriftBottleDTO bottle = driftBottleService.pickUpBottle(username);
            
            Map<String, Object> response = new HashMap<>();
            if (bottle != null) {
                response.put("success", true);
                response.put("message", "成功捡到漂流瓶");
                response.put("data", bottle);
            } else {
                response.put("success", false);
                response.put("message", "暂时没有可捡的漂流瓶，请稍后再试");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("捡漂流瓶失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "捡漂流瓶失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 丢弃漂流瓶
     *
     * @param bottleId 漂流瓶ID
     * @param username 用户名
     * @return 响应结果
     */
    @PostMapping("/discard/{bottleId}")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "discardBottleFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> discardBottle(
            @PathVariable Long bottleId, 
            @RequestParam String username) {
        try {
            logger.info("用户 {} 尝试丢弃漂流瓶 {}", username, bottleId);
            
            boolean success = driftBottleService.discardBottle(bottleId, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "漂流瓶丢弃成功" : "漂流瓶丢弃失败");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("丢弃漂流瓶失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "丢弃漂流瓶失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 回复漂流瓶
     *
     * @param replyDTO 回复DTO
     * @return 响应结果
     */
    @PostMapping("/reply")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "replyBottleFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> replyBottle(@Valid @RequestBody BottleReplyDTO replyDTO) {
        try {
            logger.info("接收到回复漂流瓶请求: {}", replyDTO);
            
            BottleReplyDTO createdReply = driftBottleService.replyToBottle(replyDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "回复成功，漂流瓶已回到发送者手中");
            response.put("data", createdReply);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("回复漂流瓶失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "回复失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户发送的漂流瓶列表
     *
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 响应结果
     */
    @GetMapping("/sent")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "getSentBottlesFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> getSentBottles(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<DriftBottleDTO> bottles = driftBottleService.getSentBottles(username, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取发送列表成功");
            response.put("data", bottles);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取发送列表失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取发送列表失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户接收的漂流瓶列表
     *
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 响应结果
     */
    @GetMapping("/received")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "getReceivedBottlesFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> getReceivedBottles(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<DriftBottleDTO> bottles = driftBottleService.getReceivedBottles(username, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取接收列表成功");
            response.put("data", bottles);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取接收列表失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取接收列表失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取漂流瓶详情
     *
     * @param bottleId 漂流瓶ID
     * @param username 用户名
     * @return 响应结果
     */
    @GetMapping("/detail/{bottleId}")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "getBottleDetailFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> getBottleDetail(
            @PathVariable Long bottleId,
            @RequestParam String username) {
        try {
            DriftBottleDTO bottle = driftBottleService.getBottleDetail(bottleId, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取详情成功");
            response.put("data", bottle);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取漂流瓶详情失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取详情失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取漂流瓶回复列表
     *
     * @param bottleId 漂流瓶ID
     * @param page 页码
     * @param size 每页大小
     * @return 响应结果
     */
    @GetMapping("/replies/{bottleId}")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "getBottleRepliesFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> getBottleReplies(
            @PathVariable Long bottleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BottleReplyDTO> replies = driftBottleService.getBottleReplies(bottleId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取回复列表成功");
            response.put("data", replies);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取回复列表失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取回复列表失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户统计信息
     *
     * @param username 用户名
     * @return 响应结果
     */
    @GetMapping("/statistics")
    @CircuitBreaker(name = "drift-bottle", fallbackMethod = "getUserStatisticsFallback")
    @RateLimiter(name = "drift-bottle", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@RequestParam String username) {
        try {
            Map<String, Object> statistics = driftBottleService.getUserStatistics(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取统计信息成功");
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取统计信息失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== 熔断降级方法 ====================

    /**
     * 投放漂流瓶熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> throwBottleFallback(DriftBottleDTO bottleDTO, Exception ex) {
        logger.warn("投放漂流瓶服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 捡漂流瓶熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> pickupBottleFallback(String username, Exception ex) {
        logger.warn("捡漂流瓶服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 丢弃漂流瓶熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> discardBottleFallback(Long bottleId, String username, Exception ex) {
        logger.warn("丢弃漂流瓶服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 回复漂流瓶熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> replyBottleFallback(BottleReplyDTO replyDTO, Exception ex) {
        logger.warn("回复漂流瓶服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 获取发送列表熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> getSentBottlesFallback(String username, int page, int size, Exception ex) {
        logger.warn("获取发送列表服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 获取接收列表熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> getReceivedBottlesFallback(String username, int page, int size, Exception ex) {
        logger.warn("获取接收列表服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 获取漂流瓶详情熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> getBottleDetailFallback(Long bottleId, String username, Exception ex) {
        logger.warn("获取漂流瓶详情服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 获取回复列表熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> getBottleRepliesFallback(Long bottleId, int page, int size, Exception ex) {
        logger.warn("获取回复列表服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 获取统计信息熔断降级方法
     */
    public ResponseEntity<Map<String, Object>> getUserStatisticsFallback(String username, Exception ex) {
        logger.warn("获取统计信息服务熔断，执行降级逻辑: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 限流降级方法
     */
    public ResponseEntity<Map<String, Object>> rateLimitFallback(Exception ex) {
        logger.warn("请求频率过高，触发限流: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "请求频率过高，请稍后重试");
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}