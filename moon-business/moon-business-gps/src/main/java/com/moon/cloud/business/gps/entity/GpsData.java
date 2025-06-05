package com.moon.cloud.business.gps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * GPS数据实体类
 * 
 * @author mooncloud
 */
@Data
@Accessors(chain = true)
@TableName("gps_data")
public class GpsData {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
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
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}