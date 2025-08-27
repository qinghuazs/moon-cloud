package com.moon.cloud.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.LoginLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface LoginLogService {

    /**
     * 记录登录日志
     *
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @param loginStatus 登录状态
     * @return 登录日志
     */
    LoginLog recordLoginLog(Long userId, String ipAddress, String userAgent, Integer loginStatus);

    /**
     * 记录登录成功日志
     *
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 登录日志
     */
    LoginLog recordSuccessLogin(Long userId, String ipAddress, String userAgent);

    /**
     * 记录登录失败日志
     *
     * @param userId 用户ID（可为空）
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 登录日志
     */
    LoginLog recordFailedLogin(Long userId, String ipAddress, String userAgent);

    /**
     * 根据用户ID获取登录日志
     *
     * @param userId 用户ID
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByUserId(Long userId);

    /**
     * 分页查询登录日志
     *
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param ipAddress IP地址（可选）
     * @param loginStatus 登录状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 登录日志分页列表
     */
    IPage<LoginLog> getLoginLogPage(Page<LoginLog> page, Long userId, String ipAddress, 
                                   Integer loginStatus, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据IP地址和时间范围查询登录日志
     *
     * @param ipAddress IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录日志列表
     */
    List<LoginLog> getLoginLogsByIpAndTimeRange(String ipAddress, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户最近的登录记录
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 登录日志列表
     */
    List<LoginLog> getRecentLoginLogs(Long userId, Integer limit);

    /**
     * 统计用户登录次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 登录次数
     */
    Long countUserLogins(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计今日登录用户数
     *
     * @return 今日登录用户数
     */
    Long countTodayLoginUsers();

    /**
     * 统计指定IP地址的登录失败次数
     *
     * @param ipAddress IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录失败次数
     */
    Long countFailedLoginsByIp(String ipAddress, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定用户的登录失败次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录失败次数
     */
    Long countFailedLoginsByUser(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除指定时间之前的登录日志
     *
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    int deleteLoginLogsBefore(LocalDateTime beforeTime);

    /**
     * 批量删除登录日志
     *
     * @param logIds 日志ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteLoginLogs(List<Long> logIds);

    /**
     * 清理过期的登录日志（保留指定天数）
     *
     * @param retentionDays 保留天数
     * @return 清理的记录数
     */
    int cleanExpiredLoginLogs(int retentionDays);

    /**
     * 检查IP地址是否被锁定（基于失败次数）
     *
     * @param ipAddress IP地址
     * @param maxFailedAttempts 最大失败次数
     * @param lockDurationMinutes 锁定时长（分钟）
     * @return 是否被锁定
     */
    boolean isIpLocked(String ipAddress, int maxFailedAttempts, int lockDurationMinutes);

    /**
     * 检查用户是否被锁定（基于失败次数）
     *
     * @param userId 用户ID
     * @param maxFailedAttempts 最大失败次数
     * @param lockDurationMinutes 锁定时长（分钟）
     * @return 是否被锁定
     */
    boolean isUserLocked(Long userId, int maxFailedAttempts, int lockDurationMinutes);
}