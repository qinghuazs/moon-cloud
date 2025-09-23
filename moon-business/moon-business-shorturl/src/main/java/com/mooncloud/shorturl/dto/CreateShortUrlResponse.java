package com.mooncloud.shorturl.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 创建短链响应DTO
 *
 * @author mooncloud
 */
@Data
@Builder
public class CreateShortUrlResponse {

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 完整短链URL
     */
    private String shortUrl;

    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 是否为新创建
     */
    private boolean isNew;

    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 创建时间
     */
    private String createdTime;
}