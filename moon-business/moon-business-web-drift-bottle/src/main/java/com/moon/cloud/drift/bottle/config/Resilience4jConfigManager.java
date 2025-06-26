package com.moon.cloud.drift.bottle.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resilience4j 配置管理器
 * 负责动态创建和更新熔断器、限流器、重试等实例
 */
//@Component
public class Resilience4jConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(Resilience4jConfigManager.class);
    
    private final Resilience4jProperties properties;
    
    // 注册表
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RetryRegistry retryRegistry;
    
    // 实例缓存
    private final ConcurrentMap<String, CircuitBreaker> circuitBreakerCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RateLimiter> rateLimiterCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Retry> retryCache = new ConcurrentHashMap<>();

    @Autowired
    public Resilience4jConfigManager(Resilience4jProperties properties) {
        this.properties = properties;
        this.circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        this.rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
        this.retryRegistry = RetryRegistry.ofDefaults();
        
        // 初始化默认实例
        initializeDefaultInstances();
    }

    /**
     * 初始化默认实例
     */
    private void initializeDefaultInstances() {
        logger.info("初始化 Resilience4j 默认实例");
        
        // 创建默认的熔断器实例
        getOrCreateCircuitBreaker("drift-bottle");
        
        // 创建默认的限流器实例
        getOrCreateRateLimiter("drift-bottle");
        
        // 创建默认的重试实例
        getOrCreateRetry("drift-bottle");
    }

    /**
     * 获取或创建熔断器实例
     */
    public CircuitBreaker getOrCreateCircuitBreaker(String name) {
        return circuitBreakerCache.computeIfAbsent(name, this::createCircuitBreaker);
    }

    /**
     * 获取或创建限流器实例
     */
    public RateLimiter getOrCreateRateLimiter(String name) {
        return rateLimiterCache.computeIfAbsent(name, this::createRateLimiter);
    }

    /**
     * 获取或创建重试实例
     */
    public Retry getOrCreateRetry(String name) {
        return retryCache.computeIfAbsent(name, this::createRetry);
    }

    /**
     * 创建熔断器实例
     */
    private CircuitBreaker createCircuitBreaker(String name) {
        logger.info("创建熔断器实例: {}", name);
        
        var config = properties.getCircuitbreaker().getInstances().getDriftBottle();
        
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(config.getSlidingWindowSize())
                .slidingWindowType("TIME_BASED".equals(config.getSlidingWindowType()) 
                    ? CircuitBreakerConfig.SlidingWindowType.TIME_BASED 
                    : CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .minimumNumberOfCalls(config.getMinimumNumberOfCalls())
                .failureRateThreshold(config.getFailureRateThreshold())
                .slowCallDurationThreshold(Duration.ofMillis(config.getSlowCallDurationThreshold()))
                .slowCallRateThreshold(config.getSlowCallRateThreshold())
                .waitDurationInOpenState(Duration.ofSeconds(config.getWaitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(config.getPermittedNumberOfCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(config.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .recordExceptions(getExceptionClasses(config.getRecordExceptions()))
                .ignoreExceptions(getExceptionClasses(config.getIgnoreExceptions()))
                .build();

        return circuitBreakerRegistry.circuitBreaker(name, circuitBreakerConfig);
    }

    /**
     * 创建限流器实例
     */
    private RateLimiter createRateLimiter(String name) {
        logger.info("创建限流器实例: {}", name);
        
        var config = properties.getRatelimiter().getInstances().getDriftBottle();
        
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(config.getLimitRefreshPeriod()))
                .limitForPeriod(config.getLimitForPeriod())
                .timeoutDuration(Duration.ofMillis(config.getTimeoutDuration()))
                .build();

        return rateLimiterRegistry.rateLimiter(name, rateLimiterConfig);
    }

    /**
     * 创建重试实例
     */
    private Retry createRetry(String name) {
        logger.info("创建重试实例: {}", name);
        
        var config = properties.getRetry().getInstances().getDriftBottle();
        
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(config.getMaxAttempts())
                .waitDuration(Duration.ofMillis(config.getWaitDuration()))
                .build();

        return retryRegistry.retry(name, retryConfig);
    }

    /**
     * 动态更新熔断器配置
     */
    public void updateCircuitBreakerConfig(String name, 
                                          int slidingWindowSize,
                                          float failureRateThreshold,
                                          long waitDurationInOpenState) {
        logger.info("动态更新熔断器配置: {}", name);
        
        // 移除旧实例
        circuitBreakerCache.remove(name);
        
        // 更新配置
        var config = properties.getCircuitbreaker().getInstances().getDriftBottle();
        config.setSlidingWindowSize(slidingWindowSize);
        config.setFailureRateThreshold(failureRateThreshold);
        config.setWaitDurationInOpenState(waitDurationInOpenState);
        
        // 创建新实例
        getOrCreateCircuitBreaker(name);
    }

    /**
     * 动态更新限流器配置
     */
    public void updateRateLimiterConfig(String name, 
                                       int limitForPeriod,
                                       int limitRefreshPeriod,
                                       long timeoutDuration) {
        logger.info("动态更新限流器配置: {}", name);
        
        // 移除旧实例
        rateLimiterCache.remove(name);
        
        // 更新配置
        var config = properties.getRatelimiter().getInstances().getDriftBottle();
        config.setLimitForPeriod(limitForPeriod);
        config.setLimitRefreshPeriod(limitRefreshPeriod);
        config.setTimeoutDuration(timeoutDuration);
        
        // 创建新实例
        getOrCreateRateLimiter(name);
    }

    /**
     * 动态更新重试配置
     */
    public void updateRetryConfig(String name, 
                                 int maxAttempts,
                                 long waitDuration) {
        logger.info("动态更新重试配置: {}", name);
        
        // 移除旧实例
        retryCache.remove(name);
        
        // 更新配置
        var config = properties.getRetry().getInstances().getDriftBottle();
        config.setMaxAttempts(maxAttempts);
        config.setWaitDuration(waitDuration);
        
        // 创建新实例
        getOrCreateRetry(name);
    }

    /**
     * 获取熔断器状态
     */
    public String getCircuitBreakerState(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerCache.get(name);
        return circuitBreaker != null ? circuitBreaker.getState().toString() : "NOT_FOUND";
    }

    /**
     * 获取限流器统计信息
     */
    public String getRateLimiterMetrics(String name) {
        RateLimiter rateLimiter = rateLimiterCache.get(name);
        if (rateLimiter != null) {
            var metrics = rateLimiter.getMetrics();
            return String.format("可用许可: %d, 等待线程数: %d", 
                    metrics.getAvailablePermissions(), 
                    metrics.getNumberOfWaitingThreads());
        }
        return "NOT_FOUND";
    }

    /**
     * 重置熔断器状态
     */
    public void resetCircuitBreaker(String name) {
        CircuitBreaker circuitBreaker = circuitBreakerCache.get(name);
        if (circuitBreaker != null) {
            circuitBreaker.reset();
            logger.info("重置熔断器状态: {}", name);
        }
    }

    /**
     * 将异常类名转换为Class数组
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Throwable>[] getExceptionClasses(List<String> exceptionNames) {
        return exceptionNames.stream()
                .map(this::loadExceptionClass)
                .toArray(Class[]::new);
    }

    /**
     * 加载异常类
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> loadExceptionClass(String className) {
        try {
            return (Class<? extends Throwable>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.warn("无法加载异常类: {}", className);
            return Exception.class;
        }
    }
}