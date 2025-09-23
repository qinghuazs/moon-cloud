package com.mooncloud.shorturl.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * 创建短链请求DTO
 *
 * @author mooncloud
 */
@Data
public class CreateShortUrlRequest {

    /**
     * 原始URL
     */
    @NotBlank(message = "原始URL不能为空")
    @Size(max = 2048, message = "URL长度不能超过2048字符")
    @Pattern(regexp = "^https?://.*", message = "URL格式不正确")
    private String originalUrl;

    /**
     * 自定义短码（可选）
     */
    @Size(min = 3, max = 20, message = "自定义短码长度必须在3-20字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "自定义短码只能包含字母、数字、下划线和连字符")
    private String customCode;

    /**
     * 用户ID（可为空，表示游客用户）
     */
    private Long userId;

    /**
     * 过期时间（可选）
     */
    private LocalDateTime expireTime;

    /**
     * 链接标题（可选）
     */
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    /**
     * 描述信息（可选）
     */
    @Size(max = 500, message = "描述信息长度不能超过500字符")
    private String description;

    /**
     * 是否检查已存在的URL
     */
    private boolean checkExisting = true;
}