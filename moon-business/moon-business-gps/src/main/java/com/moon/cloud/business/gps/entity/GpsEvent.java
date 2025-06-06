package com.moon.cloud.business.gps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * GPS事件实体类
 * 
 * @author mooncloud
 */
@Data
@Accessors(chain = true)
@TableName("gps_event")
public class GpsEvent {
    
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
     * 事件类型(ROUTE_DEVIATION:路线偏离, AREA_ENTER:驶入区域, AREA_EXIT:驶出区域)
     */
    private String eventType;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}