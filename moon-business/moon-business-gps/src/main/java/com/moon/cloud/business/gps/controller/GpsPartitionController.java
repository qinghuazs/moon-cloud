package com.moon.cloud.business.gps.controller;

import com.moon.cloud.business.gps.service.GpsPartitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GPS数据分表管理控制器
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/gps/partition")
@Tag(name = "GPS分表管理", description = "GPS数据分表管理相关接口")
public class GpsPartitionController {

    @Autowired
    private GpsPartitionService gpsPartitionService;

    /**
     * 创建指定日期的分表
     */
    @PostMapping("/create")
    @Operation(summary = "创建指定日期的分表", description = "为指定日期创建GPS数据分表")
    public ResponseEntity<Map<String, Object>> createPartition(
            @Parameter(description = "日期，格式：yyyy-MM-dd", example = "2024-01-15")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = gpsPartitionService.createPartitionTable(date);
            
            if (success) {
                result.put("success", true);
                result.put("message", "分表创建成功");
                result.put("date", date);
                result.put("tableName", "gps_data_" + date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")));
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "分表创建失败");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("创建分表接口异常", e);
            result.put("success", false);
            result.put("message", "创建分表时发生异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 批量创建未来几天的分表
     */
    @PostMapping("/create-future")
    @Operation(summary = "批量创建未来分表", description = "批量创建未来指定天数的GPS数据分表")
    public ResponseEntity<Map<String, Object>> createFuturePartitions(
            @Parameter(description = "提前创建的天数", example = "7")
            @RequestParam(defaultValue = "7") int daysAhead) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            gpsPartitionService.createFuturePartitions(daysAhead);
            
            result.put("success", true);
            result.put("message", "批量创建分表成功");
            result.put("daysAhead", daysAhead);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("批量创建分表接口异常", e);
            result.put("success", false);
            result.put("message", "批量创建分表时发生异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 清理旧的分表
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清理旧分表", description = "清理指定天数之前的GPS数据分表")
    public ResponseEntity<Map<String, Object>> cleanOldPartitions(
            @Parameter(description = "保留的天数", example = "30")
            @RequestParam(defaultValue = "30") int daysToKeep) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            gpsPartitionService.cleanOldPartitions(daysToKeep);
            
            result.put("success", true);
            result.put("message", "清理旧分表成功");
            result.put("daysToKeep", daysToKeep);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("清理旧分表接口异常", e);
            result.put("success", false);
            result.put("message", "清理旧分表时发生异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取分表信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取分表信息", description = "获取所有GPS数据分表的信息列表")
    public ResponseEntity<Map<String, Object>> getPartitionList() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> partitionInfo = gpsPartitionService.getPartitionInfo();
            
            result.put("success", true);
            result.put("data", partitionInfo);
            result.put("total", partitionInfo.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取分表信息接口异常", e);
            result.put("success", false);
            result.put("message", "获取分表信息时发生异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取分表统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取分表统计", description = "获取指定日期范围内的GPS数据分表统计信息")
    public ResponseEntity<Map<String, Object>> getPartitionStats(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd", example = "2024-01-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd", example = "2024-01-31")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> stats = gpsPartitionService.getPartitionStats(startDate, endDate);
            
            result.put("success", true);
            result.put("data", stats);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取分表统计接口异常", e);
            result.put("success", false);
            result.put("message", "获取分表统计时发生异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 创建今天的分表
     */
    @PostMapping("/create-today")
    @Operation(summary = "创建今天的分表", description = "为当前日期创建GPS数据分表")
    public ResponseEntity<Map<String, Object>> createTodayPartition() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = gpsPartitionService.createTodayPartition();
            
            if (success) {
                result.put("success", true);
                result.put("message", "今天的分表创建成功");
                result.put("date", LocalDate.now());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "今天的分表创建失败");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("创建今天分表接口异常", e);
            result.put("success", false);
            result.put("message", "创建今天分表时发生异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 创建明天的分表
     */
    @PostMapping("/create-tomorrow")
    @Operation(summary = "创建明天的分表", description = "为明天日期创建GPS数据分表")
    public ResponseEntity<Map<String, Object>> createTomorrowPartition() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = gpsPartitionService.createTomorrowPartition();
            
            if (success) {
                result.put("success", true);
                result.put("message", "明天的分表创建成功");
                result.put("date", LocalDate.now().plusDays(1));
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "明天的分表创建失败");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("创建明天分表接口异常", e);
            result.put("success", false);
            result.put("message", "创建明天分表时发生异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}