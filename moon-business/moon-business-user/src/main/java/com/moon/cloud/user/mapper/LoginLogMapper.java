package com.moon.cloud.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 根据用户ID查询登录日志列表
     *
     * @param userId 用户ID
     * @return 登录日志列表
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC")
    List<LoginLog> selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询登录日志（包含用户名）
     *
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param username 用户名（模糊查询）
     * @param ipAddress IP地址（模糊查询）
     * @param loginStatus 登录状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录日志分页列表
     */
    IPage<LoginLog> selectLoginLogPageWithUsername(Page<LoginLog> page,
                                                  @Param("userId") Long userId,
                                                  @Param("username") String username,
                                                  @Param("ipAddress") String ipAddress,
                                                  @Param("loginStatus") Integer loginStatus,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 根据IP地址查询登录日志
     *
     * @param ipAddress IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录日志列表
     */
    @Select("SELECT * FROM sys_login_log WHERE ip_address = #{ipAddress} " +
            "AND login_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY login_time DESC")
    List<LoginLog> selectByIpAddressAndTimeRange(@Param("ipAddress") String ipAddress,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户最近的登录记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 登录日志列表
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} " +
            "ORDER BY login_time DESC LIMIT #{limit}")
    List<LoginLog> selectRecentLoginsByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计登录次数
     *
     * @param userId 用户ID（可选）
     * @param loginStatus 登录状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 登录次数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_login_log WHERE 1=1" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='loginStatus != null'> AND login_status = #{loginStatus}</if>" +
            "<if test='startTime != null'> AND login_time >= #{startTime}</if>" +
            "<if test='endTime != null'> AND login_time <= #{endTime}</if>" +
            "</script>")
    Long countLoginLogs(@Param("userId") Long userId,
                       @Param("loginStatus") Integer loginStatus,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计今日登录用户数
     *
     * @param startTime 今日开始时间
     * @param endTime 今日结束时间
     * @return 登录用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM sys_login_log " +
            "WHERE login_status = 1 AND login_time BETWEEN #{startTime} AND #{endTime}")
    Long countTodayLoginUsers(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询登录失败次数（指定时间范围内）
     *
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM sys_login_log " +
            "WHERE login_status = 0 " +
            "AND (user_id = #{userId} OR ip_address = #{ipAddress}) " +
            "AND login_time BETWEEN #{startTime} AND #{endTime}")
    Long countFailedLogins(@Param("userId") Long userId,
                          @Param("ipAddress") String ipAddress,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的登录日志
     *
     * @param beforeTime 指定时间
     * @return 删除行数
     */
    @Select("DELETE FROM sys_login_log WHERE login_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量删除登录日志
     *
     * @param ids 日志ID列表
     * @return 删除行数
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 统计指定IP地址的失败登录次数
     *
     * @param ipAddress IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM sys_login_log " +
            "WHERE login_status = 0 AND ip_address = #{ipAddress} " +
            "AND login_time BETWEEN #{startTime} AND #{endTime}")
    Long countFailedLoginsByIp(@Param("ipAddress") String ipAddress,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定用户的失败登录次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM sys_login_log " +
            "WHERE login_status = 0 AND user_id = #{userId} " +
            "AND login_time BETWEEN #{startTime} AND #{endTime}")
    Long countFailedLoginsByUser(@Param("userId") Long userId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的登录日志
     *
     * @param beforeTime 指定时间
     * @return 删除行数
     */
    @Select("DELETE FROM sys_login_log WHERE login_time < #{beforeTime}")
    int deleteLoginLogsBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量删除登录日志
     *
     * @param ids 日志ID列表
     * @return 删除行数
     */
    int batchDeleteLoginLogs(@Param("ids") List<Long> ids);
}