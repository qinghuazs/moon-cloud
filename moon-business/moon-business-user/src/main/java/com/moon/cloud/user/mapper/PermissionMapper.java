package com.moon.cloud.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据权限编码查询权限
     *
     * @param permissionCode 权限编码
     * @return 权限信息
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND status = 1")
    Permission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID列表查询权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<Permission> selectPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);

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
    IPage<Permission> selectPermissionPage(Page<Permission> page,
                                          @Param("permissionName") String permissionName,
                                          @Param("permissionCode") String permissionCode,
                                          @Param("resourceType") String resourceType,
                                          @Param("status") Integer status);

    /**
     * 根据资源类型查询权限列表
     *
     * @param resourceType 资源类型
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE resource_type = #{resourceType} AND status = 1 ORDER BY created_at DESC")
    List<Permission> selectByResourceType(@Param("resourceType") String resourceType);

    /**
     * 查询所有启用的权限
     *
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE status = 1 ORDER BY resource_type, created_at DESC")
    List<Permission> selectEnabledPermissions();

    /**
     * 根据资源URL查询权限
     *
     * @param resourceUrl 资源URL
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE resource_url = #{resourceUrl} AND status = 1")
    List<Permission> selectByResourceUrl(@Param("resourceUrl") String resourceUrl);

    /**
     * 根据资源类型和URL查询权限
     *
     * @param resourceType 资源类型
     * @param resourceUrl 资源URL
     * @return 权限信息
     */
    @Select("SELECT * FROM sys_permission WHERE resource_type = #{resourceType} AND resource_url = #{resourceUrl} AND status = 1")
    Permission selectByResourceTypeAndUrl(@Param("resourceType") String resourceType, @Param("resourceUrl") String resourceUrl);

    /**
     * 批量更新权限状态
     *
     * @param permissionIds 权限ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("permissionIds") List<Long> permissionIds, @Param("status") Integer status);

    /**
     * 检查权限编码是否存在
     *
     * @param permissionCode 权限编码
     * @param excludeId 排除的权限ID（用于更新时检查）
     * @return 是否存在
     */
//    @Select("<script>" +
//            "SELECT COUNT(*) FROM sys_permission WHERE permission_code = #{permissionCode}" +
//            "<if test='excludeId != null'> AND id != #{excludeId}</if>" +
//            "</script>")
    Long countByPermissionCode(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);

    /**
     * 检查权限编码是否存在（简化版本）
     *
     * @param permissionCode 权限编码
     * @param excludeId 排除的权限ID（用于更新时检查）
     * @return 是否存在
     */
    default boolean existsByPermissionCode(String permissionCode, Long excludeId) {
        return countByPermissionCode(permissionCode, excludeId) > 0;
    }

    /**
     * 统计权限数量
     *
     * @param resourceType 资源类型（可选）
     * @param status 状态（可选）
     * @return 权限数量
     */
//    @Select("<script>" +
//            "SELECT COUNT(*) FROM sys_permission WHERE 1=1" +
//            "<if test='resourceType != null and resourceType != \'\''> AND resource_type = #{resourceType}</if>" +
//            "<if test='status != null'> AND status = #{status}</if>" +
//            "</script>")
    Long countPermissions(@Param("resourceType") String resourceType, @Param("status") Integer status);
}