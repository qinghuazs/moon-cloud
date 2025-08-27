package com.moon.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限更新请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Schema(description = "权限更新请求")
public class PermissionUpdateRequest {

    @Schema(description = "权限名称", example = "用户管理")
    @Size(min = 2, max = 50, message = "权限名称长度必须在2-50个字符之间")
    private String permissionName;

    @Schema(description = "权限编码", example = "USER_MANAGE")
    @Size(min = 2, max = 100, message = "权限编码长度必须在2-100个字符之间")
    @Pattern(regexp = "^[A-Z_:]+$", message = "权限编码只能包含大写字母、下划线和冒号")
    private String permissionCode;

    @Schema(description = "权限描述", example = "用户管理相关权限")
    @Size(max = 500, message = "权限描述长度不能超过500个字符")
    private String description;

    @Schema(description = "资源类型：MENU-菜单，BUTTON-按钮，API-接口", example = "MENU")
    @Pattern(regexp = "^(MENU|BUTTON|API)$", message = "资源类型只能是MENU、BUTTON或API")
    private String resourceType;

    @Schema(description = "资源URL", example = "/user/list")
    @Size(max = 200, message = "资源URL长度不能超过200个字符")
    private String resourceUrl;

    @Schema(description = "HTTP方法", example = "GET")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)$", message = "HTTP方法不正确")
    private String httpMethod;

    @Schema(description = "父权限ID", example = "1")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "系统内置权限")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}