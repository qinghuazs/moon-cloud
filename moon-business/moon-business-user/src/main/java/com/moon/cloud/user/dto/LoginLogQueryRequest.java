package com.moon.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志查询请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Schema(description = "登录日志查询请求")
public class LoginLogQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "登录IP", example = "192.168.1.1")
    private String loginIp;

    @Schema(description = "登录地址", example = "北京市")
    private String loginLocation;

    @Schema(description = "浏览器", example = "Chrome")
    private String browser;

    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    @Schema(description = "登录状态：0-失败，1-成功", example = "1")
    private Integer status;

    @Schema(description = "开始时间", example = "2024-01-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-12-31T23:59:59")
    private LocalDateTime endTime;
}