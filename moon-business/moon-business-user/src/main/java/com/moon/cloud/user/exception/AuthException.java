package com.moon.cloud.user.exception;

import com.moon.cloud.user.common.ResultCode;

/**
 * 认证异常类
 * 用于处理登录、注册、令牌等认证相关异常
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public class AuthException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(ResultCode.LOGIN_FAILED, message);
    }

    public AuthException(ResultCode resultCode) {
        super(resultCode);
    }

    public AuthException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public AuthException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }

    public AuthException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }

    // 静态方法，用于快速创建特定类型的认证异常

    /**
     * IP被锁定异常
     */
    public static AuthException ipLocked(String message) {
        return new AuthException(ResultCode.RESOURCE_LOCKED, message);
    }

    /**
     * 登录失败次数过多异常
     */
    public static AuthException tooManyFailures(String message) {
        return new AuthException(ResultCode.TOO_MANY_REQUESTS, message);
    }

    /**
     * 用户不存在或密码错误
     */
    public static AuthException invalidCredentials() {
        return new AuthException(ResultCode.LOGIN_FAILED, "用户名或密码错误");
    }

    /**
     * 用户被禁用异常
     */
    public static AuthException userDisabled() {
        return new AuthException(ResultCode.ACCOUNT_DISABLED, "用户已被禁用");
    }

    /**
     * 令牌无效异常
     */
    public static AuthException invalidToken(String message) {
        return new AuthException(ResultCode.TOKEN_INVALID, message);
    }

    /**
     * 用户名已存在异常
     */
    public static AuthException usernameExists() {
        return new AuthException(ResultCode.USERNAME_EXISTS, "用户名已存在");
    }

    /**
     * 邮箱已存在异常
     */
    public static AuthException emailExists() {
        return new AuthException(ResultCode.EMAIL_EXISTS, "邮箱已被注册");
    }

    /**
     * 手机号已存在异常
     */
    public static AuthException phoneExists() {
        return new AuthException(ResultCode.PHONE_EXISTS, "手机号已被注册");
    }
}