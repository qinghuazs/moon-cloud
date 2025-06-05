package com.moon.cloud.business.gps.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * GPS消息传输对象
 * 
 * @author mooncloud
 */
@Data
@Accessors(chain = true)
public class GpsMessage {
    
    /**
     * 车辆ID
     */
    private String vehicleId;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 速度 (km/h)
     */
    private Double speed;
    
    /**
     * 方向角 (0-360度)
     */
    private Double direction;
    
    /**
     * 海拔高度 (米)
     */
    private Double altitude;
    
    /**
     * GPS时间
     */
    private LocalDateTime gpsTime;
}