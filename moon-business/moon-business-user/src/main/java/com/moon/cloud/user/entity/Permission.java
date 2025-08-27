package com.moon.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 权限实体类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@TableName("sys_permission")
@Schema(description = "权限实体")
public class Permission {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "权限ID")
    private Long id;

    @TableField("permission_code")
    @Schema(description = "权限编码")
    private String permissionCode;

    @TableField("permission_name")
    @Schema(description = "权限名称")
    private String permissionName;

    @TableField("resource_type")
    @Schema(description = "资源类型：MENU-菜单，BUTTON-按钮，API-接口")
    private String resourceType;

    @TableField("resource_url")
    @Schema(description = "资源URL")
    private String resourceUrl;

    @TableField("description")
    @Schema(description = "权限描述")
    private String description;

    @TableField("status")
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    // 状态常量
    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_ENABLED = 1;
    
    // 资源类型常量
    public static final String RESOURCE_TYPE_MENU = "MENU";
    public static final String RESOURCE_TYPE_BUTTON = "BUTTON";
    public static final String RESOURCE_TYPE_API = "API";

    // 构造函数
    public Permission() {}

    public Permission(String permissionCode, String permissionName, String resourceType) {
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.resourceType = resourceType;
        this.status = 1; // 默认启用
    }

    public Permission(String permissionCode, String permissionName, String resourceType, String resourceUrl, String description) {
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.resourceType = resourceType;
        this.resourceUrl = resourceUrl;
        this.description = description;
        this.status = 1; // 默认启用
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", permissionCode='" + permissionCode + '\'' +
                ", permissionName='" + permissionName + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceUrl='" + resourceUrl + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}