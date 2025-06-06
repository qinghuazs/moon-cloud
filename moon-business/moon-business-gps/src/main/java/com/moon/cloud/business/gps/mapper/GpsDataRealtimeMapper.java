package com.moon.cloud.business.gps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.business.gps.entity.GpsDataRealtime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS实时数据Mapper接口
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Mapper
public interface GpsDataRealtimeMapper extends BaseMapper<GpsDataRealtime> {

    /**
     * 根据车辆ID查询最新的GPS数据
     *
     * @param vehicleId 车辆ID
     * @return GPS实时数据
     */
    @Select("SELECT * FROM gps_data_realtime WHERE vehicle_id = #{vehicleId} ORDER BY gps_time DESC LIMIT 1")
    GpsDataRealtime selectLatestByVehicleId(@Param("vehicleId") String vehicleId);

    /**
     * 根据车辆ID列表查询最新的GPS数据
     *
     * @param vehicleIds 车辆ID列表
     * @return GPS实时数据列表
     */
    @Select("<script>" +
            "SELECT t1.* FROM gps_data_realtime t1 " +
            "INNER JOIN (" +
            "  SELECT vehicle_id, MAX(gps_time) as max_time " +
            "  FROM gps_data_realtime " +
            "  WHERE vehicle_id IN " +
            "  <foreach collection='vehicleIds' item='vehicleId' open='(' separator=',' close=')'>" +
            "    #{vehicleId}" +
            "  </foreach>" +
            "  GROUP BY vehicle_id" +
            ") t2 ON t1.vehicle_id = t2.vehicle_id AND t1.gps_time = t2.max_time" +
            "</script>")
    List<GpsDataRealtime> selectLatestByVehicleIds(@Param("vehicleIds") List<String> vehicleIds);

    /**
     * 根据车辆ID和时间范围查询GPS数据
     *
     * @param vehicleId 车辆ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return GPS实时数据列表
     */
    @Select("SELECT * FROM gps_data_realtime " +
            "WHERE vehicle_id = #{vehicleId} " +
            "AND gps_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY gps_time ASC")
    List<GpsDataRealtime> selectByVehicleIdAndTimeRange(@Param("vehicleId") String vehicleId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查询所有车辆的最新GPS数据
     *
     * @return GPS实时数据列表
     */
    @Select("SELECT t1.* FROM gps_data_realtime t1 " +
            "INNER JOIN (" +
            "  SELECT vehicle_id, MAX(gps_time) as max_time " +
            "  FROM gps_data_realtime " +
            "  GROUP BY vehicle_id" +
            ") t2 ON t1.vehicle_id = t2.vehicle_id AND t1.gps_time = t2.max_time")
    List<GpsDataRealtime> selectAllLatest();

    /**
     * 删除指定时间之前的数据（用于数据清理）
     *
     * @param beforeTime 指定时间
     * @return 删除的记录数
     */
    @Select("DELETE FROM gps_data_realtime WHERE gps_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

}