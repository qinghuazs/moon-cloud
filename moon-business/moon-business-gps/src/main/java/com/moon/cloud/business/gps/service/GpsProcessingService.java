package com.moon.cloud.business.gps.service;

import com.moon.cloud.business.gps.dto.GpsMessage;
import com.moon.cloud.business.gps.entity.GpsData;
import com.moon.cloud.business.gps.mapper.GpsDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS数据处理服务
 * 
 * @author mooncloud
 */
@Slf4j
@Service
public class GpsProcessingService {
    

    @Autowired
    private GpsDataMapper gpsDataMapper;

    // 预定义的路线坐标点（示例：北京市内的一条路线）
    private static final double[][] ROUTE_POINTS = {
            {116.4074, 39.9042}, // 天安门
            {116.4109, 39.9097}, // 故宫
            {116.4133, 39.9239}, // 景山公园
            {116.4199, 39.9288}, // 北海公园
            {116.4236, 39.9347}  // 什刹海
    };
    
    // 允许的偏离距离（米）
    private static final double MAX_DEVIATION_DISTANCE = 500.0;
    
    // 预定义的区域边界（示例：北京市中心区域）
    private static final double AREA_MIN_LONGITUDE = 116.3;
    private static final double AREA_MAX_LONGITUDE = 116.5;
    private static final double AREA_MIN_LATITUDE = 39.8;
    private static final double AREA_MAX_LATITUDE = 40.0;
    
    /**
     * 处理GPS数据
     * 
     * @param gpsMessage GPS消息
     */
    public void processGpsData(GpsMessage gpsMessage) {
        try {
            // 1. 保存GPS数据到数据库
            saveGpsData(gpsMessage);
            
            // 2. 路线偏离判断
            boolean isDeviated = checkRouteDeviation(gpsMessage);
            if (isDeviated) {
                handleRouteDeviation(gpsMessage);
            }
            
            // 3. 驶入驶出判断
            checkAreaEnterExit(gpsMessage);
            
            log.debug("Processed GPS data for vehicle: {}", gpsMessage.getVehicleId());
            
        } catch (Exception e) {
            log.error("Failed to process GPS data for vehicle: {}", gpsMessage.getVehicleId(), e);
        }
    }
    
    /**
     * 保存GPS数据到数据库
     * 
     * @param gpsMessage GPS消息
     */
    private void saveGpsData(GpsMessage gpsMessage) {
        GpsData gpsData = new GpsData()
                .setVehicleId(gpsMessage.getVehicleId())
                .setLongitude(gpsMessage.getLongitude())
                .setLatitude(gpsMessage.getLatitude())
                .setSpeed(gpsMessage.getSpeed())
                .setDirection(gpsMessage.getDirection())
                .setAltitude(gpsMessage.getAltitude())
                .setGpsTime(gpsMessage.getGpsTime())
                .setCreateTime(LocalDateTime.now());
        
        gpsDataMapper.insert(gpsData);
    }
    
    /**
     * 检查路线偏离
     * 
     * @param gpsMessage GPS消息
     * @return 是否偏离路线
     */
    private boolean checkRouteDeviation(GpsMessage gpsMessage) {
        double currentLon = gpsMessage.getLongitude();
        double currentLat = gpsMessage.getLatitude();
        
        // 计算当前位置到路线的最短距离
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < ROUTE_POINTS.length - 1; i++) {
            double distance = calculateDistanceToLineSegment(
                    currentLon, currentLat,
                    ROUTE_POINTS[i][0], ROUTE_POINTS[i][1],
                    ROUTE_POINTS[i + 1][0], ROUTE_POINTS[i + 1][1]
            );
            minDistance = Math.min(minDistance, distance);
        }
        
        return minDistance > MAX_DEVIATION_DISTANCE;
    }
    
    /**
     * 处理路线偏离事件
     * 
     * @param gpsMessage GPS消息
     */
    private void handleRouteDeviation(GpsMessage gpsMessage) {
        log.warn("Vehicle {} deviated from route at location: [{}, {}]", 
                gpsMessage.getVehicleId(), 
                gpsMessage.getLongitude(), 
                gpsMessage.getLatitude());
        
        // 这里可以添加更多的处理逻辑，比如发送告警、记录事件等
    }
    
    /**
     * 检查区域驶入驶出
     * 
     * @param gpsMessage GPS消息
     */
    private void checkAreaEnterExit(GpsMessage gpsMessage) {
        boolean currentInArea = isInArea(gpsMessage.getLongitude(), gpsMessage.getLatitude());
        
        // 获取车辆的上一个GPS数据
        GpsData lastGpsData = gpsDataMapper.findLatestByVehicleId(gpsMessage.getVehicleId());
        if (lastGpsData != null) {
            boolean lastInArea = isInArea(lastGpsData.getLongitude(), lastGpsData.getLatitude());
            
            if (!lastInArea && currentInArea) {
                // 驶入区域
                handleAreaEnter(gpsMessage);
            } else if (lastInArea && !currentInArea) {
                // 驶出区域
                handleAreaExit(gpsMessage);
            }
        }
    }
    
    /**
     * 判断坐标是否在指定区域内
     * 
     * @param longitude 经度
     * @param latitude 纬度
     * @return 是否在区域内
     */
    private boolean isInArea(double longitude, double latitude) {
        return longitude >= AREA_MIN_LONGITUDE && longitude <= AREA_MAX_LONGITUDE
                && latitude >= AREA_MIN_LATITUDE && latitude <= AREA_MAX_LATITUDE;
    }
    
    /**
     * 处理驶入区域事件
     * 
     * @param gpsMessage GPS消息
     */
    private void handleAreaEnter(GpsMessage gpsMessage) {
        log.info("Vehicle {} entered the area at location: [{}, {}]", 
                gpsMessage.getVehicleId(), 
                gpsMessage.getLongitude(), 
                gpsMessage.getLatitude());
    }
    
    /**
     * 处理驶出区域事件
     * 
     * @param gpsMessage GPS消息
     */
    private void handleAreaExit(GpsMessage gpsMessage) {
        log.info("Vehicle {} exited the area at location: [{}, {}]", 
                gpsMessage.getVehicleId(), 
                gpsMessage.getLongitude(), 
                gpsMessage.getLatitude());
    }
    
    /**
     * 计算点到线段的距离（使用Haversine公式的简化版本）
     * 
     * @param px 点的经度
     * @param py 点的纬度
     * @param x1 线段起点经度
     * @param y1 线段起点纬度
     * @param x2 线段终点经度
     * @param y2 线段终点纬度
     * @return 距离（米）
     */
    private double calculateDistanceToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        // 简化的距离计算，实际应用中可以使用更精确的地理距离计算
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;
        
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        
        if (lenSq == 0) {
            // 线段退化为点
            return calculateDistance(px, py, x1, y1);
        }
        
        double param = dot / lenSq;
        
        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }
        
        return calculateDistance(px, py, xx, yy);
    }
    
    /**
     * 计算两点之间的距离（简化版本）
     * 
     * @param lon1 点1经度
     * @param lat1 点1纬度
     * @param lon2 点2经度
     * @param lat2 点2纬度
     * @return 距离（米）
     */
    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        // 简化的距离计算，1度约等于111000米
        double deltaLon = lon2 - lon1;
        double deltaLat = lat2 - lat1;
        return Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat) * 111000;
    }
}