package com.moon.cloud.business.gps.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moon.cloud.business.gps.dto.GpsMessage;
import com.moon.cloud.business.gps.service.GpsProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * GPS数据Kafka消费者
 * 
 * @author mooncloud
 */
@Slf4j
@Component
public class GpsDataConsumer {
    
    @Autowired
    private GpsProcessingService gpsProcessingService;
    
    private final ObjectMapper objectMapper;
    private final Executor threadPool;
    
    public GpsDataConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // 创建线程池，用于异步处理GPS数据
        this.threadPool = Executors.newFixedThreadPool(20);
    }
    
    /**
     * 消费GPS数据
     * 
     * @param message GPS消息JSON字符串
     */
    @KafkaListener(topics = "gps-data", groupId = "gps-consumer-group")
    public void consumeGpsData(String message) {
        try {
            // 解析GPS消息
            GpsMessage gpsMessage = objectMapper.readValue(message, GpsMessage.class);
            
            // 使用线程池异步处理GPS数据
            CompletableFuture.runAsync(() -> {
                try {
                    gpsProcessingService.processGpsData(gpsMessage);
                } catch (Exception e) {
                    log.error("Error processing GPS data for vehicle: {}", gpsMessage.getVehicleId(), e);
                }
            }, threadPool);
            
            log.debug("Received and queued GPS data for vehicle: {}", gpsMessage.getVehicleId());
            
        } catch (Exception e) {
            log.error("Failed to parse GPS message: {}", message, e);
        }
    }
}