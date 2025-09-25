package com.moon.cloud.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（包含角色信息）
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectUserWithRolesByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND status = 1")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND status = 1")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 分页查询用户列表（包含角色信息）
     *
     * @param page 分页参数
     * @param username 用户名（模糊查询）
     * @param email 邮箱（模糊查询）
     * @param status 状态
     * @return 用户分页列表
     */
    IPage<User> selectUserPageWithRoles(Page<User> page, 
                                       @Param("username") String username,
                                       @Param("email") String email,
                                       @Param("status") Integer status);

    /**
     * 根据角色ID查询用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<User> selectUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     * @return 更新行数
     */
    @Update("UPDATE sys_user SET last_login_time = #{lastLoginTime} WHERE id = #{userId}")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);

    /**
     * 统计用户数量
     *
     * @param status 状态（可选）
     * @return 用户数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_user WHERE 1=1" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    Long countUsers(@Param("status") Integer status);

    /**
     * 根据Google ID查询用户
     *
     * @param googleId Google用户ID
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE google_id = #{googleId}")
    User selectByGoogleId(@Param("googleId") String googleId);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 为用户分配默认角色
     *
     * @param userId 用户ID
     */
    void assignDefaultRole(@Param("userId") Long userId);

    /**
     * 搜索用户
     *
     * @param page 分页参数
     * @param keyword 关键字
     * @return 用户列表
     */
    IPage<User> searchUsers(Page<User> page, @Param("keyword") String keyword);
}