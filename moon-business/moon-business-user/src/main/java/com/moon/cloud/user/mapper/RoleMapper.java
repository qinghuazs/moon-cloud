package com.moon.cloud.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND status = 1")
    Role selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询角色（包含权限信息）
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role selectRoleWithPermissionsById(@Param("roleId") Long roleId);

    /**
     * 分页查询角色列表（包含权限信息）
     *
     * @param page 分页参数
     * @param roleName 角色名称（模糊查询）
     * @param roleCode 角色编码（模糊查询）
     * @param status 状态
     * @return 角色分页列表
     */
    IPage<Role> selectRolePageWithPermissions(Page<Role> page,
                                             @Param("roleName") String roleName,
                                             @Param("roleCode") String roleCode,
                                             @Param("status") Integer status);

    /**
     * 查询所有启用的角色
     *
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE status = 1 ORDER BY created_at DESC")
    List<Role> selectEnabledRoles();

    /**
     * 根据权限ID查询角色列表
     *
     * @param permissionId 权限ID
     * @return 角色列表
     */
    List<Role> selectRolesByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 批量更新角色状态
     *
     * @param roleIds 角色ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("roleIds") List<Long> roleIds, @Param("status") Integer status);

    /**
     * 根据角色ID查询角色（包含权限信息）
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role selectRoleWithPermissions(@Param("roleId") Long roleId);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @param excludeId 排除的角色ID（用于更新时检查）
     * @return 是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_role WHERE role_code = #{roleCode}" +
            "<if test='excludeId != null'> AND id != #{excludeId}</if>" +
            "</script>")
    Long countByRoleCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

    /**
     * 检查角色编码是否存在（简化版本）
     *
     * @param roleCode 角色编码
     * @param excludeId 排除的角色ID（用于更新时检查）
     * @return 是否存在
     */
    default boolean existsByRoleCode(String roleCode, Long excludeId) {
        return countByRoleCode(roleCode, excludeId) > 0;
    }

    /**
     * 统计角色数量
     *
     * @param status 状态（可选）
     * @return 角色数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_role WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    Long countRoles(@Param("status") Integer status);
}