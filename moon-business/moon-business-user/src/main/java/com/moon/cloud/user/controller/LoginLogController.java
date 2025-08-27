package com.moon.cloud.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moon.cloud.response.web.MoonCloudResponse;
import com.moon.cloud.user.dto.LoginLogQueryRequest;
import com.moon.cloud.user.entity.LoginLog;
import com.moon.cloud.user.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志管理控制器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Tag(name = "登录日志管理", description = "登录日志管理相关接口")
@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志", description = "根据条件分页查询登录日志")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping
    public MoonCloudResponse<IPage<LoginLog>> getLoginLogs(@Valid LoginLogQueryRequest request) {
        IPage<LoginLog> loginLogs = loginLogService.getLoginLogPage(
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(request.getCurrent(), request.getSize()),
            request.getUserId(),
            request.getLoginIp(),
            request.getStatus(),
            request.getStartTime(),
            request.getEndTime()
        );
        return MoonCloudResponse.success(loginLogs);
    }

    @Operation(summary = "根据用户ID查询登录日志", description = "分页查询指定用户的登录日志")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/user/{userId}")
    public MoonCloudResponse<List<LoginLog>> getLoginLogsByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        List<LoginLog> loginLogs = loginLogService.getLoginLogsByUserId(userId);
        return MoonCloudResponse.success(loginLogs);
    }

    @Operation(summary = "根据IP地址查询登录日志", description = "分页查询指定IP地址的登录日志")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/ip/{ipAddress}")
    public MoonCloudResponse<List<LoginLog>> getLoginLogsByIpAddress(
            @Parameter(description = "IP地址") @PathVariable String ipAddress,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        List<LoginLog> loginLogs = loginLogService.getLoginLogsByIpAndTimeRange(ipAddress, null, null);
        return MoonCloudResponse.success(loginLogs);
    }

    @Operation(summary = "根据时间范围查询登录日志", description = "分页查询指定时间范围内的登录日志")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/time-range")
    public MoonCloudResponse<List<LoginLog>> getLoginLogsByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        List<LoginLog> loginLogs = loginLogService.getLoginLogsByIpAndTimeRange(null, startTime, endTime);
        return MoonCloudResponse.success(loginLogs);
    }

    @Operation(summary = "获取用户最近登录记录", description = "获取指定用户的最近一次登录记录")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/user/{userId}/latest")
    public MoonCloudResponse<LoginLog> getLatestLoginLog(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<LoginLog> recentLogs = loginLogService.getRecentLoginLogs(userId, 1);
        LoginLog loginLog = recentLogs.isEmpty() ? null : recentLogs.get(0);
        return MoonCloudResponse.success(loginLog);
    }

    @Operation(summary = "统计用户登录次数", description = "统计指定用户的登录次数")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/user/{userId}/count")
    public MoonCloudResponse<Long> countUserLogins(@Parameter(description = "用户ID") @PathVariable Long userId) {
        long count = loginLogService.countUserLogins(userId, null, null);
        return MoonCloudResponse.success(count);
    }

    @Operation(summary = "统计今日登录用户数", description = "统计今日登录的用户数量")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/today/users/count")
    public MoonCloudResponse<Long> countTodayLoginUsers() {
        long count = loginLogService.countTodayLoginUsers();
        return MoonCloudResponse.success(count);
    }

    @Operation(summary = "统计登录失败次数", description = "统计指定用户或IP的登录失败次数")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/failures/count")
    public MoonCloudResponse<Long> countLoginFailures(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        long count = ipAddress != null ? loginLogService.countFailedLoginsByIp(ipAddress, startTime, endTime) : 
                     userId != null ? loginLogService.countFailedLoginsByUser(userId, startTime, endTime) : 0L;
        return MoonCloudResponse.success(count);
    }

    @Operation(summary = "删除登录日志", description = "根据ID删除登录日志")
    @PreAuthorize("hasAuthority('login_log:delete')")
    @DeleteMapping("/{id}")
    public MoonCloudResponse<Void> deleteLoginLog(@Parameter(description = "登录日志ID") @PathVariable Long id) {
        loginLogService.batchDeleteLoginLogs(List.of(id));
        return MoonCloudResponse.success();
    }

    @Operation(summary = "批量删除登录日志", description = "根据ID列表批量删除登录日志")
    @PreAuthorize("hasAuthority('login_log:delete')")
    @DeleteMapping("/batch")
    public MoonCloudResponse<Void> deleteLoginLogs(@RequestBody List<Long> ids) {
        loginLogService.batchDeleteLoginLogs(ids);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "清理过期登录日志", description = "清理指定天数之前的登录日志")
    @PreAuthorize("hasAuthority('login_log:delete')")
    @DeleteMapping("/cleanup")
    public MoonCloudResponse<Integer> cleanupExpiredLogs(@RequestParam Integer days) {
        int deletedCount = loginLogService.cleanExpiredLoginLogs(days);
        return MoonCloudResponse.success(deletedCount);
    }

    @Operation(summary = "检查IP是否被锁定", description = "检查指定IP地址是否被锁定")
    @GetMapping("/check/ip/{ipAddress}/locked")
    public MoonCloudResponse<Boolean> isIpLocked(@Parameter(description = "IP地址") @PathVariable String ipAddress) {
        boolean isLocked = loginLogService.isIpLocked(ipAddress, 5, 30);
        return MoonCloudResponse.success(isLocked);
    }

    @Operation(summary = "检查用户是否被锁定", description = "检查指定用户是否被锁定")
    @GetMapping("/check/user/{userId}/locked")
    public MoonCloudResponse<Boolean> isUserLocked(@Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean isLocked = loginLogService.isUserLocked(userId, 5, 30);
        return MoonCloudResponse.success(isLocked);
    }

    @Operation(summary = "获取登录统计信息", description = "获取登录相关的统计信息")
    @PreAuthorize("hasAuthority('login_log:read')")
    @GetMapping("/statistics")
    public MoonCloudResponse<Object> getLoginStatistics() {
        // 构建统计信息
        long todayLoginUsers = loginLogService.countTodayLoginUsers();
        // 暂时设置为0，因为接口不支持全局统计
        long totalLogins = 0L;
        
        // 可以根据需要添加更多统计信息
        return MoonCloudResponse.success(Map.of(
            "todayLoginUsers", todayLoginUsers,
            "totalLogins", totalLogins
        ));
    }
}