package com.moon.cloud.user.service.impl;

import com.moon.cloud.user.dto.LoginResponse;
import com.moon.cloud.user.entity.Permission;
import com.moon.cloud.user.entity.Role;
import com.moon.cloud.user.entity.User;
import com.moon.cloud.user.mapper.PermissionMapper;
import com.moon.cloud.user.mapper.RoleMapper;
import com.moon.cloud.user.mapper.UserMapper;
import com.moon.cloud.user.service.AuthService;
import com.moon.cloud.user.service.LoginLogService;
import com.moon.cloud.user.util.JwtUtil;
import com.moon.cloud.user.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证授权服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginLogService loginLogService;

    @Override
    public LoginResponse login(String username, String password, String ip, String userAgent) {
        // 检查IP是否被锁定
        if (redisUtil.isIpLocked(ip)) {
            throw new RuntimeException("IP地址已被锁定，请稍后再试");
        }

        // 检查登录失败次数
        Long failCount = redisUtil.getLoginFailureCount(username);
        if (failCount >= 5) {
            // 锁定IP 30分钟
            redisUtil.lockIp(ip, 30, TimeUnit.MINUTES);
            throw new RuntimeException("登录失败次数过多，IP已被锁定30分钟");
        }

        try {
            // 查询用户
            User user = userMapper.selectUserWithRolesByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                throw new RuntimeException("用户已被禁用");
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                // 记录登录失败
                redisUtil.recordLoginFailure(username);
                loginLogService.recordLoginLog(user.getId(), ip, userAgent, 0); // 0表示失败
                throw new RuntimeException("用户名或密码错误");
            }

            // 生成JWT令牌
            String token = generateToken(user);
            String refreshToken = generateRefreshToken(user);

            // 更新最后登录时间
            userMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());

            // 清除登录失败次数
            redisUtil.clearLoginFailureCount(username);

            // 记录登录成功日志
            loginLogService.recordLoginLog(user.getId(), ip, userAgent, 1); // 1表示成功

            // 缓存用户信息
            redisUtil.cacheUserInfo(user.getId(), user, 24, TimeUnit.HOURS);

            return new LoginResponse(token, refreshToken);
        } catch (Exception e) {
            // 记录登录失败
            redisUtil.recordLoginFailure(username);
            throw e;
        }
    }

    @Override
    public boolean logout(String token) {
        if (StringUtils.hasText(token)) {
            try {
                // 获取令牌剩余时间
                Long remainingTime = jwtUtil.getTokenRemainingTime(token);
                if (remainingTime > 0) {
                    // 获取JTI
                    String jti = jwtUtil.getJtiFromToken(token);
                    if (jti != null) {
                        // 将令牌加入黑名单
                        redisUtil.addToBlacklist(jti, remainingTime);
                    }
                }

                // 清除用户缓存
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    redisUtil.clearUserCache(userId);
                }
                return true;
            } catch (Exception e) {
                // 忽略令牌解析错误
                return false;
            }
        }
        return false;
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        // 检查刷新令牌是否已被使用（在黑名单中）
        String refreshJti = jwtUtil.getJtiFromToken(refreshToken);
        if (refreshJti != null && redisUtil.isTokenBlacklisted(refreshJti)) {
            throw new RuntimeException("刷新令牌已被使用或已失效");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 验证用户是否存在且状态正常
        User user = userMapper.selectUserWithRolesByUsername(username);
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 将旧的刷新令牌加入黑名单
        if (refreshJti != null) {
            Long remainingTime = jwtUtil.getTokenRemainingTime(refreshToken);
            if (remainingTime > 0) {
                redisUtil.addToBlacklist(refreshJti, remainingTime);
            }
        }

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = generateToken(user);
        String newRefreshToken = generateRefreshToken(user);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                return false;
            }

            // 检查是否在黑名单中
            String jti = jwtUtil.getJtiFromToken(token);
            if (jti != null && redisUtil.isTokenBlacklisted(jti)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public User getUserFromToken(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return null;
            }

            // 先从缓存获取
            Object cachedUser = redisUtil.getCachedUserInfo(userId);
            if (cachedUser instanceof User) {
                return (User) cachedUser;
            }

            // 从数据库获取
            User user = userMapper.selectById(userId);
            if (user != null) {
                // 缓存用户信息
                redisUtil.cacheUserInfo(userId, user, 24, TimeUnit.HOURS);
            }

            return user;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        try {
            // 先从缓存获取权限
            Object cachedPermissions = redisUtil.getCachedUserPermissions(userId);
            if (cachedPermissions instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> permissions = (List<String>) cachedPermissions;
                return permissions.contains(permissionCode);
            }

            // 从数据库获取权限
            List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
            List<String> permissionCodes = permissions.stream()
                    .map(Permission::getPermissionCode)
                    .collect(Collectors.toList());

            // 缓存权限信息
            redisUtil.cacheUserPermissions(userId, permissionCodes, 1, TimeUnit.HOURS);

            return permissionCodes.contains(permissionCode);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        try {
            // 先从缓存获取角色
            Object cachedRoles = redisUtil.getCachedUserRoles(userId);
            if (cachedRoles instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) cachedRoles;
                return roles.contains(roleCode);
            }

            // 从数据库获取角色
            List<Role> roles = roleMapper.selectRolesByUserId(userId);
            List<String> roleCodes = roles.stream()
                    .map(Role::getRoleCode)
                    .collect(Collectors.toList());

            // 缓存角色信息
            redisUtil.cacheUserRoles(userId, roleCodes, 1, TimeUnit.HOURS);

            return roleCodes.contains(roleCode);
        } catch (Exception e) {
            return false;
        }
    }

    // 辅助方法：检查用户是否有指定URL权限
    public boolean hasUrlPermission(Long userId, String url) {
        try {
            List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
            return permissions.stream()
                    .anyMatch(permission -> url.matches(permission.getResourceUrl()));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateToken(User user) {
        String jti = UUID.randomUUID().toString();
        return jwtUtil.generateTokenWithJti(user.getId(), user.getUsername(), jti);
    }

    @Override
    public String generateRefreshToken(User user) {
        return jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
    }

    @Override
    public boolean canAccessUrl(Long userId, String requestUrl, String httpMethod) {
        try {
            List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
            return permissions.stream()
                    .anyMatch(permission -> requestUrl.matches(permission.getResourceUrl()));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean blacklistToken(String token) {
        try {
            Long remainingTime = jwtUtil.getTokenRemainingTime(token);
            if (remainingTime > 0) {
                String jti = jwtUtil.getJtiFromToken(token);
                if (jti != null) {
                    redisUtil.addToBlacklist(jti, remainingTime);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            String jti = jwtUtil.getJtiFromToken(token);
            return jti != null && redisUtil.isTokenBlacklisted(jti);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int cleanExpiredBlacklistTokens() {
        // 获取所有黑名单令牌
        Set<String> keys = redisUtil.keys("jwt:blacklist:*");
        int cleanedCount = 0;
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Long expire = redisUtil.getExpire(key);
                if (expire != null && expire <= 0) {
                    redisUtil.delete(key);
                    cleanedCount++;
                }
            }
        }
        return cleanedCount;
    }

    @Override
    public User loadUserByUsername(String username) {
        return userMapper.selectUserWithRolesByUsername(username);
    }

    public UserDetails loadUserByUsernameForSecurity(String username) throws UsernameNotFoundException {
        User user = userMapper.selectUserWithRolesByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        // 获取用户权限
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());
        List<GrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermissionCode()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getStatus() == 0)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0)
                .build();
    }
}