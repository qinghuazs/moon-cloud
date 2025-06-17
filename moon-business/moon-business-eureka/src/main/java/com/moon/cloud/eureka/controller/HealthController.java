package com.moon.cloud.eureka.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查接口
     *
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "eureka-server");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "Eureka Server is running normally");
        return result;
    }

    /**
     * 服务信息接口
     *
     * @return 服务信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", "Moon Cloud Eureka Server");
        result.put("version", "1.0.0");
        result.put("description", "Spring Cloud Netflix Eureka Server");
        result.put("port", 8761);
        return result;
    }
}