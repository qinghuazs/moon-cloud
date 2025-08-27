package com.moon.cloud.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface PermissionService {

    /**
     * 创建权限
     *
     * @param permission 权限信息
     * @return 创建的权限
     */
    Permission createPermission(Permission permission);

    /**
     * 更新权限
     *
     * @param permission 权限信息
     * @return 更新后的权限
     */
    Permission updatePermission(Permission permission);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 是否删除成功
     */
    boolean deletePermission(Long permissionId);

    /**
     * 批量删除权限
     *
     * @param permissionIds 权限ID列表
     * @return 是否删除成功
     */
    boolean batchDeletePermissions(List<Long> permissionIds);

    /**
     * 根据ID获取权限
     *
     * @param permissionId 权限ID
     * @return 权限信息
     */
    Permission getPermissionById(Long permissionId);

    /**
     * 根据权限编码获取权限
     *
     * @param permissionCode 权限编码
     * @return 权限信息
     */
    Permission getPermissionByCode(String permissionCode);

    /**
     * 根据用户ID获取权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);

    /**
     * 分页查询权限列表
     *
     * @param page 分页参数
     * @param permissionName 权限名称（模糊查询）
     * @param permissionCode 权限编码（模糊查询）
     * @param resourceType 资源类型
     * @param status 状态
     * @return 权限分页列表
     */
    IPage<Permission> getPermissionPage(Page<Permission> page, String permissionName, 
                                       String permissionCode, String resourceType, Integer status);

    /**
     * 获取所有启用的权限
     *
     * @return 启用的权限列表
     */
    List<Permission> getEnabledPermissions();

    /**
     * 根据资源类型获取权限列表
     *
     * @param resourceType 资源类型
     * @return 权限列表
     */
    List<Permission> getPermissionsByResourceType(String resourceType);

    /**
     * 根据资源类型和URL获取权限
     *
     * @param resourceType 资源类型
     * @param resourceUrl 资源URL
     * @return 权限信息
     */
    Permission getPermissionByResourceTypeAndUrl(String resourceType, String resourceUrl);

    /**
     * 更新权限状态
     *
     * @param permissionId 权限ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updatePermissionStatus(Long permissionId, Integer status);

    /**
     * 批量更新权限状态
     *
     * @param permissionIds 权限ID列表
     * @param status 状态
     * @return 是否更新成功
     */
    boolean batchUpdatePermissionStatus(List<Long> permissionIds, Integer status);

    /**
     * 检查权限编码是否存在
     *
     * @param permissionCode 权限编码
     * @param excludePermissionId 排除的权限ID
     * @return 是否存在
     */
    boolean isPermissionCodeExists(String permissionCode, Long excludePermissionId);

    /**
     * 统计权限数量
     *
     * @param status 状态（可选）
     * @return 权限数量
     */
    Long countPermissions(Integer status);

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否有访问指定URL的权限
     *
     * @param userId 用户ID
     * @param resourceUrl 资源URL
     * @return 是否有权限
     */
    boolean hasUrlPermission(Long userId, String resourceUrl);
}