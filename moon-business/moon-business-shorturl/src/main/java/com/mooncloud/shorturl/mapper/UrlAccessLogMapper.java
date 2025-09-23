package com.mooncloud.shorturl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mooncloud.shorturl.entity.UrlAccessLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * URL访问记录Mapper接口
 *
 * @author mooncloud
 */
@Mapper
public interface UrlAccessLogMapper extends BaseMapper<UrlAccessLogEntity> {

    /**
     * 按日期统计访问次数
     *
     * @param shortUrl 短链标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日访问统计
     */
    @Select("SELECT DATE(access_time) as date, COUNT(*) as count " +
           "FROM url_access_log " +
           "WHERE short_url = #{shortUrl} AND access_time BETWEEN #{startTime} AND #{endTime} " +
           "GROUP BY DATE(access_time) " +
           "ORDER BY DATE(access_time)")
    List<Object[]> getDailyAccessStats(@Param("shortUrl") String shortUrl,
                                     @Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime);

    /**
     * 按国家统计访问次数
     *
     * @param shortUrl 短链标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 国家访问统计
     */
    @Select("SELECT country, COUNT(*) as count " +
           "FROM url_access_log " +
           "WHERE short_url = #{shortUrl} AND access_time BETWEEN #{startTime} AND #{endTime} " +
           "AND country IS NOT NULL " +
           "GROUP BY country " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> getCountryAccessStats(@Param("shortUrl") String shortUrl,
                                       @Param("startTime") Date startTime,
                                       @Param("endTime") Date endTime);

    /**
     * 按设备类型统计访问次数
     *
     * @param shortUrl 短链标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 设备类型访问统计
     */
    @Select("SELECT device_type, COUNT(*) as count " +
           "FROM url_access_log " +
           "WHERE short_url = #{shortUrl} AND access_time BETWEEN #{startTime} AND #{endTime} " +
           "AND device_type IS NOT NULL " +
           "GROUP BY device_type " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> getDeviceTypeAccessStats(@Param("shortUrl") String shortUrl,
                                          @Param("startTime") Date startTime,
                                          @Param("endTime") Date endTime);

    /**
     * 按浏览器统计访问次数
     *
     * @param shortUrl 短链标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 浏览器访问统计
     */
    @Select("SELECT browser, COUNT(*) as count " +
           "FROM url_access_log " +
           "WHERE short_url = #{shortUrl} AND access_time BETWEEN #{startTime} AND #{endTime} " +
           "AND browser IS NOT NULL " +
           "GROUP BY browser " +
           "ORDER BY COUNT(*) DESC")
    List<Object[]> getBrowserAccessStats(@Param("shortUrl") String shortUrl,
                                       @Param("startTime") Date startTime,
                                       @Param("endTime") Date endTime);

    /**
     * 获取最近的访问记录
     *
     * @param shortUrl 短链标识符
     * @param page 分页参数
     * @return 最近的访问记录
     */
    @Select("SELECT * FROM url_access_log " +
           "WHERE short_url = #{shortUrl} " +
           "ORDER BY access_time DESC")
    Page<UrlAccessLogEntity> getRecentAccessLogs(@Param("shortUrl") String shortUrl, Page<UrlAccessLogEntity> page);

    /**
     * 统计短链的独立IP数量
     *
     * @param shortUrl 短链标识符
     * @return 独立IP数量
     */
    @Select("SELECT COUNT(DISTINCT ip_address) FROM url_access_log WHERE short_url = #{shortUrl}")
    Long countDistinctIpsByShortUrl(@Param("shortUrl") String shortUrl);

    /**
     * 统计短链在指定时间段的独立IP数量
     *
     * @param shortUrl 短链标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 独立IP数量
     */
    @Select("SELECT COUNT(DISTINCT ip_address) FROM url_access_log WHERE short_url = #{shortUrl} AND access_time BETWEEN #{startTime} AND #{endTime}")
    Long countDistinctIpsByShortUrlAndTimeBetween(@Param("shortUrl") String shortUrl,
                                                 @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime);

    /**
     * 获取指定时间段内访问量最高的短链
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 分页参数
     * @return 短链访问统计
     */
    @Select("SELECT short_url, COUNT(*) as accessCount FROM url_access_log WHERE access_time BETWEEN #{startTime} AND #{endTime} GROUP BY short_url ORDER BY accessCount DESC")
    Page<Object[]> findTopAccessedShortUrlsBetween(@Param("startTime") Date startTime,
                                                  @Param("endTime") Date endTime,
                                                  Page<Object[]> page);

    /**
     * 获取指定时间段内独立用户数最多的短链
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 分页参数
     * @return 短链用户统计
     */
    @Select("SELECT short_url, COUNT(DISTINCT ip_address) as uniqueUsers FROM url_access_log WHERE access_time BETWEEN #{startTime} AND #{endTime} GROUP BY short_url ORDER BY uniqueUsers DESC")
    Page<Object[]> findTopUniqueUsersShortUrlsBetween(@Param("startTime") Date startTime,
                                                     @Param("endTime") Date endTime,
                                                     Page<Object[]> page);

    /**
     * 按设备类型统计访问次数
     *
     * @return 设备类型访问次数统计
     */
    @Select("SELECT device_type, COUNT(*) as count FROM url_access_log GROUP BY device_type ORDER BY count DESC")
    List<Object[]> countByDeviceType();
}