package com.moon.cloud.business.gps.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moon.cloud.business.gps.dto.GpsMessage;
import com.moon.cloud.business.gps.entity.VehicleInfo;
import com.moon.cloud.business.gps.mapper.VehicleInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * GPS模拟器服务
 * 
 * @author mooncloud
 */
@Slf4j
@Service
public class GpsSimulatorService {
    
    private static final String GPS_TOPIC = "gps-data";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private final ObjectMapper objectMapper;

    @Autowired
    private VehicleInfoMapper vehicleInfoMapper;
    
    public GpsSimulatorService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * 定时生成GPS数据，每5秒执行一次
     */
    @Scheduled(fixedRate = 5000)
    public void generateGpsData() {
        List<VehicleInfo> vehicleInfos = vehicleInfoMapper.selectByStatus(1);
        if (CollectionUtils.isEmpty(vehicleInfos)) {
            return;
        }
        for (VehicleInfo vehicleInfo : vehicleInfos) {
            GpsMessage gpsMessage = generateRandomGpsMessage(vehicleInfo.getVehicleId());
            sendGpsMessage(gpsMessage);
        }
        log.info("Generated GPS data for {} vehicles", vehicleInfos.size());
    }
    
    /**
     * 生成随机GPS消息
     * 
     * @param vehicleId 车辆ID
     * @return GPS消息
     */
    private GpsMessage generateRandomGpsMessage(String vehicleId) {
        // 模拟在北京市范围内的GPS坐标
        double baseLongitude = 116.4074; // 北京经度
        double baseLatitude = 39.9042;   // 北京纬度
        
        // 在基础坐标附近随机偏移
        double longitude = baseLongitude + (ThreadLocalRandom.current().nextDouble(-0.5, 0.5));
        double latitude = baseLatitude + (ThreadLocalRandom.current().nextDouble(-0.3, 0.3));
        
        return new GpsMessage()
                .setVehicleId(vehicleId)
                .setLongitude(longitude)
                .setLatitude(latitude)
                .setSpeed(ThreadLocalRandom.current().nextDouble(0, 120)) // 0-120 km/h
                .setDirection(ThreadLocalRandom.current().nextDouble(0, 360)) // 0-360度
                .setAltitude(ThreadLocalRandom.current().nextDouble(0, 100)) // 0-100米
                .setGpsTime(LocalDateTime.now());
    }
    
    /**
     * 发送GPS消息到Kafka
     * 
     * @param gpsMessage GPS消息
     */
    private void sendGpsMessage(GpsMessage gpsMessage) {
        try {
            String messageJson = objectMapper.writeValueAsString(gpsMessage);
            kafkaTemplate.send(GPS_TOPIC, gpsMessage.getVehicleId(), messageJson);
            log.debug("Sent GPS message for vehicle: {}", gpsMessage.getVehicleId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize GPS message for vehicle: {}", gpsMessage.getVehicleId(), e);
        }
    }
    
    /**
     * 手动触发GPS数据生成
     */
    public void triggerGpsGeneration() {
        generateGpsData();
    }
}