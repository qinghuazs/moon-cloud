package com.moon.cloud.eureka.client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供服务健康状态和基本信息接口
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    /**
     * 健康检查接口
     *
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("application", applicationName);
        result.put("port", serverPort);
        result.put("role", "EUREKA_CLIENT_SERVER");
        return result;
    }

    /**
     * 服务信息接口
     *
     * @return 服务基本信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("application", applicationName);
        result.put("port", serverPort);
        result.put("description", "Moon Cloud Eureka Client & Server");
        result.put("version", "1.0.0");
        result.put("features", new String[]{"eureka-client", "eureka-server", "health-check"});
        result.put("timestamp", LocalDateTime.now());
        return result;
    }

    /**
     * 服务发现接口
     *
     * @return 服务发现状态
     */
    @GetMapping("/discovery")
    public Map<String, Object> discovery() {
        Map<String, Object> result = new HashMap<>();
        result.put("clientEnabled", true);
        result.put("serverEnabled", true);
        result.put("registerWithEureka", true);
        result.put("fetchRegistry", true);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
}