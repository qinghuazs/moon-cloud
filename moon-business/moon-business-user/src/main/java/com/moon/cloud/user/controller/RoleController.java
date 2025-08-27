package com.moon.cloud.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.response.web.MoonCloudResponse;
import com.moon.cloud.user.dto.RoleCreateRequest;
import com.moon.cloud.user.dto.RoleUpdateRequest;
import com.moon.cloud.user.dto.RoleQueryRequest;
import com.moon.cloud.user.dto.RolePermissionRequest;
import com.moon.cloud.user.entity.Role;
import com.moon.cloud.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "创建角色", description = "创建新角色")
    @PreAuthorize("hasAuthority('role:create')")
    @PostMapping
    public MoonCloudResponse<Role> createRole(@Valid @RequestBody RoleCreateRequest request) {
        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        Role createdRole = roleService.createRole(role);
        return MoonCloudResponse.success(createdRole);
    }

    @Operation(summary = "更新角色", description = "根据ID更新角色信息")
    @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/{id}")
    public MoonCloudResponse<Role> updateRole(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        Role updatedRole = roleService.updateRole(role);
        return MoonCloudResponse.success(updatedRole);
    }

    @Operation(summary = "删除角色", description = "根据ID删除角色")
    @PreAuthorize("hasAuthority('role:delete')")
    @DeleteMapping("/{id}")
    public MoonCloudResponse<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.deleteRole(id);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "批量删除角色", description = "根据ID列表批量删除角色")
    @PreAuthorize("hasAuthority('role:delete')")
    @DeleteMapping("/batch")
    public MoonCloudResponse<Void> deleteRoles(@RequestBody List<Long> ids) {
        roleService.batchDeleteRoles(ids);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/{id}")
    public MoonCloudResponse<Role> getRoleById(@Parameter(description = "角色ID") @PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return MoonCloudResponse.success(role);
    }

    @Operation(summary = "根据编码获取角色", description = "根据角色编码获取角色信息")
    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/code/{code}")
    public MoonCloudResponse<Role> getRoleByCode(@Parameter(description = "角色编码") @PathVariable String code) {
        Role role = roleService.getRoleByCode(code);
        return MoonCloudResponse.success(role);
    }

    @Operation(summary = "分页查询角色", description = "根据条件分页查询角色列表")
    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping
    public MoonCloudResponse<IPage<Role>> getRoles(@Valid RoleQueryRequest request) {
        Page<Role> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<Role> roles = roleService.getRolePage(
            page,
            request.getRoleName(),
            request.getRoleCode(),
            request.getStatus()
        );
        return MoonCloudResponse.success(roles);
    }

    @Operation(summary = "获取所有启用角色", description = "获取所有状态为启用的角色")
    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/enabled")
    public MoonCloudResponse<List<Role>> getEnabledRoles() {
        List<Role> roles = roleService.getEnabledRoles();
        return MoonCloudResponse.success(roles);
    }

    @Operation(summary = "根据用户ID获取角色", description = "获取指定用户的所有角色")
    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/user/{userId}")
    public MoonCloudResponse<List<Role>> getRolesByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<Role> roles = roleService.getRolesByUserId(userId);
        return MoonCloudResponse.success(roles);
    }

    @Operation(summary = "根据权限ID获取角色", description = "获取拥有指定权限的所有角色")
    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/permission/{permissionId}")
    public MoonCloudResponse<List<Role>> getRolesByPermissionId(@Parameter(description = "权限ID") @PathVariable Long permissionId) {
        List<Role> roles = roleService.getRolesByPermissionId(permissionId);
        return MoonCloudResponse.success(roles);
    }

    @Operation(summary = "更新角色状态", description = "启用或禁用角色")
    @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/{id}/status")
    public MoonCloudResponse<Void> updateRoleStatus(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Parameter(description = "状态：1-启用，0-禁用") @RequestParam Integer status) {
        roleService.updateRoleStatus(id, status);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "批量更新角色状态", description = "批量启用或禁用角色")
    @PreAuthorize("hasAuthority('role:update')")
    @PutMapping("/batch/status")
    public MoonCloudResponse<Void> updateRolesStatus(
            @RequestBody List<Long> ids,
            @Parameter(description = "状态：1-启用，0-禁用") @RequestParam Integer status) {
        roleService.batchUpdateRoleStatus(ids, status);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "分配角色权限", description = "为角色分配权限")
    @PreAuthorize("hasAuthority('role:assign_permission')")
    @PostMapping("/{id}/permissions")
    public MoonCloudResponse<Void> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Valid @RequestBody RolePermissionRequest request) {
        roleService.assignPermissionsToRole(id, request.getPermissionIds());
        return MoonCloudResponse.success();
    }

    @Operation(summary = "移除角色权限", description = "移除角色的权限")
    @PreAuthorize("hasAuthority('role:remove_permission')")
    @DeleteMapping("/{id}/permissions")
    public MoonCloudResponse<Void> removePermissions(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Valid @RequestBody RolePermissionRequest request) {
        roleService.removePermissionsFromRole(id, request.getPermissionIds());
        return MoonCloudResponse.success();
    }

    @Operation(summary = "检查角色编码是否存在", description = "检查角色编码是否已被使用")
    @GetMapping("/check/code")
    public MoonCloudResponse<Boolean> checkCodeExists(@RequestParam String code) {
        boolean exists = roleService.isRoleCodeExists(code, null);
        return MoonCloudResponse.success(exists);
    }

    @Operation(summary = "统计角色数量", description = "统计角色总数量")
    @PreAuthorize("hasAuthority('role:read')")
    @GetMapping("/count")
    public MoonCloudResponse<Long> countRoles() {
        long count = roleService.countRoles(null);
        return MoonCloudResponse.success(count);
    }
}