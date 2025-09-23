package com.moon.cloud.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.email.entity.EmailRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 邮件记录Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface EmailRecordMapper extends BaseMapper<EmailRecord> {

    /**
     * 分页查询邮件记录
     *
     * @param page 分页参数
     * @param status 状态
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    IPage<EmailRecord> selectPageList(Page<EmailRecord> page,
                                     @Param("status") Integer status,
                                     @Param("businessType") String businessType,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 根据状态查询邮件记录
     *
     * @param status 状态
     * @param limit 限制数量
     * @return 邮件记录列表
     */
    @Select("SELECT * FROM email_record WHERE status = #{status} ORDER BY created_at ASC LIMIT #{limit}")
    List<EmailRecord> selectByStatus(@Param("status") Integer status, @Param("limit") Integer limit);

    /**
     * 查询待发送的定时邮件
     *
     * @param currentTime 当前时间
     * @param limit 限制数量
     * @return 邮件记录列表
     */
    @Select("SELECT * FROM email_record WHERE status = 0 AND scheduled_at <= #{currentTime} ORDER BY scheduled_at ASC LIMIT #{limit}")
    List<EmailRecord> selectScheduledEmails(@Param("currentTime") LocalDateTime currentTime, @Param("limit") Integer limit);

    /**
     * 查询需要重试的邮件
     *
     * @param maxRetryCount 最大重试次数
     * @param retryInterval 重试间隔(分钟)
     * @param limit 限制数量
     * @return 邮件记录列表
     */
    @Select("SELECT * FROM email_record WHERE status = 3 AND retry_count < #{maxRetryCount} " +
            "AND updated_at <= DATE_SUB(NOW(), INTERVAL #{retryInterval} MINUTE) " +
            "ORDER BY updated_at ASC LIMIT #{limit}")
    List<EmailRecord> selectRetryEmails(@Param("maxRetryCount") Integer maxRetryCount,
                                       @Param("retryInterval") Integer retryInterval,
                                       @Param("limit") Integer limit);

    /**
     * 更新邮件状态
     *
     * @param id 记录ID
     * @param status 状态
     * @param errorMessage 错误信息
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE email_record SET status = #{status}, updated_at = NOW()" +
            "<if test='errorMessage != null'>, error_message = #{errorMessage}</if>" +
            "<if test='status == 2'>, completed_at = NOW()</if>" +
            "<if test='status == 1'>, send_at = NOW()</if>" +
            " WHERE id = #{id}" +
            "</script>")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("errorMessage") String errorMessage);

    /**
     * 增加重试次数
     *
     * @param id 记录ID
     * @return 更新行数
     */
    @Update("UPDATE email_record SET retry_count = retry_count + 1, status = 5, updated_at = NOW() WHERE id = #{id}")
    int incrementRetryCount(@Param("id") Long id);

    /**
     * 统计邮件发送情况
     *
     * @param businessType 业务类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    List<Map<String, Object>> selectEmailStatistics(@Param("businessType") String businessType,
                                                   @Param("startDate") String startDate,
                                                   @Param("endDate") String endDate);

    /**
     * 统计每日发送量
     *
     * @param days 天数
     * @return 统计结果
     */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as total, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as success, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as failed " +
            "FROM email_record " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> selectDailyStatistics(@Param("days") Integer days);

    /**
     * 清理过期的邮件记录
     *
     * @param retentionDays 保留天数
     * @return 删除行数
     */
    @Update("DELETE FROM email_record WHERE created_at < DATE_SUB(NOW(), INTERVAL #{retentionDays} DAY)")
    int cleanExpiredRecords(@Param("retentionDays") Integer retentionDays);

    /**
     * 根据业务ID查询邮件记录
     *
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 邮件记录列表
     */
    @Select("SELECT * FROM email_record WHERE business_id = #{businessId} AND business_type = #{businessType} ORDER BY created_at DESC")
    List<EmailRecord> selectByBusinessId(@Param("businessId") String businessId, @Param("businessType") String businessType);

    /**
     * 统计状态数量
     *
     * @return 状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM email_record GROUP BY status")
    List<Map<String, Object>> selectStatusStatistics();

    /**
     * 根据条件统计状态
     *
     * @param params 查询参数
     * @return 状态统计
     */
    List<Map<String, Object>> selectStatisticsByStatus(@Param("params") Map<String, Object> params);

    /**
     * 按日期统计邮件发送情况
     *
     * @param params 查询参数
     * @return 日期统计
     */
    List<Map<String, Object>> selectDailyStatistics(@Param("params") Map<String, Object> params);

    /**
     * 更新邮件发送时间
     *
     * @param id 记录ID
     * @param sentTime 发送时间
     * @return 更新行数
     */
    @Update("UPDATE email_record SET sent_time = #{sentTime}, updated_at = NOW() WHERE id = #{id}")
    int updateSentTime(@Param("id") Long id, @Param("sentTime") LocalDateTime sentTime);
}