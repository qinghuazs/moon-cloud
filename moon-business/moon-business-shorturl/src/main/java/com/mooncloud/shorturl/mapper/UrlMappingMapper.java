package com.mooncloud.shorturl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.enums.UrlStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * URL映射Mapper接口
 *
 * @author mooncloud
 */
@Mapper
public interface UrlMappingMapper extends BaseMapper<UrlMappingEntity> {

    /**
     * 查找过期的URL映射
     *
     * @param currentTime 当前时间
     * @return 过期的URL映射列表
     */
    @Select("SELECT * FROM url_mapping WHERE expires_at < #{currentTime} AND status = 'ACTIVE'")
    List<UrlMappingEntity> findExpiredUrls(@Param("currentTime") Date currentTime);

    /**
     * 批量更新过期URL状态
     *
     * @param currentTime 当前时间
     * @return 更新的记录数
     */
    @Update("UPDATE url_mapping SET status = 'EXPIRED' WHERE expires_at < #{currentTime} AND status = 'ACTIVE'")
    int updateExpiredUrls(@Param("currentTime") Date currentTime);

    /**
     * 增加点击次数
     *
     * @param shortUrl 短链标识符
     * @return 更新的记录数
     */
    @Update("UPDATE url_mapping SET click_count = click_count + 1 WHERE short_url = #{shortUrl}")
    int incrementClickCount(@Param("shortUrl") String shortUrl);

    /**
     * 根据关键词搜索用户的URL
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param page 分页参数
     * @return 搜索结果
     */
    @Select("SELECT * FROM url_mapping WHERE user_id = #{userId} AND " +
           "(original_url LIKE CONCAT('%', #{keyword}, '%') OR title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))")
    Page<UrlMappingEntity> searchByUserIdAndKeyword(@Param("userId") Long userId,
                                                   @Param("keyword") String keyword,
                                                   Page<UrlMappingEntity> page);

    /**
     * 按日期统计访问次数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日访问统计
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
           "FROM url_mapping " +
           "WHERE created_at BETWEEN #{startTime} AND #{endTime} " +
           "GROUP BY DATE(created_at) " +
           "ORDER BY DATE(created_at)")
    List<Object[]> getDailyCreationStats(@Param("startTime") Date startTime,
                                       @Param("endTime") Date endTime);
}