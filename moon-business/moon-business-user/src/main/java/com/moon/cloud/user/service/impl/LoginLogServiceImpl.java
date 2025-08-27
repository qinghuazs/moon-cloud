package com.moon.cloud.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.LoginLog;
import com.moon.cloud.user.mapper.LoginLogMapper;
import com.moon.cloud.user.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Override
    public LoginLog recordLoginLog(Long userId, String ipAddress, String userAgent, Integer loginStatus) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setIpAddress(ipAddress);
        loginLog.setUserAgent(userAgent);
        loginLog.setLoginStatus(loginStatus);
        loginLog.setLoginTime(LocalDateTime.now());
        
        loginLogMapper.insert(loginLog);
        return loginLog;
    }

    @Override
    public LoginLog recordSuccessLogin(Long userId, String ipAddress, String userAgent) {
        return recordLoginLog(userId, ipAddress, userAgent, LoginLog.STATUS_SUCCESS);
    }

    @Override
    public LoginLog recordFailedLogin(Long userId, String ipAddress, String userAgent) {
        return recordLoginLog(userId, ipAddress, userAgent, LoginLog.STATUS_FAILED);
    }

    @Override
    public List<LoginLog> getLoginLogsByUserId(Long userId) {
        return loginLogMapper.selectByUserId(userId);
    }

    @Override
    public IPage<LoginLog> getLoginLogPage(Page<LoginLog> page, Long userId, String ipAddress, 
                                          Integer loginStatus, LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogMapper.selectLoginLogPageWithUsername(page, userId, null, ipAddress, loginStatus, startTime, endTime);
    }

    @Override
    public List<LoginLog> getLoginLogsByIpAndTimeRange(String ipAddress, LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogMapper.selectByIpAddressAndTimeRange(ipAddress, startTime, endTime);
    }

    @Override
    public List<LoginLog> getRecentLoginLogs(Long userId, Integer limit) {
        return loginLogMapper.selectRecentLoginsByUserId(userId, limit);
    }

    @Override
    public Long countUserLogins(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogMapper.countLoginLogs(userId, LoginLog.STATUS_SUCCESS, startTime, endTime);
    }

    @Override
    public Long countTodayLoginUsers() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return loginLogMapper.countTodayLoginUsers(startOfDay, endOfDay);
    }

    @Override
    public Long countFailedLoginsByIp(String ipAddress, LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogMapper.countFailedLoginsByIp(ipAddress, startTime, endTime);
    }

    @Override
    public Long countFailedLoginsByUser(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return loginLogMapper.countFailedLoginsByUser(userId, startTime, endTime);
    }

    @Override
    public int deleteLoginLogsBefore(LocalDateTime beforeTime) {
        return loginLogMapper.deleteLoginLogsBefore(beforeTime);
    }

    @Override
    public boolean batchDeleteLoginLogs(List<Long> logIds) {
        if (CollectionUtils.isEmpty(logIds)) {
            return false;
        }
        return loginLogMapper.batchDeleteLoginLogs(logIds) > 0;
    }

    @Override
    public int cleanExpiredLoginLogs(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        return deleteLoginLogsBefore(cutoffTime);
    }

    @Override
    public boolean isIpLocked(String ipAddress, int maxFailedAttempts, int lockDurationMinutes) {
        LocalDateTime lockStartTime = LocalDateTime.now().minusMinutes(lockDurationMinutes);
        Long failedCount = countFailedLoginsByIp(ipAddress, lockStartTime, LocalDateTime.now());
        return failedCount >= maxFailedAttempts;
    }

    @Override
    public boolean isUserLocked(Long userId, int maxFailedAttempts, int lockDurationMinutes) {
        LocalDateTime lockStartTime = LocalDateTime.now().minusMinutes(lockDurationMinutes);
        Long failedCount = countFailedLoginsByUser(userId, lockStartTime, LocalDateTime.now());
        return failedCount >= maxFailedAttempts;
    }
}