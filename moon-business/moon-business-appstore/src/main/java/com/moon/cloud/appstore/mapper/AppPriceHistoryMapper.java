package com.moon.cloud.appstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.appstore.entity.AppPriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * APP价格历史记录Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Mapper
public interface AppPriceHistoryMapper extends BaseMapper<AppPriceHistory> {

    /**
     * 获取APP的最新价格记录
     *
     * @param appId App Store ID
     * @return 最新价格记录
     */
    @Select("SELECT * FROM app_price_history WHERE app_id = #{appId} ORDER BY created_at DESC LIMIT 1")
    AppPriceHistory getLatestPriceRecord(@Param("appId") String appId);

    /**
     * 获取指定时间段内的价格变化记录
     *
     * @param appId     App Store ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 价格历史列表
     */
    @Select("SELECT * FROM app_price_history WHERE app_id = #{appId} " +
            "AND change_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY change_time DESC")
    List<AppPriceHistory> getPriceHistoryByTimeRange(@Param("appId") String appId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 获取限免应用列表
     *
     * @param limit 限制数量
     * @return 限免应用列表
     */
    @Select("SELECT * FROM app_price_history WHERE change_type = 'FREE' " +
            "AND change_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "ORDER BY change_time DESC LIMIT #{limit}")
    List<AppPriceHistory> getRecentFreeApps(@Param("limit") int limit);

    /**
     * 获取最近的降价应用
     *
     * @param days  天数
     * @param limit 限制数量
     * @return 降价应用列表
     */
    @Select("SELECT * FROM app_price_history " +
            "WHERE change_type IN ('DECREASE', 'FREE') " +
            "AND change_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY change_time DESC LIMIT #{limit}")
    List<AppPriceHistory> getRecentPriceDrops(@Param("days") int days,
                                              @Param("limit") int limit);

    /**
     * 统计指定APP的价格变化次数
     *
     * @param appId App Store ID
     * @return 变化次数
     */
    @Select("SELECT COUNT(*) FROM app_price_history WHERE app_id = #{appId} AND change_type != 'INITIAL'")
    Integer countPriceChanges(@Param("appId") String appId);

    /**
     * 获取APP的历史最低价
     *
     * @param appId App Store ID
     * @return 历史最低价
     */
    @Select("SELECT MIN(new_price) FROM app_price_history WHERE app_id = #{appId} AND new_price > 0")
    BigDecimal getHistoricalLowestPrice(@Param("appId") String appId);

    /**
     * 获取APP的历史最高价
     *
     * @param appId App Store ID
     * @return 历史最高价
     */
    @Select("SELECT MAX(new_price) FROM app_price_history WHERE app_id = #{appId}")
    BigDecimal getHistoricalHighestPrice(@Param("appId") String appId);
}