package com.moon.cloud.response.web;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一响应封装类
 * 支持泛型，可以封装任意类型的响应数据
 *
 * @param <T> 响应数据类型
 * @author Moon Cloud
 */
@Data
public class MoonCloudResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应是否成功
     */
    private boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据（单个对象）
     */
    private T data;

    /**
     * 错误代码（可选）
     */
    private String errorCode;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 总记录数（用于分页）
     */
    private Long total;

    /**
     * 当前页码（用于分页）
     */
    private Integer page;

    /**
     * 每页大小（用于分页）
     */
    private Integer size;

    /**
     * 默认构造函数
     */
    public MoonCloudResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造函数
     *
     * @param success 是否成功
     * @param message 响应消息
     */
    public MoonCloudResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    /**
     * 构造函数
     *
     * @param success 是否成功
     * @param message 响应消息
     * @param data    响应数据
     */
    public MoonCloudResponse(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }

    /**
     * 构造函数（带错误代码）
     *
     * @param success   是否成功
     * @param message   响应消息
     * @param errorCode 错误代码
     */
    public MoonCloudResponse(boolean success, String message, String errorCode) {
        this(success, message);
        this.errorCode = errorCode;
    }

    /**
     * 创建成功响应
     *
     * @param message 成功消息
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> MoonCloudResponse<T> success(String message) {
        return new MoonCloudResponse<>(true, message);
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param message 成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> MoonCloudResponse<T> success(String message, T data) {
        return new MoonCloudResponse<>(true, message, data);
    }

    /**
     * 创建成功响应（无消息）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> MoonCloudResponse<T> success() {
        return new MoonCloudResponse<>(true, "操作成功");
    }

    /**
     * 创建成功响应（仅带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> MoonCloudResponse<T> success(T data) {
        return new MoonCloudResponse<>(true, "操作成功", data);
    }

    /**
     * 创建失败响应
     *
     * @param message 失败消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> MoonCloudResponse<T> error(String message) {
        return new MoonCloudResponse<>(false, message);
    }

    /**
     * 创建失败响应（带错误代码）
     *
     * @param message   失败消息
     * @param errorCode 错误代码
     * @param <T>       数据类型
     * @return 失败响应
     */
    public static <T> MoonCloudResponse<T> error(String message, String errorCode) {
        return new MoonCloudResponse<>(false, message, errorCode);
    }

    /**
     * 创建失败响应（带数据和错误代码）
     *
     * @param message   失败消息
     * @param data      响应数据
     * @param errorCode 错误代码
     * @param <T>       数据类型
     * @return 失败响应
     */
    public static <T> MoonCloudResponse<T> error(String message, T data, String errorCode) {
        MoonCloudResponse<T> response = new MoonCloudResponse<>(false, message, data);
        response.setErrorCode(errorCode);
        return response;
    }

    /**
     * 创建分页成功响应
     *
     * @param message 成功消息
     * @param data    响应数据
     * @param total   总记录数
     * @param page    当前页码
     * @param size    每页大小
     * @param <T>     数据类型
     * @return 分页成功响应
     */
    public static <T> MoonCloudResponse<T> successPage(String message, T data, Long total, Integer page, Integer size) {
        MoonCloudResponse<T> response = new MoonCloudResponse<>(true, message, data);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        return response;
    }

    /**
     * 创建分页成功响应（默认消息）
     *
     * @param data  响应数据
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页大小
     * @param <T>   数据类型
     * @return 分页成功响应
     */
    public static <T> MoonCloudResponse<T> successPage(T data, Long total, Integer page, Integer size) {
        return successPage("查询成功", data, total, page, size);
    }

    /**
     * 判断响应是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * 判断响应是否失败
     *
     * @return true-失败，false-成功
     */
    public boolean isError() {
        return !this.success;
    }

    /**
     * 判断是否为分页响应
     *
     * @return true-分页响应，false-非分页响应
     */
    public boolean isPaginated() {
        return this.total != null && this.page != null && this.size != null;
    }
}
