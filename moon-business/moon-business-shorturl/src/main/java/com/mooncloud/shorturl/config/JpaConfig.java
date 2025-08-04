package com.mooncloud.shorturl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA配置
 * 
 * @author mooncloud
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.mooncloud.shorturl.repository")
public class JpaConfig {
}