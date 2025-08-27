package com.moon.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@TableName("sys_login_log")
@Schema(description = "登录日志实体")
public class LoginLog {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("ip_address")
    @Schema(description = "IP地址")
    private String ipAddress;

    @TableField("user_agent")
    @Schema(description = "用户代理")
    private String userAgent;

    @TableField("login_status")
    @Schema(description = "登录状态：0-失败，1-成功")
    private Integer loginStatus;

    @TableField(value = "login_time", fill = FieldFill.INSERT)
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    @TableField(exist = false)
    @Schema(description = "用户名")
    private String username;

    // 登录状态常量
    public static final int LOGIN_STATUS_FAILED = 0;
    public static final int LOGIN_STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 0;
    public static final int STATUS_SUCCESS = 1;

    // 构造函数
    public LoginLog() {}

    public LoginLog(Long userId, String ipAddress, String userAgent, Integer loginStatus) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginStatus = loginStatus;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(Integer loginStatus) {
        this.loginStatus = loginStatus;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "LoginLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", ipAddress='" + ipAddress + '\'' +
                ", loginStatus=" + loginStatus +
                ", loginTime=" + loginTime +
                '}';
    }
}