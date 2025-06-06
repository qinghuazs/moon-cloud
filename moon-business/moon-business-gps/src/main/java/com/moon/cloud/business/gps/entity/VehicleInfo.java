package com.moon.cloud.business.gps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 车辆信息实体类
 * 
 * @author mooncloud
 */
@Data
@Accessors(chain = true)
@TableName("vehicle_info")
public class VehicleInfo {
    
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
     * 车辆名称
     */
    private String vehicleName;
    
    /**
     * 车辆类型
     */
    private String vehicleType;
    
    /**
     * 司机姓名
     */
    private String driverName;
    
    /**
     * 司机电话
     */
    private String driverPhone;
    
    /**
     * 状态(0:停用, 1:启用)
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}