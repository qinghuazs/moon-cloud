package com.moon.cloud.business.gps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.business.gps.entity.GpsData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS数据Mapper接口
 * 
 * @author mooncloud
 */
@Mapper
public interface GpsDataMapper extends BaseMapper<GpsData> {
    
    /**
     * 根据车辆ID查询最新的GPS数据
     * 
     * @param vehicleId 车辆ID
     * @return GPS数据
     */
    @Select("SELECT * FROM gps_data WHERE vehicle_id = #{vehicleId} ORDER BY gps_time DESC LIMIT 1")
    GpsData findLatestByVehicleId(@Param("vehicleId") String vehicleId);
    
    /**
     * 根据车辆ID和时间范围查询GPS数据
     * 
     * @param vehicleId 车辆ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return GPS数据列表
     */
    @Select("SELECT * FROM gps_data WHERE vehicle_id = #{vehicleId} AND gps_time BETWEEN #{startTime} AND #{endTime} ORDER BY gps_time")
    List<GpsData> findByVehicleIdAndTimeRange(@Param("vehicleId") String vehicleId, 
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);
}