package com.moon.cloud.user.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.moon.cloud.user.dto.LoginResponse;
import com.moon.cloud.user.entity.User;
import com.moon.cloud.user.mapper.UserMapper;
import com.moon.cloud.user.service.AuthService;
import com.moon.cloud.user.service.GoogleOAuthService;
import com.moon.cloud.user.service.LoginLogService;
import com.moon.cloud.user.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Google OAuth 服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @Override
    public LoginResponse loginWithGoogle(String idToken, String ip, String userAgent) {
        // 验证Google ID Token
        GoogleUserInfo googleUserInfo = verifyGoogleToken(idToken);
        if (googleUserInfo == null) {
            throw new RuntimeException("Google ID Token验证失败");
        }

        // 创建或更新用户
        User user = createOrUpdateUserFromGoogle(googleUserInfo);

        // 检查用户状态
        if (user.getStatus() == User.STATUS_DISABLED) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成JWT令牌
        String token = authService.generateToken(user);
        String refreshToken = authService.generateRefreshToken(user);

        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());

        // 记录登录成功日志
        loginLogService.recordLoginLog(user.getId(), ip, userAgent, 1);

        // 缓存用户信息
        redisUtil.cacheUserInfo(user.getId(), user, 24, TimeUnit.HOURS);

        return new LoginResponse(token, refreshToken);
    }

    @Override
    public GoogleUserInfo verifyGoogleToken(String idToken) {
        try {
            if (verifier == null) {
                verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                        .setAudience(Collections.singletonList(googleClientId))
                        .build();
            }

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();

                String googleId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String picture = (String) payload.get("picture");
                Boolean emailVerified = payload.getEmailVerified();

                return new GoogleUserInfo(googleId, email, name, picture, emailVerified);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("验证Google ID Token时发生错误: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public User createOrUpdateUserFromGoogle(GoogleUserInfo googleUserInfo) {
        // 首先通过Google ID查找用户
        User existingUser = userMapper.selectByGoogleId(googleUserInfo.getGoogleId());

        if (existingUser != null) {
            // 更新现有用户信息
            existingUser.setEmail(googleUserInfo.getEmail());
            existingUser.setNickname(googleUserInfo.getName());
            existingUser.setAvatarUrl(googleUserInfo.getPicture());
            existingUser.setIsEmailVerified(googleUserInfo.getEmailVerified());
            existingUser.setUpdatedAt(LocalDateTime.now());

            userMapper.updateById(existingUser);
            return existingUser;
        }

        // 检查是否已存在相同邮箱的用户
        User userByEmail = userMapper.selectByEmail(googleUserInfo.getEmail());
        if (userByEmail != null) {
            // 如果存在相同邮箱的用户，将Google ID关联到该用户
            userByEmail.setGoogleId(googleUserInfo.getGoogleId());
            userByEmail.setProviderType(User.PROVIDER_GOOGLE);
            userByEmail.setNickname(googleUserInfo.getName());
            userByEmail.setAvatarUrl(googleUserInfo.getPicture());
            userByEmail.setIsEmailVerified(googleUserInfo.getEmailVerified());
            userByEmail.setUpdatedAt(LocalDateTime.now());

            userMapper.updateById(userByEmail);
            return userByEmail;
        }

        // 创建新用户
        User newUser = new User();
        newUser.setUsername(generateUniqueUsername(googleUserInfo.getEmail()));
        newUser.setEmail(googleUserInfo.getEmail());
        newUser.setNickname(googleUserInfo.getName());
        newUser.setAvatarUrl(googleUserInfo.getPicture());
        newUser.setGoogleId(googleUserInfo.getGoogleId());
        newUser.setProviderType(User.PROVIDER_GOOGLE);
        newUser.setIsEmailVerified(googleUserInfo.getEmailVerified());
        newUser.setStatus(User.STATUS_ENABLED);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(newUser);

        // 为新用户分配默认角色
        assignDefaultRole(newUser.getId());

        return newUser;
    }

    /**
     * 生成唯一的用户名
     */
    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (userMapper.selectByUsername(username) != null) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    /**
     * 为新用户分配默认角色
     */
    private void assignDefaultRole(Long userId) {
        // 这里可以根据业务需求分配默认角色
        // 例如：分配"普通用户"角色
        try {
            userMapper.assignDefaultRole(userId);
        } catch (Exception e) {
            // 如果分配角色失败，记录日志但不影响用户创建
            System.err.println("分配默认角色失败，用户ID: " + userId + ", 错误: " + e.getMessage());
        }
    }
}