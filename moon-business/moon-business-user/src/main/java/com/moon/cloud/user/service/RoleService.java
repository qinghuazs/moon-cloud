package com.moon.cloud.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 创建的角色
     */
    Role createRole(Role role);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 更新后的角色
     */
    Role updateRole(Role role);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否删除成功
     */
    boolean deleteRole(Long roleId);

    /**
     * 批量删除角色
     *
     * @param roleIds 角色ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteRoles(List<Long> roleIds);

    /**
     * 根据ID获取角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role getRoleById(Long roleId);

    /**
     * 根据角色编码获取角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    Role getRoleByCode(String roleCode);

    /**
     * 根据用户ID获取角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 根据角色ID获取角色（包含权限信息）
     *
     * @param roleId 角色ID
     * @return 角色信息（包含权限）
     */
    Role getRoleWithPermissions(Long roleId);

    /**
     * 分页查询角色列表
     *
     * @param page 分页参数
     * @param roleName 角色名称（模糊查询）
     * @param roleCode 角色编码（模糊查询）
     * @param status 状态
     * @return 角色分页列表
     */
    IPage<Role> getRolePage(Page<Role> page, String roleName, String roleCode, Integer status);

    /**
     * 获取所有启用的角色
     *
     * @return 启用的角色列表
     */
    List<Role> getEnabledRoles();

    /**
     * 根据权限ID获取角色列表
     *
     * @param permissionId 权限ID
     * @return 角色列表
     */
    List<Role> getRolesByPermissionId(Long permissionId);

    /**
     * 更新角色状态
     *
     * @param roleId 角色ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateRoleStatus(Long roleId, Integer status);

    /**
     * 批量更新角色状态
     *
     * @param roleIds 角色ID列表
     * @param status 状态
     * @return 是否更新成功
     */
    boolean batchUpdateRoleStatus(List<Long> roleIds, Integer status);

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否分配成功
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 移除角色的权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否移除成功
     */
    boolean removePermissionsFromRole(Long roleId, List<Long> permissionIds);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @param excludeRoleId 排除的角色ID
     * @return 是否存在
     */
    boolean isRoleCodeExists(String roleCode, Long excludeRoleId);

    /**
     * 统计角色数量
     *
     * @param status 状态（可选）
     * @return 角色数量
     */
    Long countRoles(Integer status);
}