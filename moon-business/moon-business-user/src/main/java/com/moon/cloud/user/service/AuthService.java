package com.moon.cloud.user.service;

import com.moon.cloud.user.dto.LoginResponse;
import com.moon.cloud.user.dto.RegisterRequest;
import com.moon.cloud.user.entity.User;

/**
 * 认证授权服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 登录响应对象，包含访问令牌和刷新令牌
     */
    LoginResponse login(String username, String password, String ipAddress, String userAgent);

    /**
     * 用户登出
     *
     * @param token JWT令牌
     * @return 是否登出成功
     */
    boolean logout(String token);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 登录响应对象，包含新的访问令牌和刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 验证令牌
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 从令牌中获取用户信息
     *
     * @param token JWT令牌
     * @return 用户信息
     */
    User getUserFromToken(String token);

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 检查用户是否可以访问指定URL
     *
     * @param userId 用户ID
     * @param requestUrl 请求URL
     * @param httpMethod HTTP方法
     * @return 是否可以访问
     */
    boolean canAccessUrl(Long userId, String requestUrl, String httpMethod);

    /**
     * 生成JWT令牌
     *
     * @param user 用户信息
     * @return JWT令牌
     */
    String generateToken(User user);

    /**
     * 生成刷新令牌
     *
     * @param user 用户信息
     * @return 刷新令牌
     */
    String generateRefreshToken(User user);

    /**
     * 将令牌加入黑名单
     *
     * @param token JWT令牌
     * @return 是否成功
     */
    boolean blacklistToken(String token);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);

    /**
     * 清理过期的黑名单令牌
     *
     * @return 清理的数量
     */
    int cleanExpiredBlacklistTokens();

    /**
     * 根据用户名加载用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User loadUserByUsername(String username);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @param ip 客户端IP
     * @param userAgent 用户代理
     * @return 登录响应对象，包含访问令牌和刷新令牌
     */
    LoginResponse register(RegisterRequest registerRequest, String ip, String userAgent);

    /**
     * 发送密码重置验证码
     *
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendPasswordResetCode(String email);

    /**
     * 验证重置密码验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyPasswordResetCode(String email, String code);

    /**
     * 重置密码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    boolean resetPassword(String email, String code, String newPassword);
}