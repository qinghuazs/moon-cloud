package com.moon.cloud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.Permission;
import com.moon.cloud.user.mapper.PermissionMapper;
import com.moon.cloud.user.mapper.RolePermissionMapper;
import com.moon.cloud.user.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 权限服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission createPermission(Permission permission) {
        // 检查权限编码是否存在
        if (isPermissionCodeExists(permission.getPermissionCode(), null)) {
            throw new RuntimeException("权限编码已存在");
        }
        
        // 设置默认值
        if (permission.getStatus() == null) {
            permission.setStatus(Permission.STATUS_ENABLED);
        }
        
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission updatePermission(Permission permission) {
        Permission existingPermission = permissionMapper.selectById(permission.getId());
        if (existingPermission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 检查权限编码是否存在
        if (!existingPermission.getPermissionCode().equals(permission.getPermissionCode()) && 
            isPermissionCodeExists(permission.getPermissionCode(), permission.getId())) {
            throw new RuntimeException("权限编码已存在");
        }
        
        permissionMapper.updateById(permission);
        return permissionMapper.selectById(permission.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long permissionId) {
        // 删除角色权限关联
        rolePermissionMapper.deleteByPermissionId(permissionId);
        
        // 删除权限
        return permissionMapper.deleteById(permissionId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeletePermissions(List<Long> permissionIds) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return false;
        }
        
        // 删除角色权限关联
        rolePermissionMapper.batchDeleteByPermissionIds(permissionIds);
        
        // 删除权限
        return permissionMapper.deleteBatchIds(permissionIds) > 0;
    }

    @Override
    public Permission getPermissionById(Long permissionId) {
        return permissionMapper.selectById(permissionId);
    }

    @Override
    public Permission getPermissionByCode(String permissionCode) {
        return permissionMapper.selectByPermissionCode(permissionCode);
    }

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        return permissionMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    public IPage<Permission> getPermissionPage(Page<Permission> page, String permissionName, 
                                              String permissionCode, String resourceType, Integer status) {
        return permissionMapper.selectPermissionPage(page, permissionName, permissionCode, resourceType, status);
    }

    @Override
    public List<Permission> getEnabledPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, Permission.STATUS_ENABLED)
               .orderByAsc(Permission::getResourceType)
               .orderByDesc(Permission::getCreatedAt);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public List<Permission> getPermissionsByResourceType(String resourceType) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getResourceType, resourceType)
               .eq(Permission::getStatus, Permission.STATUS_ENABLED)
               .orderByDesc(Permission::getCreatedAt);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public Permission getPermissionByResourceTypeAndUrl(String resourceType, String resourceUrl) {
        return permissionMapper.selectByResourceTypeAndUrl(resourceType, resourceUrl);
    }

    @Override
    public boolean updatePermissionStatus(Long permissionId, Integer status) {
        Permission permission = new Permission();
        permission.setId(permissionId);
        permission.setStatus(status);
        return permissionMapper.updateById(permission) > 0;
    }

    @Override
    public boolean batchUpdatePermissionStatus(List<Long> permissionIds, Integer status) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return false;
        }
        return permissionMapper.batchUpdateStatus(permissionIds, status) > 0;
    }

    @Override
    public boolean isPermissionCodeExists(String permissionCode, Long excludePermissionId) {
        return permissionMapper.existsByPermissionCode(permissionCode, excludePermissionId);
    }

    @Override
    public Long countPermissions(Integer status) {
        return permissionMapper.countPermissions(null, status);
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || !StringUtils.hasText(permissionCode)) {
            return false;
        }
        
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        return permissions.stream()
                .anyMatch(permission -> permissionCode.equals(permission.getPermissionCode()));
    }

    @Override
    public boolean hasUrlPermission(Long userId, String resourceUrl) {
        if (userId == null || !StringUtils.hasText(resourceUrl)) {
            return false;
        }
        
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        return permissions.stream()
                .anyMatch(permission -> Permission.RESOURCE_TYPE_API.equals(permission.getResourceType()) &&
                                      resourceUrl.matches(permission.getResourceUrl()));
    }
}