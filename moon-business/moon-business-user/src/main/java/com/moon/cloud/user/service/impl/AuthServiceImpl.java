package com.moon.cloud.user.service.impl;

import com.moon.cloud.user.common.SpringContextUtil;
import com.moon.cloud.user.dto.LoginResponse;
import com.moon.cloud.user.dto.RegisterRequest;
import com.moon.cloud.user.entity.Permission;
import com.moon.cloud.user.entity.Role;
import com.moon.cloud.user.entity.User;
import com.moon.cloud.user.exception.AuthException;
import com.moon.cloud.user.exception.BusinessException;
import com.moon.cloud.user.mapper.PermissionMapper;
import com.moon.cloud.user.mapper.RoleMapper;
import com.moon.cloud.user.mapper.UserMapper;
import com.moon.cloud.user.service.AuthService;
import com.moon.cloud.user.service.LoginLogService;
import com.moon.cloud.user.util.JwtUtil;
import com.moon.cloud.user.util.RedisUtil;
import com.moon.cloud.user.service.EmailService;
import com.moon.cloud.captcha.service.CaptchaService;
import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.enums.CaptchaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
@Slf4j
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

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    @Override
    public LoginResponse login(String email, String password, String ip, String userAgent) {
        // 检查IP是否被锁定
        if (redisUtil.isIpLocked(ip)) {
            throw AuthException.ipLocked("IP地址已被锁定，请稍后再试");
        }

        // 检查登录失败次数
        Long failCount = redisUtil.getLoginFailureCount(email);
        if (failCount >= 5) {
            // 锁定IP 30分钟
            redisUtil.lockIp(ip, 30, TimeUnit.MINUTES);
            throw AuthException.tooManyFailures("登录失败次数过多，IP已被锁定30分钟");
        }

        try {
            // 查询用户
            User user = userMapper.selectUserWithRolesByEmail(email);
            if (user == null) {
                throw AuthException.invalidCredentials();
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                throw AuthException.userDisabled();
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                // 记录登录失败
                redisUtil.recordLoginFailure(email);
                loginLogService.recordLoginLog(user.getId(), ip, userAgent, 0); // 0表示失败
                throw AuthException.invalidCredentials();
            }

            // 生成JWT令牌
            String token = generateToken(user);
            String refreshToken = generateRefreshToken(user);

            // 更新最后登录时间
            userMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());

            // 清除登录失败次数
            redisUtil.clearLoginFailureCount(email);

            // 记录登录成功日志
            loginLogService.recordLoginLog(user.getId(), ip, userAgent, 1); // 1表示成功

            // 缓存用户信息
            redisUtil.cacheUserInfo(user.getId(), user, 24, TimeUnit.HOURS);

            return new LoginResponse(token, refreshToken);
        } catch (Exception e) {
            log.error("登录失败", e);
            // 记录登录失败
            redisUtil.recordLoginFailure(email);
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
            throw AuthException.invalidToken("无效的刷新令牌");
        }

        // 检查刷新令牌是否已被使用（在黑名单中）
        String refreshJti = jwtUtil.getJtiFromToken(refreshToken);
        if (refreshJti != null && redisUtil.isTokenBlacklisted(refreshJti)) {
            throw AuthException.invalidToken("刷新令牌已被使用或已失效");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 验证用户是否存在且状态正常
        User user = userMapper.selectUserWithRolesByUsername(username);
        if (user == null) {
            throw AuthException.invalidCredentials();
        }
        if (user.getStatus() == 0) {
            throw AuthException.userDisabled();
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

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest registerRequest, String ip, String userAgent) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
            throw AuthException.usernameExists();
        }

        // 检查邮箱是否已存在
        if (userMapper.selectByEmail(registerRequest.getEmail()) != null) {
            throw AuthException.emailExists();
        }

        // 检查手机号是否已存在（如果提供了手机号）
        if (registerRequest.getPhone() != null && !registerRequest.getPhone().trim().isEmpty()) {
            if (userMapper.selectByPhone(registerRequest.getPhone()) != null) {
                throw AuthException.phoneExists();
            }
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setNickname(registerRequest.getNickname() != null ? registerRequest.getNickname() : registerRequest.getUsername());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setProviderType(User.PROVIDER_LOCAL);
        newUser.setIsEmailVerified(false);
        newUser.setStatus(User.STATUS_ENABLED);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        // 保存用户
        userMapper.insert(newUser);

        // 为新用户分配默认角色
        try {
            userMapper.assignDefaultRole(newUser.getId());
        } catch (Exception e) {
            // 如果分配角色失败，记录日志但不影响注册
            log.error(String.format("分配默认角色失败，用户%s, 错误信息：", newUser.getId(), e));
        }

        // 生成JWT令牌
        String token = generateToken(newUser);
        String refreshToken = generateRefreshToken(newUser);

        // 记录登录成功日志
        loginLogService.recordLoginLog(newUser.getId(), ip, userAgent, 1);

        // 缓存用户信息
        redisUtil.cacheUserInfo(newUser.getId(), newUser, 24, TimeUnit.HOURS);

        return new LoginResponse(token, refreshToken);
    }

    @Override
    public boolean sendPasswordResetCode(String email) {
        try {
            // 检查邮箱是否被锁定
            if (captchaService.isLocked(email)) {
                log.warn("邮箱验证失败次数过多，已被锁定: {}", email);
                throw new RuntimeException("验证失败次数过多，请30分钟后重试");
            }

            // 查询用户是否存在
            User user = userMapper.selectByEmail(email);
            if (user == null) {
                log.warn("尝试重置不存在的邮箱密码: {}", email);
                // 为了安全，即使用户不存在也返回成功
                return true;
            }

            // 使用验证码服务生成6位数字验证码
            Captcha captcha = captchaService.generate(CaptchaType.DIGIT, 6);
            String code = captcha.getCode();

            // 使用验证码服务保存验证码，有效期10分钟
            captchaService.save(email, captcha, 10, TimeUnit.MINUTES);

            // 发送邮件（通过Kafka）
            emailService.sendVerificationCode(email, code);

            log.info("密码重置验证码已发送: email={}", email);
            return true;
        } catch (Exception e) {
            log.error("发送密码重置验证码失败: email={}", email, e);
            throw new BusinessException("发送验证码失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyPasswordResetCode(String email, String code) {
        try {
            // 检查邮箱是否被锁定
            if (captchaService.isLocked(email)) {
                log.warn("邮箱验证失败次数过多，已被锁定: {}", email);
                return false;
            }

            // 使用验证码服务验证
            boolean valid = captchaService.validate(email, code);
            if (!valid) {
                log.warn("验证码验证失败: email={}", email);
                // 检查失败次数
                int failCount = captchaService.getFailureCount(email);
                if (failCount >= 5) {
                    log.warn("验证码验证失败次数过多: email={}, count={}", email, failCount);
                }
                return false;
            }
            log.info("验证码验证成功: email={}", email);
            return true;
        } catch (Exception e) {
            log.error("验证密码重置验证码失败: email={}", email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(String email, String code, String newPassword) {
        try {
            // 先验证验证码
            if (!verifyPasswordResetCode(email, code)) {
                log.warn("重置密码时验证码验证失败: email={}", email);
                throw new RuntimeException("验证码错误或已过期");
            }

            // 查询用户
            User user = userMapper.selectByEmail(email);
            if (user == null) {
                log.error("重置密码时用户不存在: email={}", email);
                throw new RuntimeException("用户不存在");
            }

            // 更新密码
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPasswordHash(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());

            int updated = userMapper.updateById(user);
            if (updated <= 0) {
                log.error("更新用户密码失败: userId={}", user.getId());
                throw new RuntimeException("密码重置失败");
            }

            // 删除验证码
            captchaService.remove(email);

            // 清除用户缓存
            redisUtil.clearUserCache(user.getId());

            // 发送密码重置成功通知邮件
            try {
                emailService.sendPasswordResetNotification(email);
            } catch (Exception e) {
                // 发送通知邮件失败不影响密码重置
                log.error("发送密码重置成功通知邮件失败: email={}", email, e);
            }

            log.info("用户密码重置成功: userId={}, email={}", user.getId(), email);
            return true;
        } catch (Exception e) {
            log.error("重置密码失败: email={}", email, e);
            throw new RuntimeException(e.getMessage());
        }
    }

}