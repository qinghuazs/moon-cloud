package com.moon.cloud.user.controller;

import com.moon.cloud.response.web.MoonCloudResponse;
import com.moon.cloud.user.dto.GoogleLoginRequest;
import com.moon.cloud.user.dto.LoginRequest;
import com.moon.cloud.user.dto.LoginResponse;
import com.moon.cloud.user.dto.RefreshTokenRequest;
import com.moon.cloud.user.dto.RegisterRequest;
import com.moon.cloud.user.dto.ForgotPasswordRequest;
import com.moon.cloud.user.dto.VerifyCodeRequest;
import com.moon.cloud.user.dto.ResetPasswordRequest;
import com.moon.cloud.user.entity.User;
import com.moon.cloud.user.service.AuthService;
import com.moon.cloud.user.service.GoogleOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;

/**
 * 认证控制器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Operation(summary = "用户登录", description = "用户登录获取访问令牌和刷新令牌")
    @PostMapping("/login")
    public MoonCloudResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        LoginResponse loginResponse = authService.login(
            loginRequest.getEmail(),
            loginRequest.getPassword(),
            ip,
            userAgent
        );
        
        return MoonCloudResponse.success(loginResponse);
    }

    @Operation(summary = "用户登出", description = "用户登出，令牌加入黑名单")
    @PostMapping("/logout")
    public MoonCloudResponse<Void> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            authService.logout(token);
        }
        return MoonCloudResponse.success();
    }

    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌和刷新令牌")
    @PostMapping("/refresh")
    public MoonCloudResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        LoginResponse loginResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        return MoonCloudResponse.success(loginResponse);
    }

    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效")
    @PostMapping("/validate")
    public MoonCloudResponse<Boolean> validateToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            return MoonCloudResponse.success(false);
        }
        
        boolean isValid = authService.validateToken(token);
        return MoonCloudResponse.success(isValid);
    }

    @Operation(summary = "获取当前用户信息", description = "根据令牌获取当前登录用户信息")
    @GetMapping("/me")
    public MoonCloudResponse<Object> getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            return MoonCloudResponse.error("未提供访问令牌");
        }
        
        Object user = authService.getUserFromToken(token);
        if (user == null) {
            return MoonCloudResponse.error("无效的访问令牌");
        }
        
        return MoonCloudResponse.success(user);
    }

    @Operation(summary = "用户注册", description = "用户注册并自动登录")
    @PostMapping("/register")
    public MoonCloudResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                   HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        LoginResponse loginResponse = authService.register(registerRequest, ip, userAgent);
        return MoonCloudResponse.success(loginResponse);
    }

    @Operation(summary = "Google登录", description = "使用Google OAuth进行登录")
    @PostMapping("/login/google")
    public MoonCloudResponse<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest googleLoginRequest,
                                                       HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        LoginResponse loginResponse = googleOAuthService.loginWithGoogle(
            googleLoginRequest.getIdToken(), ip, userAgent);
        return MoonCloudResponse.success(loginResponse);
    }

    @Operation(summary = "Token保活", description = "延长当前令牌的有效期")
    @PostMapping("/keepalive")
    public MoonCloudResponse<LoginResponse> keepAlive(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            return MoonCloudResponse.error("未提供访问令牌");
        }

        // 验证令牌是否有效
        if (!authService.validateToken(token)) {
            return MoonCloudResponse.error("无效的访问令牌");
        }

        // 获取用户信息
        User user = authService.getUserFromToken(token);
        if (user == null) {
            return MoonCloudResponse.error("用户不存在");
        }

        // 生成新的令牌
        String newToken = authService.generateToken(user);
        String newRefreshToken = authService.generateRefreshToken(user);

        // 将旧令牌加入黑名单
        authService.blacklistToken(token);

        return MoonCloudResponse.success(new LoginResponse(newToken, newRefreshToken));
    }

    @Operation(summary = "发送密码重置验证码", description = "向邮箱发送密码重置验证码")
    @PostMapping("/forgot-password")
    public MoonCloudResponse<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        boolean success = authService.sendPasswordResetCode(request.getEmail());
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "验证码已发送到您的邮箱，请查收");
            return MoonCloudResponse.success(response);
        } else {
            return MoonCloudResponse.error("发送验证码失败，请稍后重试");
        }
    }

    @Operation(summary = "验证密码重置验证码", description = "验证邮箱收到的验证码是否正确")
    @PostMapping("/verify-code")
    public MoonCloudResponse<Map<String, Boolean>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        boolean valid = authService.verifyPasswordResetCode(request.getEmail(), request.getCode());
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", valid);
        if (valid) {
            return MoonCloudResponse.success(response);
        } else {
            return MoonCloudResponse.error("验证码错误或已过期");
        }
    }

    @Operation(summary = "重置密码", description = "使用验证码重置密码")
    @PostMapping("/reset-password")
    public MoonCloudResponse<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            boolean success = authService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword()
            );
            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("message", "密码重置成功");
                return MoonCloudResponse.success(response);
            } else {
                return MoonCloudResponse.error("密码重置失败");
            }
        } catch (Exception e) {
            return MoonCloudResponse.error(e.getMessage());
        }
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}