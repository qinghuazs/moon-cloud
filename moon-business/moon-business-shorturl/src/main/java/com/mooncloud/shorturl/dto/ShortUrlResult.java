package com.mooncloud.shorturl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 短链生成结果
 * 
 * @author mooncloud
 */
@Data
@AllArgsConstructor
public class ShortUrlResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 生成的短链
     */
    private String shortUrl;
    
    /**
     * 是否为新创建的短链
     */
    private boolean isNew;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 创建成功结果
     * 
     * @param shortUrl 短链
     * @param isNew 是否为新创建
     * @return 成功结果
     */
    public static ShortUrlResult success(String shortUrl, boolean isNew) {
        return new ShortUrlResult(true, shortUrl, isNew, "生成成功");
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @return 失败结果
     */
    public static ShortUrlResult failure(String message) {
        return new ShortUrlResult(false, null, false, message);
    }
}