package com.moon.cloud.response.adapter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.moon.cloud.response.enums.ResponseCode;
import com.moon.cloud.response.web.MoonCloudResponse;

import java.util.List;

/**
 * 响应适配器
 * 提供各种响应类型到 MoonCloudResponse 的转换方法
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public class ResponseAdapter {

    /**
     * 私有构造函数，防止实例化
     */
    private ResponseAdapter() {
    }

    /**
     * 从 Result 类转换（适配 user 模块）
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> fromResult(Integer code, String message, T data) {
        return new MoonCloudResponse<>(code, message, data);
    }

    /**
     * 从 ApiResponse 类转换（适配 shorturl 模块）
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> fromApiResponse(int code, String message, T data) {
        return new MoonCloudResponse<>(code, message, data);
    }

    /**
     * 从 MyBatis Plus 的 IPage 转换为分页响应
     *
     * @param page IPage 对象
     * @param <T>  数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<List<T>> fromPage(IPage<T> page) {
        if (page == null) {
            return MoonCloudResponse.page(null, 0, 0, 0, 0);
        }

        return MoonCloudResponse.page(
                page.getRecords(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages()
        );
    }

    /**
     * 从 MyBatis Plus 的 IPage 转换为分页响应（自定义消息）
     *
     * @param page    IPage 对象
     * @param message 自定义消息
     * @param <T>     数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<List<T>> fromPage(IPage<T> page, String message) {
        MoonCloudResponse<List<T>> response = fromPage(page);
        response.setMessage(message);
        return response;
    }

    /**
     * 将旧的 Result 转换为 MoonCloudResponse
     * 兼容方法，用于平滑迁移
     *
     * @param result Result 对象
     * @param <T>    数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> convert(Object result) {
        if (result == null) {
            return MoonCloudResponse.success();
        }

        // 通过反射获取 Result 类的字段
        try {
            Class<?> clazz = result.getClass();

            // 获取 code
            Integer code = null;
            try {
                Object codeObj = clazz.getMethod("getCode").invoke(result);
                if (codeObj instanceof Integer) {
                    code = (Integer) codeObj;
                }
            } catch (Exception ignored) {
            }

            // 获取 message
            String message = null;
            try {
                Object messageObj = clazz.getMethod("getMessage").invoke(result);
                if (messageObj instanceof String) {
                    message = (String) messageObj;
                }
            } catch (Exception ignored) {
            }

            // 获取 data
            T data = null;
            try {
                data = (T) clazz.getMethod("getData").invoke(result);
            } catch (Exception ignored) {
            }

            // 创建响应
            if (code != null) {
                return new MoonCloudResponse<>(code, message, data);
            } else {
                return MoonCloudResponse.success(data);
            }

        } catch (Exception e) {
            // 如果转换失败，返回一个包含原对象的成功响应
            return MoonCloudResponse.success((T) result);
        }
    }

    /**
     * 根据错误码枚举名称获取 ResponseCode
     * 用于兼容旧的 ResultCode
     *
     * @param codeName 错误码枚举名称
     * @return ResponseCode
     */
    public static ResponseCode getResponseCode(String codeName) {
        if (codeName == null) {
            return ResponseCode.INTERNAL_SERVER_ERROR;
        }

        // 尝试直接匹配
        try {
            return ResponseCode.valueOf(codeName);
        } catch (IllegalArgumentException e) {
            // 如果直接匹配失败，尝试映射
            return mapResultCode(codeName);
        }
    }

    /**
     * 映射旧的 ResultCode 到新的 ResponseCode
     *
     * @param codeName ResultCode 名称
     * @return ResponseCode
     */
    private static ResponseCode mapResultCode(String codeName) {
        // 映射关系
        switch (codeName) {
            case "SUCCESS":
                return ResponseCode.SUCCESS;
            case "ERROR":
                return ResponseCode.INTERNAL_SERVER_ERROR;
            case "PARAM_ERROR":
                return ResponseCode.BAD_REQUEST;
            case "NOT_FOUND":
                return ResponseCode.NOT_FOUND;
            case "UNAUTHORIZED":
                return ResponseCode.UNAUTHORIZED;
            case "FORBIDDEN":
                return ResponseCode.FORBIDDEN;
            case "TOKEN_INVALID":
                return ResponseCode.TOKEN_INVALID;
            case "TOKEN_EXPIRED":
                return ResponseCode.TOKEN_EXPIRED;
            case "LOGIN_FAILED":
                return ResponseCode.LOGIN_FAILED;
            case "USER_NOT_FOUND":
                return ResponseCode.USER_NOT_FOUND;
            case "USERNAME_EXISTS":
                return ResponseCode.USERNAME_EXISTS;
            case "EMAIL_EXISTS":
                return ResponseCode.EMAIL_EXISTS;
            case "PHONE_EXISTS":
                return ResponseCode.PHONE_EXISTS;
            case "DATABASE_ERROR":
                return ResponseCode.DATABASE_ERROR;
            case "BUSINESS_ERROR":
                return ResponseCode.BUSINESS_ERROR;
            default:
                return ResponseCode.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * 快速成功响应（兼容方法）
     *
     * @param <T> 数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> ok() {
        return MoonCloudResponse.success();
    }

    /**
     * 快速成功响应（兼容方法）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> ok(T data) {
        return MoonCloudResponse.success(data);
    }

    /**
     * 快速成功响应（兼容方法）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> ok(String message, T data) {
        return MoonCloudResponse.success(message, data);
    }

    /**
     * 快速失败响应（兼容方法）
     *
     * @param <T> 数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> fail() {
        return MoonCloudResponse.error();
    }

    /**
     * 快速失败响应（兼容方法）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> fail(String message) {
        return MoonCloudResponse.error(message);
    }

    /**
     * 快速失败响应（兼容方法）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return MoonCloudResponse
     */
    public static <T> MoonCloudResponse<T> fail(Integer code, String message) {
        return MoonCloudResponse.error(code, message);
    }
}