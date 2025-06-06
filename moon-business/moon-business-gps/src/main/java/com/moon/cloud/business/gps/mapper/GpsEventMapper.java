package com.moon.cloud.business.gps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.business.gps.entity.GpsEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS事件Mapper接口
 * 
 * @author mooncloud
 */
public interface GpsEventMapper extends BaseMapper<GpsEvent> {
    
    /**
     * 根据车辆ID查询事件列表
     * 
     * @param vehicleId 车辆ID
     * @return 事件列表
     */
    @Select("SELECT * FROM gps_event WHERE vehicle_id = #{vehicleId} ORDER BY event_time DESC")
    List<GpsEvent> selectByVehicleId(@Param("vehicleId") String vehicleId);
    
    /**
     * 根据事件类型查询事件列表
     * 
     * @param eventType 事件类型
     * @return 事件列表
     */
    @Select("SELECT * FROM gps_event WHERE event_type = #{eventType} ORDER BY event_time DESC")
    List<GpsEvent> selectByEventType(@Param("eventType") String eventType);
    
    /**
     * 根据车辆ID和事件类型查询事件列表
     * 
     * @param vehicleId 车辆ID
     * @param eventType 事件类型
     * @return 事件列表
     */
    @Select("SELECT * FROM gps_event WHERE vehicle_id = #{vehicleId} AND event_type = #{eventType} ORDER BY event_time DESC")
    List<GpsEvent> selectByVehicleIdAndEventType(@Param("vehicleId") String vehicleId, @Param("eventType") String eventType);
    
    /**
     * 根据时间范围查询事件列表
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    @Select("SELECT * FROM gps_event WHERE event_time BETWEEN #{startTime} AND #{endTime} ORDER BY event_time DESC")
    List<GpsEvent> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据车辆ID和时间范围查询事件列表
     * 
     * @param vehicleId 车辆ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    @Select("SELECT * FROM gps_event WHERE vehicle_id = #{vehicleId} AND event_time BETWEEN #{startTime} AND #{endTime} ORDER BY event_time DESC")
    List<GpsEvent> selectByVehicleIdAndTimeRange(@Param("vehicleId") String vehicleId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定车辆的事件数量
     * 
     * @param vehicleId 车辆ID
     * @return 事件数量
     */
    @Select("SELECT COUNT(*) FROM gps_event WHERE vehicle_id = #{vehicleId}")
    Long countByVehicleId(@Param("vehicleId") String vehicleId);
    
    /**
     * 统计指定事件类型的数量
     * 
     * @param eventType 事件类型
     * @return 事件数量
     */
    @Select("SELECT COUNT(*) FROM gps_event WHERE event_type = #{eventType}")
    Long countByEventType(@Param("eventType") String eventType);
}