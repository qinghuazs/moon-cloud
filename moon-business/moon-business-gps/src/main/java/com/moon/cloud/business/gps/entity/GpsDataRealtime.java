package com.moon.cloud.business.gps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GPS实时数据实体类
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("gps_data_realtime")
public class GpsDataRealtime implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 车辆ID
     */
    @TableField("vehicle_id")
    private String vehicleId;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 速度(km/h)
     */
    @TableField("speed")
    private BigDecimal speed;

    /**
     * 方向角(0-360度)
     */
    @TableField("direction")
    private BigDecimal direction;

    /**
     * 海拔高度(米)
     */
    @TableField("altitude")
    private BigDecimal altitude;

    /**
     * GPS时间
     */
    @TableField("gps_time")
    private LocalDateTime gpsTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

}