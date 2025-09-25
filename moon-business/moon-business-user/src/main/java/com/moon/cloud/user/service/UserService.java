package com.moon.cloud.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.dto.UserProfileUpdateRequest;
import com.moon.cloud.user.dto.UserQueryRequest;
import com.moon.cloud.user.entity.User;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    User createUser(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 更新的用户
     */
    User updateUser(User user);

    /**
     * 根据ID删除用户
     *
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long userId);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteUsers(List<Long> userIds);

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 根据用户名查询用户（包含角色信息）
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User getUserByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);

    /**
     * 分页查询用户列表
     *
     * @param page 分页参数
     * @param username 用户名（模糊查询）
     * @param email 邮箱（模糊查询）
     * @param status 状态
     * @return 用户分页列表
     */
    IPage<User> getUserPage(Page<User> page, String username, String email, Integer status);

    /**
     * 查询所有启用的用户
     *
     * @return 用户列表
     */
    List<User> getEnabledUsers();

    /**
     * 根据角色ID查询用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<User> getUsersByRoleId(Long roleId);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status 状态
     * @return 是否更新成功
     */
    boolean batchUpdateUserStatus(List<Long> userIds, Integer status);

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 分配角色给用户
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否分配成功
     */
    boolean assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否移除成功
     */
    boolean removeRolesFromUser(Long userId, List<Long> roleIds);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeUserId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isUsernameExists(String username, Long excludeUserId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @param excludeUserId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isEmailExists(String email, Long excludeUserId);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @param excludeUserId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isPhoneExists(String phone, Long excludeUserId);

    /**
     * 统计用户数量
     *
     * @param status 状态（可选）
     * @return 用户数量
     */
    Long countUsers(Integer status);

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @return 是否更新成功
     */
    boolean updateLastLoginTime(Long userId);

    /**
     * 获取当前登录用户
     *
     * @param token JWT token
     * @return 用户信息
     */
    User getCurrentUser(String token);

    /**
     * 更新用户个人资料
     *
     * @param token JWT token
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    User updateUserProfile(String token, UserProfileUpdateRequest request);

    /**
     * 导出用户数据
     *
     * @param request 查询条件
     * @param response HTTP响应
     */
    void exportUsers(UserQueryRequest request, HttpServletResponse response);

    /**
     * 搜索用户
     *
     * @param page 分页参数
     * @param keyword 关键字
     * @return 用户分页列表
     */
    IPage<User> searchUsers(Page<User> page, String keyword);
}