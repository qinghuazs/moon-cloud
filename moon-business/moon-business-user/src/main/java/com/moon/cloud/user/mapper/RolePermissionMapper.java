package com.moon.cloud.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.user.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查询角色权限关联列表
     *
     * @param roleId 角色ID
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM sys_role_permission WHERE role_id = #{roleId}")
    List<RolePermission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查询角色权限关联列表
     *
     * @param permissionId 权限ID
     * @return 角色权限关联列表
     */
    @Select("SELECT * FROM sys_role_permission WHERE permission_id = #{permissionId}")
    List<RolePermission> selectByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID删除角色权限关联
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID删除角色权限关联
     *
     * @param permissionId 权限ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID和权限ID删除角色权限关联
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     *
     * @param rolePermissions 角色权限关联列表
     * @return 插入行数
     */
    int batchInsert(@Param("rolePermissions") List<RolePermission> rolePermissions);

    /**
     * 批量删除角色权限关联
     *
     * @param roleIds 角色ID列表
     * @return 删除行数
     */
    int batchDeleteByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 批量删除角色权限关联
     *
     * @param permissionIds 权限ID列表
     * @return 删除行数
     */
    int batchDeleteByPermissionIds(@Param("permissionIds") List<Long> permissionIds);

    /**
     * 检查角色是否拥有指定权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    Long countByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 统计权限被分配给多少个角色
     *
     * @param permissionId 权限ID
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE permission_id = #{permissionId}")
    Long countRolesByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 统计角色拥有的权限数量
     *
     * @param roleId 角色ID
     * @return 权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_role_permission WHERE role_id = #{roleId}")
    Long countPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID列表查询所有权限ID
     *
     * @param roleIds 角色ID列表
     * @return 权限ID列表
     */
    @Select("<script>" +
            "SELECT DISTINCT permission_id FROM sys_role_permission WHERE role_id IN" +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            "</script>")
    List<Long> selectPermissionIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}