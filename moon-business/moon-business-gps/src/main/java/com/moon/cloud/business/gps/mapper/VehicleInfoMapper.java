package com.moon.cloud.business.gps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.business.gps.entity.VehicleInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 车辆信息Mapper接口
 * 
 * @author mooncloud
 */
public interface VehicleInfoMapper extends BaseMapper<VehicleInfo> {


    /**
     * 根据车辆ID查询车辆信息
     * 
     * @param vehicleId 车辆ID
     * @return 车辆信息
     */
    @Select("SELECT * FROM vehicle_info WHERE vehicle_id = #{vehicleId}")
    VehicleInfo selectByVehicleId(@Param("vehicleId") String vehicleId);
    
    /**
     * 根据状态查询车辆列表
     * 
     * @param status 状态
     * @return 车辆列表
     */
    @Select("SELECT * FROM vehicle_info WHERE status = #{status} ORDER BY create_time DESC")
    List<VehicleInfo> selectByStatus(@Param("status") Integer status);
    
    /**
     * 根据车辆类型查询车辆列表
     * 
     * @param vehicleType 车辆类型
     * @return 车辆列表
     */
    @Select("SELECT * FROM vehicle_info WHERE vehicle_type = #{vehicleType} ORDER BY create_time DESC")
    List<VehicleInfo> selectByVehicleType(@Param("vehicleType") String vehicleType);
    
    /**
     * 根据司机姓名模糊查询车辆列表
     * 
     * @param driverName 司机姓名
     * @return 车辆列表
     */
    @Select("SELECT * FROM vehicle_info WHERE driver_name LIKE CONCAT('%', #{driverName}, '%') ORDER BY create_time DESC")
    List<VehicleInfo> selectByDriverNameLike(@Param("driverName") String driverName);
}