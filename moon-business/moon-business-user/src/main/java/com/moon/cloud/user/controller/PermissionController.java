package com.moon.cloud.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.response.web.MoonCloudResponse;
import com.moon.cloud.user.dto.PermissionCreateRequest;
import com.moon.cloud.user.dto.PermissionQueryRequest;
import com.moon.cloud.user.dto.PermissionUpdateRequest;
import com.moon.cloud.user.entity.Permission;
import com.moon.cloud.user.service.PermissionService;
import com.moon.cloud.response.web.MoonCloudResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 权限管理控制器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Tag(name = "权限管理", description = "权限管理相关接口")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Operation(summary = "创建权限", description = "创建新权限")
    @PreAuthorize("hasAuthority('permission:create')")
    @PostMapping
    public MoonCloudResponse<Permission> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        Permission permission = new Permission();
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionCode(request.getPermissionCode());
        permission.setResourceType(request.getResourceType());
        permission.setResourceUrl(request.getResourceUrl());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus());
        Permission createdPermission = permissionService.createPermission(permission);
        return MoonCloudResponse.success(createdPermission);
    }

    @Operation(summary = "更新权限", description = "根据ID更新权限信息")
    @PreAuthorize("hasAuthority('permission:update')")
    @PutMapping("/{id}")
    public MoonCloudResponse<Permission> updatePermission(
            @Parameter(description = "权限ID") @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequest request) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionCode(request.getPermissionCode());
        permission.setResourceType(request.getResourceType());
        permission.setResourceUrl(request.getResourceUrl());
        permission.setDescription(request.getDescription());
        permission.setStatus(request.getStatus());
        Permission updatedPermission = permissionService.updatePermission(permission);
        return MoonCloudResponse.success(updatedPermission);
    }

    @Operation(summary = "删除权限", description = "根据ID删除权限")
    @PreAuthorize("hasAuthority('permission:delete')")
    @DeleteMapping("/{id}")
    public MoonCloudResponse<Void> deletePermission(@Parameter(description = "权限ID") @PathVariable Long id) {
        permissionService.deletePermission(id);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "批量删除权限", description = "根据ID列表批量删除权限")
    @PreAuthorize("hasAuthority('permission:delete')")
    @DeleteMapping("/batch")
    public MoonCloudResponse<Void> deletePermissions(@RequestBody List<Long> ids) {
        permissionService.batchDeletePermissions(ids);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "获取权限详情", description = "根据ID获取权限详细信息")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/{id}")
    public MoonCloudResponse<Permission> getPermissionById(@Parameter(description = "权限ID") @PathVariable Long id) {
        Permission permission = permissionService.getPermissionById(id);
        return MoonCloudResponse.success(permission);
    }

    @Operation(summary = "根据编码获取权限", description = "根据权限编码获取权限信息")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/code/{code}")
    public MoonCloudResponse<Permission> getPermissionByCode(@Parameter(description = "权限编码") @PathVariable String code) {
        Permission permission = permissionService.getPermissionByCode(code);
        return MoonCloudResponse.success(permission);
    }

    @Operation(summary = "分页查询权限", description = "根据条件分页查询权限列表")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping
    public MoonCloudResponse<IPage<Permission>> getPermissions(@Valid PermissionQueryRequest request) {
        Page<Permission> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<Permission> permissions = permissionService.getPermissionPage(
            page,
            request.getPermissionName(),
            request.getPermissionCode(),
            request.getResourceType(),
            request.getStatus()
        );
        return MoonCloudResponse.success(permissions);
    }

    @Operation(summary = "获取所有启用权限", description = "获取所有状态为启用的权限")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/enabled")
    public MoonCloudResponse<List<Permission>> getEnabledPermissions() {
        List<Permission> permissions = permissionService.getEnabledPermissions();
        return MoonCloudResponse.success(permissions);
    }

    @Operation(summary = "根据用户ID获取权限", description = "获取指定用户的所有权限")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/user/{userId}")
    public MoonCloudResponse<List<Permission>> getPermissionsByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
        return MoonCloudResponse.success(permissions);
    }

    @Operation(summary = "根据角色ID获取权限", description = "获取指定角色的所有权限")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/role/{roleId}")
    public MoonCloudResponse<List<Permission>> getPermissionsByRoleId(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        return MoonCloudResponse.success(permissions);
    }

    @Operation(summary = "根据资源类型获取权限", description = "根据资源类型获取权限列表")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/resource/{resourceType}")
    public MoonCloudResponse<List<Permission>> getPermissionsByResourceType(
            @Parameter(description = "资源类型") @PathVariable String resourceType) {
        List<Permission> permissions = permissionService.getPermissionsByResourceType(resourceType);
        return MoonCloudResponse.success(permissions);
    }

    @Operation(summary = "根据URL获取权限", description = "根据资源URL获取权限信息")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/url")
    public MoonCloudResponse<Permission> getPermissionByUrl(@RequestParam String url) {
        Permission permission = permissionService.getPermissionByResourceTypeAndUrl("API", url);
        return MoonCloudResponse.success(permission);
    }

    @Operation(summary = "更新权限状态", description = "启用或禁用权限")
    @PreAuthorize("hasAuthority('permission:update')")
    @PutMapping("/{id}/status")
    public MoonCloudResponse<Void> updatePermissionStatus(
            @Parameter(description = "权限ID") @PathVariable Long id,
            @Parameter(description = "状态：1-启用，0-禁用") @RequestParam Integer status) {
        permissionService.updatePermissionStatus(id, status);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "批量更新权限状态", description = "批量启用或禁用权限")
    @PreAuthorize("hasAuthority('permission:update')")
    @PutMapping("/batch/status")
    public MoonCloudResponse<Void> updatePermissionsStatus(
            @RequestBody List<Long> ids,
            @Parameter(description = "状态：1-启用，0-禁用") @RequestParam Integer status) {
        permissionService.batchUpdatePermissionStatus(ids, status);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "检查权限编码是否存在", description = "检查权限编码是否已被使用")
    @GetMapping("/check/code")
    public MoonCloudResponse<Boolean> checkCodeExists(@RequestParam String code) {
        boolean exists = permissionService.isPermissionCodeExists(code, null);
        return MoonCloudResponse.success(exists);
    }

    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    @GetMapping("/check/user/{userId}")
    public MoonCloudResponse<Boolean> checkUserPermission(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestParam String permissionCode) {
        boolean hasPermission = permissionService.hasPermission(userId, permissionCode);
        return MoonCloudResponse.success(hasPermission);
    }

    @Operation(summary = "检查用户URL访问权限", description = "检查用户是否有权限访问指定URL")
    @GetMapping("/check/user/{userId}/url")
    public MoonCloudResponse<Boolean> checkUserUrlPermission(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestParam String url) {
        boolean hasPermission = permissionService.hasUrlPermission(userId, url);
        return MoonCloudResponse.success(hasPermission);
    }

    @Operation(summary = "统计权限数量", description = "统计权限总数量")
    @PreAuthorize("hasAuthority('permission:read')")
    @GetMapping("/count")
    public MoonCloudResponse<Long> countPermissions() {
        long count = permissionService.countPermissions(null);
        return MoonCloudResponse.success(count);
    }
}