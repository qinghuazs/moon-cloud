package com.moon.cloud.response.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 *
 * 状态码规范：
 * - 200-299：成功状态
 * - 400-499：客户端错误
 * - 500-599：服务端错误
 * - 1000-1999：认证授权相关
 * - 2000-2999：用户相关
 * - 3000-3999：角色权限相关
 * - 4000-4999：数据相关
 * - 5000-5999：业务相关
 * - 6000-6999：第三方服务相关
 * - 7000-7999：系统相关
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {

    // ========== 成功响应 ==========
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    ACCEPTED(202, "请求已接受"),
    NO_CONTENT(204, "无内容"),

    // ========== 客户端错误 ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    PAYMENT_REQUIRED(402, "需要付费"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    NOT_ACCEPTABLE(406, "请求格式不支持"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "资源冲突"),
    GONE(410, "资源已删除"),
    LENGTH_REQUIRED(411, "需要Content-Length"),
    PRECONDITION_FAILED(412, "前置条件失败"),
    PAYLOAD_TOO_LARGE(413, "请求体过大"),
    URI_TOO_LONG(414, "URI过长"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    RANGE_NOT_SATISFIABLE(416, "请求范围无法满足"),
    EXPECTATION_FAILED(417, "期望失败"),
    UNPROCESSABLE_ENTITY(422, "无法处理的实体"),
    LOCKED(423, "资源已锁定"),
    FAILED_DEPENDENCY(424, "依赖失败"),
    TOO_EARLY(425, "太早"),
    UPGRADE_REQUIRED(426, "需要升级"),
    PRECONDITION_REQUIRED(428, "需要前置条件"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "请求头过大"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "法律原因不可用"),

    // ========== 服务端错误 ==========
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    NOT_IMPLEMENTED(501, "功能未实现"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP版本不支持"),
    VARIANT_ALSO_NEGOTIATES(506, "变体协商"),
    INSUFFICIENT_STORAGE(507, "存储空间不足"),
    LOOP_DETECTED(508, "循环检测"),
    NOT_EXTENDED(510, "未扩展"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "需要网络认证"),

    // ========== 认证授权相关 1000-1999 ==========
    TOKEN_INVALID(1001, "令牌无效"),
    TOKEN_EXPIRED(1002, "令牌已过期"),
    TOKEN_NOT_FOUND(1003, "令牌不存在"),
    TOKEN_REFRESH_FAILED(1004, "令牌刷新失败"),
    LOGIN_FAILED(1010, "登录失败"),
    LOGIN_EXPIRED(1011, "登录已过期"),
    LOGOUT_FAILED(1012, "退出登录失败"),
    ACCOUNT_NOT_FOUND(1020, "账户不存在"),
    ACCOUNT_LOCKED(1021, "账户已锁定"),
    ACCOUNT_DISABLED(1022, "账户已禁用"),
    ACCOUNT_EXPIRED(1023, "账户已过期"),
    PASSWORD_ERROR(1030, "密码错误"),
    PASSWORD_EXPIRED(1031, "密码已过期"),
    PASSWORD_NEED_CHANGE(1032, "密码需要修改"),
    CAPTCHA_ERROR(1040, "验证码错误"),
    CAPTCHA_EXPIRED(1041, "验证码已过期"),
    CAPTCHA_FREQUENT(1042, "验证码请求过于频繁"),
    SMS_CODE_ERROR(1050, "短信验证码错误"),
    SMS_CODE_EXPIRED(1051, "短信验证码已过期"),

    // ========== 用户相关 2000-2999 ==========
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    USERNAME_EXISTS(2003, "用户名已存在"),
    EMAIL_EXISTS(2004, "邮箱已存在"),
    PHONE_EXISTS(2005, "手机号已存在"),
    USER_INFO_INCOMPLETE(2010, "用户信息不完整"),
    USER_STATUS_ERROR(2011, "用户状态异常"),
    OLD_PASSWORD_ERROR(2020, "原密码错误"),
    PASSWORD_CONFIRM_ERROR(2021, "确认密码不一致"),
    PASSWORD_FORMAT_ERROR(2022, "密码格式错误"),
    USER_PROFILE_UPDATE_FAILED(2030, "用户信息更新失败"),
    USER_AVATAR_UPLOAD_FAILED(2031, "头像上传失败"),

    // ========== 角色权限相关 3000-3999 ==========
    ROLE_NOT_FOUND(3001, "角色不存在"),
    ROLE_ALREADY_EXISTS(3002, "角色已存在"),
    ROLE_CODE_EXISTS(3003, "角色编码已存在"),
    ROLE_NAME_EXISTS(3004, "角色名称已存在"),
    ROLE_HAS_USERS(3005, "角色下存在用户，无法删除"),
    ROLE_STATUS_ERROR(3006, "角色状态异常"),
    ROLE_DEFAULT_CANNOT_DELETE(3007, "默认角色无法删除"),
    ROLE_DEFAULT_CANNOT_MODIFY(3008, "默认角色无法修改"),

    PERMISSION_NOT_FOUND(3101, "权限不存在"),
    PERMISSION_ALREADY_EXISTS(3102, "权限已存在"),
    PERMISSION_CODE_EXISTS(3103, "权限编码已存在"),
    PERMISSION_HAS_CHILDREN(3104, "权限下存在子权限，无法删除"),
    PERMISSION_HAS_ROLES(3105, "权限已分配给角色，无法删除"),
    PERMISSION_STATUS_ERROR(3106, "权限状态异常"),
    PERMISSION_PARENT_NOT_FOUND(3107, "父权限不存在"),
    PERMISSION_LEVEL_ERROR(3108, "权限层级错误"),

    // ========== 数据相关 4000-4999 ==========
    DATA_NOT_FOUND(4001, "数据不存在"),
    DATA_ALREADY_EXISTS(4002, "数据已存在"),
    DATA_INTEGRITY_VIOLATION(4003, "数据完整性约束违反"),
    DATA_VERSION_CONFLICT(4004, "数据版本冲突"),
    DATA_REFERENCE_ERROR(4005, "数据引用错误"),
    DATABASE_ERROR(4100, "数据库操作失败"),
    DATABASE_CONNECTION_ERROR(4101, "数据库连接失败"),
    DATABASE_TIMEOUT(4102, "数据库操作超时"),
    TRANSACTION_ERROR(4200, "事务处理失败"),
    TRANSACTION_ROLLBACK(4201, "事务回滚"),

    // ========== 业务相关 5000-5999 ==========
    BUSINESS_ERROR(5000, "业务处理失败"),
    OPERATION_NOT_ALLOWED(5001, "操作不被允许"),
    OPERATION_TOO_FREQUENT(5002, "操作过于频繁"),
    RESOURCE_LOCKED(5003, "资源已被锁定"),
    RESOURCE_EXPIRED(5004, "资源已过期"),
    CONCURRENT_UPDATE_ERROR(5005, "并发更新冲突"),
    WORKFLOW_ERROR(5100, "流程处理失败"),
    WORKFLOW_NOT_FOUND(5101, "流程不存在"),
    WORKFLOW_STATUS_ERROR(5102, "流程状态异常"),

    // ========== 文件相关 5200-5299 ==========
    FILE_NOT_FOUND(5201, "文件不存在"),
    FILE_UPLOAD_ERROR(5202, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(5203, "文件下载失败"),
    FILE_DELETE_ERROR(5204, "文件删除失败"),
    FILE_SIZE_EXCEED(5205, "文件大小超限"),
    FILE_TYPE_NOT_ALLOWED(5206, "文件类型不允许"),
    FILE_NAME_INVALID(5207, "文件名无效"),

    // ========== 第三方服务相关 6000-6999 ==========
    THIRD_PARTY_ERROR(6000, "第三方服务错误"),
    THIRD_PARTY_TIMEOUT(6001, "第三方服务超时"),
    THIRD_PARTY_UNAVAILABLE(6002, "第三方服务不可用"),
    API_CALL_ERROR(6100, "API调用失败"),
    API_RATE_LIMIT(6101, "API调用频率限制"),
    API_QUOTA_EXCEEDED(6102, "API配额超限"),
    PAYMENT_ERROR(6200, "支付失败"),
    PAYMENT_TIMEOUT(6201, "支付超时"),
    PAYMENT_CANCELLED(6202, "支付已取消"),
    SMS_SEND_ERROR(6300, "短信发送失败"),
    EMAIL_SEND_ERROR(6301, "邮件发送失败"),

    // ========== 系统相关 7000-7999 ==========
    SYSTEM_ERROR(7000, "系统错误"),
    SYSTEM_MAINTENANCE(7001, "系统维护中"),
    SYSTEM_BUSY(7002, "系统繁忙"),
    SYSTEM_RESOURCE_ERROR(7003, "系统资源异常"),
    CONFIG_ERROR(7100, "配置错误"),
    CONFIG_NOT_FOUND(7101, "配置不存在"),
    CACHE_ERROR(7200, "缓存错误"),
    CACHE_KEY_NOT_FOUND(7201, "缓存键不存在"),
    QUEUE_ERROR(7300, "队列错误"),
    QUEUE_FULL(7301, "队列已满"),

    // ========== 参数校验相关 8000-8999 ==========
    PARAM_ERROR(8000, "参数错误"),
    PARAM_MISSING(8001, "参数缺失"),
    PARAM_TYPE_ERROR(8002, "参数类型错误"),
    PARAM_FORMAT_ERROR(8003, "参数格式错误"),
    PARAM_VALUE_INVALID(8004, "参数值无效"),
    PARAM_LENGTH_ERROR(8005, "参数长度错误"),
    PARAM_RANGE_ERROR(8006, "参数范围错误"),

    // ========== 网络相关 9000-9999 ==========
    NETWORK_ERROR(9000, "网络错误"),
    NETWORK_TIMEOUT(9001, "网络超时"),
    NETWORK_UNREACHABLE(9002, "网络不可达"),
    CONNECTION_ERROR(9100, "连接错误"),
    CONNECTION_TIMEOUT(9101, "连接超时"),
    CONNECTION_REFUSED(9102, "连接被拒绝");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 判断是否成功状态码
     */
    public boolean isSuccess() {
        return this.code >= 200 && this.code < 300;
    }

    /**
     * 判断是否客户端错误
     */
    public boolean isClientError() {
        return this.code >= 400 && this.code < 500;
    }

    /**
     * 判断是否服务端错误
     */
    public boolean isServerError() {
        return this.code >= 500 && this.code < 600;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ResponseCode getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResponseCode responseCode : values()) {
            if (responseCode.getCode().equals(code)) {
                return responseCode;
            }
        }
        return null;
    }
}