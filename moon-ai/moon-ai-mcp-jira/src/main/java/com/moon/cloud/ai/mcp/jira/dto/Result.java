package com.moon.cloud.ai.mcp.jira.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用响应结果
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
public class Result<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return new Result<T>()
                .setCode(200)
                .setMessage("操作成功");
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>()
                .setCode(200)
                .setMessage("操作成功")
                .setData(data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<T>()
                .setCode(200)
                .setMessage(message)
                .setData(data);
    }

    public static <T> Result<T> error() {
        return new Result<T>()
                .setCode(500)
                .setMessage("操作失败");
    }

    public static <T> Result<T> error(String message) {
        return new Result<T>()
                .setCode(500)
                .setMessage(message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>()
                .setCode(code)
                .setMessage(message);
    }
}