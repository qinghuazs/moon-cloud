package com.moon.cloud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.Role;
import com.moon.cloud.user.entity.RolePermission;
import com.moon.cloud.user.mapper.RoleMapper;
import com.moon.cloud.user.mapper.RolePermissionMapper;
import com.moon.cloud.user.mapper.UserRoleMapper;
import com.moon.cloud.user.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role createRole(Role role) {
        // 检查角色编码是否存在
        if (isRoleCodeExists(role.getRoleCode(), null)) {
            throw new RuntimeException("角色编码已存在");
        }
        
        // 设置默认值
        if (role.getStatus() == null) {
            role.setStatus(Role.STATUS_ENABLED);
        }
        
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role updateRole(Role role) {
        Role existingRole = roleMapper.selectById(role.getId());
        if (existingRole == null) {
            throw new RuntimeException("角色不存在");
        }
        
        // 检查角色编码是否存在
        if (!existingRole.getRoleCode().equals(role.getRoleCode()) && 
            isRoleCodeExists(role.getRoleCode(), role.getId())) {
            throw new RuntimeException("角色编码已存在");
        }
        
        roleMapper.updateById(role);
        return roleMapper.selectById(role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 删除用户角色关联
        userRoleMapper.deleteByRoleId(roleId);
        
        // 删除角色
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteRoles(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }
        
        // 删除角色权限关联
        rolePermissionMapper.batchDeleteByRoleIds(roleIds);
        
        // 删除用户角色关联
        userRoleMapper.batchDeleteByRoleIds(roleIds);
        
        // 删除角色
        return roleMapper.deleteBatchIds(roleIds) > 0;
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public Role getRoleByCode(String roleCode) {
        return roleMapper.selectByRoleCode(roleCode);
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public Role getRoleWithPermissions(Long roleId) {
        return roleMapper.selectRoleWithPermissions(roleId);
    }

    @Override
    public IPage<Role> getRolePage(Page<Role> page, String roleName, String roleCode, Integer status) {
        return roleMapper.selectRolePageWithPermissions(page, roleName, roleCode, status);
    }

    @Override
    public List<Role> getEnabledRoles() {
        return roleMapper.selectEnabledRoles();
    }

    @Override
    public List<Role> getRolesByPermissionId(Long permissionId) {
        return roleMapper.selectRolesByPermissionId(permissionId);
    }

    @Override
    public boolean updateRoleStatus(Long roleId, Integer status) {
        Role role = new Role();
        role.setId(roleId);
        role.setStatus(status);
        return roleMapper.updateById(role) > 0;
    }

    @Override
    public boolean batchUpdateRoleStatus(List<Long> roleIds, Integer status) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }
        return roleMapper.batchUpdateStatus(roleIds, status) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return false;
        }
        
        // 删除现有权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 添加新的权限关联
        List<RolePermission> rolePermissions = permissionIds.stream()
                .map(permissionId -> new RolePermission(roleId, permissionId))
                .collect(Collectors.toList());
        
        return rolePermissionMapper.batchInsert(rolePermissions) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return false;
        }
        
        for (Long permissionId : permissionIds) {
            rolePermissionMapper.deleteByRoleIdAndPermissionId(roleId, permissionId);
        }
        
        return true;
    }

    @Override
    public boolean isRoleCodeExists(String roleCode, Long excludeRoleId) {
        return roleMapper.existsByRoleCode(roleCode, excludeRoleId);
    }

    @Override
    public Long countRoles(Integer status) {
        return roleMapper.countRoles(status);
    }
}