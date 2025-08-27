package com.moon.cloud.user.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 分页响应结果类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分页响应结果")
public class PageResult<T> extends Result<List<T>> {

    @Schema(description = "当前页码")
    private Long current;

    @Schema(description = "每页大小")
    private Long size;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long pages;

    public PageResult() {
        super();
    }

    public PageResult(Integer code, String message, List<T> data, Long current, Long size, Long total, Long pages) {
        super(code, message, data);
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
    }

    /**
     * 成功分页响应
     */
    public static <T> PageResult<T> success(IPage<T> page) {
        return new PageResult<>(
                ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMessage(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages()
        );
    }

    /**
     * 成功分页响应
     */
    public static <T> PageResult<T> success(List<T> records, Long current, Long size, Long total) {
        Long pages = (total + size - 1) / size;
        return new PageResult<>(
                ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMessage(),
                records,
                current,
                size,
                total,
                pages
        );
    }

    /**
     * 失败分页响应
     */
    public static <T> PageResult<T> errorPage() {
        return new PageResult<>(
                ResultCode.ERROR.getCode(),
                ResultCode.ERROR.getMessage(),
                null,
                0L,
                0L,
                0L,
                0L
        );
    }

    /**
     * 失败分页响应
     */
    public static <T> PageResult<T> errorPage(String message) {
        return new PageResult<>(
                ResultCode.ERROR.getCode(),
                message,
                null,
                0L,
                0L,
                0L,
                0L
        );
    }

    /**
     * 失败分页响应
     */
    public static <T> PageResult<T> errorPage(ResultCode resultCode) {
        return new PageResult<>(
                resultCode.getCode(),
                resultCode.getMessage(),
                null,
                0L,
                0L,
                0L,
                0L
        );
    }
}