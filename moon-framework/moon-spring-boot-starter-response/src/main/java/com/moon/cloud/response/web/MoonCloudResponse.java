package com.moon.cloud.response.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moon.cloud.response.enums.ResponseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 全局统一响应类
 *
 * 功能特性：
 * - 支持泛型数据
 * - 支持链式调用
 * - 支持分页数据
 * - 支持响应码枚举
 * - 支持国际化消息
 * - 支持追踪ID
 *
 * @param <T> 响应数据类型
 * @author Moon Cloud
 * @since 1.0.0
 */
@Data
@Schema(description = "统一响应结果")
public class MoonCloudResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    @Schema(description = "响应码", example = "200")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 时间戳（毫秒）
     */
    @Schema(description = "响应时间戳", example = "1704038400000")
    private Long timestamp;

    /**
     * 格式化的时间
     */
    @Schema(description = "响应时间", example = "2024-01-01 00:00:00")
    private String time;

    /**
     * 追踪ID（用于日志追踪）
     */
    @Schema(description = "追踪ID", example = "uuid-12345")
    private String traceId;

    /**
     * 分页信息（仅在分页查询时使用）
     */
    @Schema(description = "分页信息")
    private PageInfo pageInfo;

    /**
     * 扩展信息（可选）
     */
    @Schema(description = "扩展信息")
    private Object extra;

    /**
     * 默认构造函数
     */
    public MoonCloudResponse() {
        this.timestamp = System.currentTimeMillis();
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 完整构造函数
     */
    public MoonCloudResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 响应码构造函数
     */
    public MoonCloudResponse(ResponseCode responseCode) {
        this();
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }

    /**
     * 响应码构造函数（带数据）
     */
    public MoonCloudResponse(ResponseCode responseCode, T data) {
        this(responseCode);
        this.data = data;
    }

    /**
     * 响应码构造函数（自定义消息）
     */
    public MoonCloudResponse(ResponseCode responseCode, String message) {
        this();
        this.code = responseCode.getCode();
        this.message = message;
    }

    /**
     * 响应码构造函数（自定义消息和数据）
     */
    public MoonCloudResponse(ResponseCode responseCode, String message, T data) {
        this(responseCode, message);
        this.data = data;
    }

    // ========== 成功响应静态方法 ==========

    /**
     * 成功响应（无数据）
     */
    public static <T> MoonCloudResponse<T> success() {
        return new MoonCloudResponse<>(ResponseCode.SUCCESS);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> MoonCloudResponse<T> success(T data) {
        return new MoonCloudResponse<>(ResponseCode.SUCCESS, data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> MoonCloudResponse<T> success(String message) {
        return new MoonCloudResponse<>(ResponseCode.SUCCESS, message);
    }

    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> MoonCloudResponse<T> success(String message, T data) {
        return new MoonCloudResponse<>(ResponseCode.SUCCESS, message, data);
    }

    /**
     * 创建成功响应
     */
    public static <T> MoonCloudResponse<T> created() {
        return new MoonCloudResponse<>(ResponseCode.CREATED);
    }

    /**
     * 创建成功响应（带数据）
     */
    public static <T> MoonCloudResponse<T> created(T data) {
        return new MoonCloudResponse<>(ResponseCode.CREATED, data);
    }

    // ========== 失败响应静态方法 ==========

    /**
     * 失败响应（默认错误）
     */
    public static <T> MoonCloudResponse<T> error() {
        return new MoonCloudResponse<>(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> MoonCloudResponse<T> error(String message) {
        return new MoonCloudResponse<>(ResponseCode.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 失败响应（指定响应码）
     */
    public static <T> MoonCloudResponse<T> error(ResponseCode responseCode) {
        return new MoonCloudResponse<>(responseCode);
    }

    /**
     * 失败响应（指定响应码和自定义消息）
     */
    public static <T> MoonCloudResponse<T> error(ResponseCode responseCode, String message) {
        return new MoonCloudResponse<>(responseCode, message);
    }

    /**
     * 失败响应（自定义响应码和消息）
     */
    public static <T> MoonCloudResponse<T> error(Integer code, String message) {
        return new MoonCloudResponse<>(code, message, null);
    }

    /**
     * 参数错误响应
     */
    public static <T> MoonCloudResponse<T> badRequest() {
        return new MoonCloudResponse<>(ResponseCode.BAD_REQUEST);
    }

    /**
     * 参数错误响应（自定义消息）
     */
    public static <T> MoonCloudResponse<T> badRequest(String message) {
        return new MoonCloudResponse<>(ResponseCode.BAD_REQUEST, message);
    }

    /**
     * 未认证响应
     */
    public static <T> MoonCloudResponse<T> unauthorized() {
        return new MoonCloudResponse<>(ResponseCode.UNAUTHORIZED);
    }

    /**
     * 未认证响应（自定义消息）
     */
    public static <T> MoonCloudResponse<T> unauthorized(String message) {
        return new MoonCloudResponse<>(ResponseCode.UNAUTHORIZED, message);
    }

    /**
     * 无权限响应
     */
    public static <T> MoonCloudResponse<T> forbidden() {
        return new MoonCloudResponse<>(ResponseCode.FORBIDDEN);
    }

    /**
     * 无权限响应（自定义消息）
     */
    public static <T> MoonCloudResponse<T> forbidden(String message) {
        return new MoonCloudResponse<>(ResponseCode.FORBIDDEN, message);
    }

    /**
     * 资源不存在响应
     */
    public static <T> MoonCloudResponse<T> notFound() {
        return new MoonCloudResponse<>(ResponseCode.NOT_FOUND);
    }

    /**
     * 资源不存在响应（自定义消息）
     */
    public static <T> MoonCloudResponse<T> notFound(String message) {
        return new MoonCloudResponse<>(ResponseCode.NOT_FOUND, message);
    }

    /**
     * 服务不可用响应
     */
    public static <T> MoonCloudResponse<T> serviceUnavailable() {
        return new MoonCloudResponse<>(ResponseCode.SERVICE_UNAVAILABLE);
    }

    /**
     * 服务不可用响应（自定义消息）
     */
    public static <T> MoonCloudResponse<T> serviceUnavailable(String message) {
        return new MoonCloudResponse<>(ResponseCode.SERVICE_UNAVAILABLE, message);
    }

    // ========== 分页响应静态方法 ==========

    /**
     * 分页成功响应
     */
    public static <T> MoonCloudResponse<T> page(T data, PageInfo pageInfo) {
        MoonCloudResponse<T> response = success(data);
        response.setPageInfo(pageInfo);
        return response;
    }

    /**
     * 分页成功响应（简化版）
     */
    public static <T> MoonCloudResponse<T> page(T data, long current, long size, long total) {
        PageInfo pageInfo = PageInfo.of(current, size, total);
        return page(data, pageInfo);
    }

    /**
     * 分页成功响应（完整版）
     */
    public static <T> MoonCloudResponse<T> page(T data, long current, long size, long total, long pages) {
        PageInfo pageInfo = PageInfo.of(current, size, total, pages);
        return page(data, pageInfo);
    }

    // ========== 链式调用方法 ==========

    /**
     * 设置追踪ID（链式调用）
     */
    public MoonCloudResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 设置扩展信息（链式调用）
     */
    public MoonCloudResponse<T> withExtra(Object extra) {
        this.extra = extra;
        return this;
    }

    /**
     * 设置分页信息（链式调用）
     */
    public MoonCloudResponse<T> withPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
        return this;
    }

    /**
     * 设置分页信息（链式调用）
     */
    public MoonCloudResponse<T> withPageInfo(long current, long size, long total) {
        this.pageInfo = PageInfo.of(current, size, total);
        return this;
    }

    // ========== 判断方法 ==========

    /**
     * 判断响应是否成功
     */
    @JsonIgnore
    public boolean isSuccess() {
        return code != null && code >= 200 && code < 300;
    }

    /**
     * 判断响应是否失败
     */
    @JsonIgnore
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 判断是否为分页响应
     */
    @JsonIgnore
    public boolean isPaged() {
        return pageInfo != null;
    }

    /**
     * 判断是否有数据
     */
    @JsonIgnore
    public boolean hasData() {
        return data != null;
    }

    /**
     * 判断是否为指定状态码
     */
    @JsonIgnore
    public boolean isCode(Integer code) {
        return this.code != null && this.code.equals(code);
    }

    /**
     * 判断是否为指定响应码枚举
     */
    @JsonIgnore
    public boolean isCode(ResponseCode responseCode) {
        return responseCode != null && isCode(responseCode.getCode());
    }

    // ========== 内部类：分页信息 ==========

    /**
     * 分页信息
     */
    @Data
    @Schema(description = "分页信息")
    public static class PageInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "当前页码", example = "1")
        private Long current;

        @Schema(description = "每页大小", example = "10")
        private Long size;

        @Schema(description = "总记录数", example = "100")
        private Long total;

        @Schema(description = "总页数", example = "10")
        private Long pages;

        @Schema(description = "是否有上一页", example = "false")
        private Boolean hasPrevious;

        @Schema(description = "是否有下一页", example = "true")
        private Boolean hasNext;

        public PageInfo() {
        }

        public PageInfo(Long current, Long size, Long total) {
            this.current = current;
            this.size = size;
            this.total = total;
            this.pages = (total + size - 1) / size;
            this.hasPrevious = current > 1;
            this.hasNext = current < pages;
        }

        public PageInfo(Long current, Long size, Long total, Long pages) {
            this.current = current;
            this.size = size;
            this.total = total;
            this.pages = pages;
            this.hasPrevious = current > 1;
            this.hasNext = current < pages;
        }

        /**
         * 创建分页信息
         */
        public static PageInfo of(Long current, Long size, Long total) {
            return new PageInfo(current, size, total);
        }

        /**
         * 创建分页信息（完整版）
         */
        public static PageInfo of(Long current, Long size, Long total, Long pages) {
            return new PageInfo(current, size, total, pages);
        }
    }

    /**
     * 用于兼容旧版本的方法
     * @deprecated 使用 isSuccess() 代替
     */
    @Deprecated
    @JsonIgnore
    public boolean getSuccess() {
        return isSuccess();
    }
}