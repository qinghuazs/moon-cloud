package com.mooncloud.shorturl.repository;

import com.mooncloud.shorturl.entity.UrlAccessLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * URL访问记录仓库接口
 * 
 * @author mooncloud
 */
@Repository
public interface UrlAccessLogRepository extends JpaRepository<UrlAccessLogEntity, Long> {
    
    /**
     * 根据短链查找访问记录
     * 
     * @param shortUrl 短链标识符
     * @param pageable 分页参数
     * @return 访问记录分页列表
     */
    Page<UrlAccessLogEntity> findByShortUrlOrderByAccessTimeDesc(String shortUrl, Pageable pageable);
    
    /**
     * 统计短链的总访问次数
     * 
     * @param shortUrl 短链标识符
     * @return 访问次数
     */
    long countByShortUrl(String shortUrl);
    
    /**
     * 统计指定时间范围内的访问次数
     * 
     * @param shortUrl 短链标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 访问次数
     */
    long countByShortUrlAndAccessTimeBetween(String shortUrl, Date startTime, Date endTime);
    
    /**
     * 按日期统计访问次数
     * 
     * @param shortUrl 短链标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日访问统计
     */
    @Query("SELECT DATE(l.accessTime) as date, COUNT(l) as count " +
           "FROM UrlAccessLogEntity l " +
           "WHERE l.shortUrl = :shortUrl AND l.accessTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(l.accessTime) " +
           "ORDER BY DATE(l.accessTime)")
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
    @Query("SELECT l.country, COUNT(l) as count " +
           "FROM UrlAccessLogEntity l " +
           "WHERE l.shortUrl = :shortUrl AND l.accessTime BETWEEN :startTime AND :endTime " +
           "AND l.country IS NOT NULL " +
           "GROUP BY l.country " +
           "ORDER BY COUNT(l) DESC")
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
    @Query("SELECT l.deviceType, COUNT(l) as count " +
           "FROM UrlAccessLogEntity l " +
           "WHERE l.shortUrl = :shortUrl AND l.accessTime BETWEEN :startTime AND :endTime " +
           "AND l.deviceType IS NOT NULL " +
           "GROUP BY l.deviceType " +
           "ORDER BY COUNT(l) DESC")
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
    @Query("SELECT l.browser, COUNT(l) as count " +
           "FROM UrlAccessLogEntity l " +
           "WHERE l.shortUrl = :shortUrl AND l.accessTime BETWEEN :startTime AND :endTime " +
           "AND l.browser IS NOT NULL " +
           "GROUP BY l.browser " +
           "ORDER BY COUNT(l) DESC")
    List<Object[]> getBrowserAccessStats(@Param("shortUrl") String shortUrl, 
                                       @Param("startTime") Date startTime, 
                                       @Param("endTime") Date endTime);
    
    /**
     * 获取最近的访问记录
     * 
     * @param shortUrl 短链标识符
     * @param limit 限制数量
     * @return 最近的访问记录
     */
    @Query("SELECT l FROM UrlAccessLogEntity l " +
           "WHERE l.shortUrl = :shortUrl " +
           "ORDER BY l.accessTime DESC")
    List<UrlAccessLogEntity> getRecentAccessLogs(@Param("shortUrl") String shortUrl, Pageable pageable);
}