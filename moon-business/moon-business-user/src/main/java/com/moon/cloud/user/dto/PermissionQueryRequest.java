package com.moon.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限查询请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Schema(description = "权限查询请求")
public class PermissionQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Long current = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;

    @Schema(description = "权限名称", example = "用户管理")
    private String permissionName;

    @Schema(description = "权限编码", example = "USER_MANAGE")
    private String permissionCode;

    @Schema(description = "资源类型：MENU-菜单，BUTTON-按钮，API-接口", example = "MENU")
    private String resourceType;

    @Schema(description = "资源URL", example = "/user/list")
    private String resourceUrl;

    @Schema(description = "HTTP方法", example = "GET")
    private String httpMethod;

    @Schema(description = "父权限ID", example = "1")
    private Long parentId;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "开始时间", example = "2024-01-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-12-31T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "角色ID", example = "1")
    private Long roleId;
}