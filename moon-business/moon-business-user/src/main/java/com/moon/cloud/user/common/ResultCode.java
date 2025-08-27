package com.moon.cloud.user.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 认证授权相关
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    TOKEN_INVALID(4001, "令牌无效"),
    TOKEN_EXPIRED(4002, "令牌已过期"),
    TOKEN_REFRESH_FAILED(4003, "令牌刷新失败"),
    LOGIN_FAILED(4004, "登录失败"),
    LOGOUT_FAILED(4005, "退出登录失败"),
    ACCOUNT_LOCKED(4006, "账户已锁定"),
    ACCOUNT_DISABLED(4007, "账户已禁用"),
    PASSWORD_ERROR(4008, "密码错误"),
    CAPTCHA_ERROR(4009, "验证码错误"),

    // 用户相关
    USER_NOT_FOUND(5001, "用户不存在"),
    USERNAME_EXISTS(5002, "用户名已存在"),
    EMAIL_EXISTS(5003, "邮箱已存在"),
    PHONE_EXISTS(5004, "手机号已存在"),
    OLD_PASSWORD_ERROR(5005, "原密码错误"),
    PASSWORD_CONFIRM_ERROR(5006, "确认密码不一致"),
    USER_STATUS_ERROR(5007, "用户状态异常"),

    // 角色相关
    ROLE_NOT_FOUND(6001, "角色不存在"),
    ROLE_CODE_EXISTS(6002, "角色编码已存在"),
    ROLE_HAS_USERS(6003, "角色下存在用户，无法删除"),
    ROLE_STATUS_ERROR(6004, "角色状态异常"),

    // 权限相关
    PERMISSION_NOT_FOUND(7001, "权限不存在"),
    PERMISSION_CODE_EXISTS(7002, "权限编码已存在"),
    PERMISSION_HAS_CHILDREN(7003, "权限下存在子权限，无法删除"),
    PERMISSION_HAS_ROLES(7004, "权限已分配给角色，无法删除"),
    PERMISSION_STATUS_ERROR(7005, "权限状态异常"),

    // 数据库相关
    DATABASE_ERROR(8001, "数据库操作失败"),
    DATA_NOT_FOUND(8002, "数据不存在"),
    DATA_EXISTS(8003, "数据已存在"),
    DATA_INTEGRITY_VIOLATION(8004, "数据完整性约束违反"),

    // 业务相关
    BUSINESS_ERROR(9001, "业务处理失败"),
    OPERATION_NOT_ALLOWED(9002, "操作不被允许"),
    RESOURCE_LOCKED(9003, "资源已被锁定"),
    CONCURRENT_UPDATE_ERROR(9004, "并发更新冲突");

    private final Integer code;
    private final String message;
}