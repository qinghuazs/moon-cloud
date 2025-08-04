package com.mooncloud.shorturl.config;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 * 
 * @author mooncloud
 */
@Configuration
public class BloomFilterConfig {
    
    /**
     * URL布隆过滤器
     * 预期插入100万个URL，误判率0.01%
     * 
     * @return 布隆过滤器实例
     */
    @Bean
    public BloomFilter<String> urlBloomFilter() {
        return BloomFilter.create(
            Funnels.stringFunnel(Charsets.UTF_8),
            1_000_000,
            0.0001
        );
    }
}