package com.moon.cloud.drift.bottle.service;

import com.moon.cloud.drift.bottle.config.Resilience4jConfigManager;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * 漂流瓶弹性服务
 * 演示如何在业务代码中使用动态配置的Resilience4j组件
 */
@Service
public class DriftBottleResilienceService {

    private static final Logger logger = LoggerFactory.getLogger(DriftBottleResilienceService.class);
    
    private final Resilience4jConfigManager configManager;

    @Autowired
    public DriftBottleResilienceService(Resilience4jConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 使用熔断器保护的方法示例
     */
    public String executeWithCircuitBreaker(String operation) {
        CircuitBreaker circuitBreaker = configManager.getOrCreateCircuitBreaker("drift-bottle");
        
        Supplier<String> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, () -> {
                    logger.info("执行操作: {}", operation);
                    
                    // 模拟可能失败的操作
                    if (Math.random() > 0.7) {
                        throw new RuntimeException("模拟业务异常");
                    }
                    
                    return "操作成功: " + operation;
                });
        
        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            logger.error("操作失败: {}", e.getMessage());
            return "操作失败，熔断器保护: " + e.getMessage();
        }
    }

    /**
     * 使用限流器保护的方法示例
     */
    public String executeWithRateLimiter(String operation) {
        RateLimiter rateLimiter = configManager.getOrCreateRateLimiter("drift-bottle");
        
        Supplier<String> decoratedSupplier = RateLimiter
                .decorateSupplier(rateLimiter, () -> {
                    logger.info("执行限流操作: {}", operation);
                    
                    // 模拟耗时操作
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    return "限流操作成功: " + operation;
                });
        
        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            logger.error("限流操作失败: {}", e.getMessage());
            return "操作被限流: " + e.getMessage();
        }
    }

    /**
     * 使用重试机制保护的方法示例
     */
    public String executeWithRetry(String operation) {
        Retry retry = configManager.getOrCreateRetry("drift-bottle");
        
        Supplier<String> decoratedSupplier = Retry
                .decorateSupplier(retry, () -> {
                    logger.info("执行重试操作: {}", operation);
                    
                    // 模拟可能失败的操作
                    if (Math.random() > 0.6) {
                        throw new RuntimeException("模拟临时故障");
                    }
                    
                    return "重试操作成功: " + operation;
                });
        
        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            logger.error("重试操作最终失败: {}", e.getMessage());
            return "重试操作最终失败: " + e.getMessage();
        }
    }

    /**
     * 组合使用多种保护机制的方法示例
     */
    public String executeWithCombinedProtection(String operation) {
        CircuitBreaker circuitBreaker = configManager.getOrCreateCircuitBreaker("drift-bottle");
        RateLimiter rateLimiter = configManager.getOrCreateRateLimiter("drift-bottle");
        Retry retry = configManager.getOrCreateRetry("drift-bottle");
        
        // 组合装饰器：重试 -> 熔断器 -> 限流器
        Supplier<String> decoratedSupplier = () -> {
            logger.info("执行组合保护操作: {}", operation);
            
            // 模拟复杂的业务逻辑
            if (Math.random() > 0.8) {
                throw new RuntimeException("模拟业务异常");
            }
            
            // 模拟耗时操作
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return "组合保护操作成功: " + operation;
        };
        
        // 应用装饰器链
        decoratedSupplier = RateLimiter.decorateSupplier(rateLimiter, decoratedSupplier);
        decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, decoratedSupplier);
        decoratedSupplier = Retry.decorateSupplier(retry, decoratedSupplier);
        
        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            logger.error("组合保护操作失败: {}", e.getMessage());
            return "组合保护操作失败: " + e.getMessage();
        }
    }

    /**
     * 模拟发送漂流瓶的业务方法
     */
    public String sendDriftBottle(String message) {
        return executeWithCombinedProtection("发送漂流瓶: " + message);
    }

    /**
     * 模拟接收漂流瓶的业务方法
     */
    public String receiveDriftBottle(String userId) {
        return executeWithCircuitBreaker("接收漂流瓶: 用户" + userId);
    }

    /**
     * 模拟回复漂流瓶的业务方法
     */
    public String replyDriftBottle(String bottleId, String reply) {
        return executeWithRetry("回复漂流瓶: " + bottleId + ", 回复: " + reply);
    }

    /**
     * 模拟查询漂流瓶列表的业务方法
     */
    public String listDriftBottles(String userId) {
        return executeWithRateLimiter("查询漂流瓶列表: 用户" + userId);
    }

    /**
     * 获取当前保护机制的状态信息
     */
    public String getProtectionStatus() {
        StringBuilder status = new StringBuilder();
        
        status.append("熔断器状态: ").append(configManager.getCircuitBreakerState("drift-bottle")).append("\n");
        status.append("限流器指标: ").append(configManager.getRateLimiterMetrics("drift-bottle")).append("\n");
        status.append("时间戳: ").append(System.currentTimeMillis());
        
        return status.toString();
    }
}