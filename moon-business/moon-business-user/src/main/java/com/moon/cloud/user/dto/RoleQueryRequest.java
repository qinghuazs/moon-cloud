package com.moon.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色查询请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Schema(description = "角色查询请求")
public class RoleQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色编码", example = "ADMIN")
    private String roleCode;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "开始时间", example = "2024-01-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-12-31T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "权限ID", example = "1")
    private Long permissionId;
}