package com.moon.cloud.threadpool.rejector;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 重试拒绝策略配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "moon.threadpool.retry")
public class RetryRejectedExecutionConfig {
    
    /**
     * 最大重试次数，默认5次
     */
    private int maxAttempts = 5;
    
    /**
     * 重试间隔时间（毫秒），默认100ms
     */
    private long retryInterval = 100L;
    
    /**
     * 重试间隔递增因子，默认1.5
     */
    private double backoffMultiplier = 1.5;
    
    /**
     * 最大重试间隔时间（毫秒），默认1秒
     */
    private long maxRetryInterval = 1000L;
    
    /**
     * 是否启用重试策略，默认true
     */
    private boolean enabled = true;
}