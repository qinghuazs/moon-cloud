package com.moon.cloud.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.response.web.MoonCloudResponse;
import com.moon.cloud.user.dto.*;
import com.moon.cloud.user.entity.User;
import com.moon.cloud.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "创建用户", description = "创建新用户")
    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping
    public MoonCloudResponse<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        // 将 UserCreateRequest 转换为 User 对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatar());
        user.setStatus(request.getStatus());
        User createdUser = userService.createUser(user);
        return MoonCloudResponse.success(createdUser);
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{id}")
    public MoonCloudResponse<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        // 将 UserUpdateRequest 转换为 User 对象
        User user = new User();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatar());
        user.setStatus(request.getStatus());
        User updatedUser = userService.updateUser(user);
        return MoonCloudResponse.success(updatedUser);
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/{id}")
    public MoonCloudResponse<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "批量删除用户", description = "根据ID列表批量删除用户")
    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/batch")
    public MoonCloudResponse<Void> deleteUsers(@RequestBody List<Long> ids) {
        userService.batchDeleteUsers(ids);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{id}")
    public MoonCloudResponse<User> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return MoonCloudResponse.success(user);
    }

    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表")
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping
    public MoonCloudResponse<IPage<User>> getUsers(@Valid UserQueryRequest request) {
        Page<User> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<User> users = userService.getUserPage(
            page,
            request.getUsername(),
            request.getEmail(),
            request.getStatus()
        );
        return MoonCloudResponse.success(users);
    }

    @Operation(summary = "获取所有启用用户", description = "获取所有状态为启用的用户")
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/enabled")
    public MoonCloudResponse<List<User>> getEnabledUsers() {
        List<User> users = userService.getEnabledUsers();
        return MoonCloudResponse.success(users);
    }

    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{id}/status")
    public MoonCloudResponse<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态：1-启用，0-禁用") @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return MoonCloudResponse.success();
    }

    @Operation(summary = "批量更新用户状态", description = "批量更新用户状态")
    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/status/batch")
    public MoonCloudResponse<Void> updateUsersStatus(
            @RequestBody StatusUpdateRequest request) {
        userService.batchUpdateUserStatus(request.getIds(), request.getStatus());
        return MoonCloudResponse.success();
    }

    @Operation(summary = "重置用户密码", description = "重置用户密码为默认密码")
    @PreAuthorize("hasAuthority('user:reset-password')")
    @PutMapping("/{id}/reset-password")
    public MoonCloudResponse<String> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id) {
        // 生成默认密码
        String defaultPassword = "123456";
        boolean success = userService.resetPassword(id, defaultPassword);
        if (success) {
            return MoonCloudResponse.success(defaultPassword);
        } else {
            return MoonCloudResponse.error("重置密码失败");
        }
    }

    @Operation(summary = "修改用户密码", description = "用户修改自己的密码")
    @PutMapping("/{id}/password")
    public MoonCloudResponse<Void> changePassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return MoonCloudResponse.success();
    }

    @Operation(summary = "分配角色", description = "为用户分配角色")
    @PreAuthorize("hasAuthority('user:assign-role')")
    @PostMapping("/{id}/roles")
    public MoonCloudResponse<Void> assignRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody UserRoleRequest request) {
        userService.assignRolesToUser(id, request.getRoleIds());
        return MoonCloudResponse.success();
    }

    @Operation(summary = "移除角色", description = "移除用户的角色")
    @PreAuthorize("hasAuthority('user:remove-role')")
    @DeleteMapping("/{id}/roles")
    public MoonCloudResponse<Void> removeRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody UserRoleRequest request) {
        userService.removeRolesFromUser(id, request.getRoleIds());
        return MoonCloudResponse.success();
    }

    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被使用")
    @GetMapping("/check/username")
    public MoonCloudResponse<Boolean> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.isUsernameExists(username, null);
        return MoonCloudResponse.success(exists);
    }

    @Operation(summary = "检查邮箱是否存在", description = "检查邮箱是否已被使用")
    @GetMapping("/check/email")
    public MoonCloudResponse<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email, null);
        return MoonCloudResponse.success(exists);
    }

    @Operation(summary = "检查手机号是否存在", description = "检查手机号是否已被使用")
    @GetMapping("/check/phone")
    public MoonCloudResponse<Boolean> checkPhoneExists(@RequestParam String phone) {
        boolean exists = userService.isPhoneExists(phone, null);
        return MoonCloudResponse.success(exists);
    }

    @Operation(summary = "统计用户数量", description = "统计用户总数量")
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/count")
    public MoonCloudResponse<Long> countUsers() {
        long count = userService.countUsers(null);
        return MoonCloudResponse.success(count);
    }
}