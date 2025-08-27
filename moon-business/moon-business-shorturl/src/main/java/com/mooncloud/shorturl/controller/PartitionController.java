package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.service.PartitionManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分区管理API控制器
 * 
 * @author mooncloud
 */
@RestController
@RequestMapping("/api/admin/partition")
@Slf4j
@ConditionalOnProperty(name = "app.partition.enabled", havingValue = "true", matchIfMissing = true)
public class PartitionController {
    
    @Autowired
    private PartitionManagerService partitionManagerService;
    
    /**
     * 获取URL映射表分区信息
     */
    @GetMapping("/url-mapping")
    public ResponseEntity<Map<String, Object>> getUrlMappingPartitions() {
        try {
            List<String> partitions = partitionManagerService.getPartitionInfo("url_mapping");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", partitions);
            result.put("message", "获取URL映射表分区信息成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取URL映射表分区信息失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取分区信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取访问日志表分区信息
     */
    @GetMapping("/access-log")
    public ResponseEntity<Map<String, Object>> getAccessLogPartitions() {
        try {
            List<String> partitions = partitionManagerService.getPartitionInfo("url_access_log");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", partitions);
            result.put("message", "获取访问日志表分区信息成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取访问日志表分区信息失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取分区信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 手动创建URL映射表分区
     */
    @PostMapping("/url-mapping/create")
    public ResponseEntity<Map<String, Object>> createUrlMappingPartition(@RequestParam String date) {
        try {
            LocalDate partitionDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            partitionManagerService.createUrlMappingPartition(partitionDate);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "URL映射表分区创建成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("创建URL映射表分区失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "创建分区失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 手动创建访问日志表分区
     */
    @PostMapping("/access-log/create")
    public ResponseEntity<Map<String, Object>> createAccessLogPartition(@RequestParam String date) {
        try {
            LocalDate partitionDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            partitionManagerService.createAccessLogPartition(partitionDate);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "访问日志表分区创建成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("创建访问日志表分区失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "创建分区失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 手动执行分区维护
     */
    @PostMapping("/maintenance")
    public ResponseEntity<Map<String, Object>> executePartitionMaintenance() {
        try {
            partitionManagerService.maintainPartitions();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "分区维护执行成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("执行分区维护失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "分区维护失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 初始化分区表
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializePartitions() {
        try {
            partitionManagerService.initializePartitions();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "分区初始化成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("分区初始化失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "分区初始化失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取分区统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPartitionStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取URL映射表分区信息
            List<String> urlMappingPartitions = partitionManagerService.getPartitionInfo("url_mapping");
            statistics.put("urlMappingPartitionCount", urlMappingPartitions.size());
            
            // 获取访问日志表分区信息
            List<String> accessLogPartitions = partitionManagerService.getPartitionInfo("url_access_log");
            statistics.put("accessLogPartitionCount", accessLogPartitions.size());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", statistics);
            result.put("message", "获取分区统计信息成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取分区统计信息失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取分区统计信息失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
}