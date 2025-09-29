package com.moon.cloud.response.handler;

import com.moon.cloud.response.enums.ResponseCode;
import lombok.Getter;

/**
 * 业务异常类
 * 用于在业务处理过程中抛出自定义异常
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 响应码枚举
     */
    private ResponseCode responseCode;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
        this.message = message;
        this.responseCode = ResponseCode.BUSINESS_ERROR;
    }

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码枚举
     */
    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.responseCode = responseCode;
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码枚举
     * @param message      自定义错误消息
     */
    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
        this.message = message;
        this.responseCode = responseCode;
    }

    /**
     * 构造函数（带原因）
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
        this.message = message;
        this.responseCode = ResponseCode.BUSINESS_ERROR;
    }

    /**
     * 构造函数（带原因）
     *
     * @param responseCode 响应码枚举
     * @param cause        异常原因
     */
    public BusinessException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.responseCode = responseCode;
    }

    /**
     * 构造函数（带原因）
     *
     * @param responseCode 响应码枚举
     * @param message      自定义错误消息
     * @param cause        异常原因
     */
    public BusinessException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.code = responseCode.getCode();
        this.message = message;
        this.responseCode = responseCode;
    }

    /**
     * 抛出业务异常
     *
     * @param message 错误消息
     */
    public static void throwException(String message) {
        throw new BusinessException(message);
    }

    /**
     * 抛出业务异常
     *
     * @param responseCode 响应码枚举
     */
    public static void throwException(ResponseCode responseCode) {
        throw new BusinessException(responseCode);
    }

    /**
     * 抛出业务异常
     *
     * @param responseCode 响应码枚举
     * @param message      自定义错误消息
     */
    public static void throwException(ResponseCode responseCode, String message) {
        throw new BusinessException(responseCode, message);
    }

    /**
     * 条件性抛出异常
     *
     * @param condition 条件
     * @param message   错误消息
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * 条件性抛出异常
     *
     * @param condition    条件
     * @param responseCode 响应码枚举
     */
    public static void throwIf(boolean condition, ResponseCode responseCode) {
        if (condition) {
            throw new BusinessException(responseCode);
        }
    }

    /**
     * 条件性抛出异常
     *
     * @param condition    条件
     * @param responseCode 响应码枚举
     * @param message      自定义错误消息
     */
    public static void throwIf(boolean condition, ResponseCode responseCode, String message) {
        if (condition) {
            throw new BusinessException(responseCode, message);
        }
    }
}