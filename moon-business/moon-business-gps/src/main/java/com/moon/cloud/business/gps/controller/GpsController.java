package com.moon.cloud.business.gps.controller;

import com.moon.cloud.business.gps.entity.GpsData;
import com.moon.cloud.business.gps.mapper.GpsDataMapper;
import com.moon.cloud.business.gps.service.GpsSimulatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS控制器
 * 
 * @author mooncloud
 */
@Slf4j
@RestController
@RequestMapping("/api/gps")
public class GpsController {
    
    @Autowired
    private GpsSimulatorService gpsSimulatorService;
    @Autowired
    private GpsDataMapper gpsDataMapper;
    

    /**
     * 手动触发GPS数据生成
     * 
     * @return 响应结果
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateGpsData() {
        try {
            gpsSimulatorService.triggerGpsGeneration();
            return ResponseEntity.ok("GPS data generation triggered successfully");
        } catch (Exception e) {
            log.error("Failed to trigger GPS data generation", e);
            return ResponseEntity.internalServerError().body("Failed to generate GPS data: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定车辆的最新GPS数据
     * 
     * @param vehicleId 车辆ID
     * @return GPS数据
     */
    @GetMapping("/vehicle/{vehicleId}/latest")
    public ResponseEntity<GpsData> getLatestGpsData(@PathVariable String vehicleId) {
        try {
            GpsData gpsData = gpsDataMapper.findLatestByVehicleId(vehicleId);
            if (gpsData != null) {
                return ResponseEntity.ok(gpsData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to get latest GPS data for vehicle: {}", vehicleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取指定车辆在指定时间范围内的GPS数据
     * 
     * @param vehicleId 车辆ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return GPS数据列表
     */
    @GetMapping("/vehicle/{vehicleId}/history")
    public ResponseEntity<List<GpsData>> getGpsHistory(
            @PathVariable String vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            //List<GpsData> gpsDataList = gpsDataMapper.findByVehicleIdAndTimeRange(vehicleId, startTime, endTime);
            //return ResponseEntity.ok(gpsDataList);
            return null;
        } catch (Exception e) {
            log.error("Failed to get GPS history for vehicle: {}", vehicleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GPS Service is running");
    }
}