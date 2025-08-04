package com.mooncloud.shorturl.util;

import org.springframework.stereotype.Component;

/**
 * Base62编码器
 * 使用62个字符（0-9, a-z, A-Z）进行编码，将长整型ID压缩为短字符串
 * 
 * @author mooncloud
 */
@Component
public class Base62Encoder {
    
    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;
    
    /**
     * 将数字ID编码为Base62字符串
     * 
     * @param id 要编码的数字ID
     * @return Base62编码后的字符串
     */
    public String encode(long id) {
        if (id == 0) {
            return "0";
        }
        
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62_CHARS.charAt((int) (id % BASE)));
            id /= BASE;
        }
        
        return sb.reverse().toString();
    }
    
    /**
     * 将Base62字符串解码为数字ID
     * 
     * @param shortUrl Base62编码的字符串
     * @return 解码后的数字ID
     * @throws IllegalArgumentException 如果包含无效字符
     */
    public long decode(String shortUrl) {
        long result = 0;
        long power = 1;
        
        for (int i = shortUrl.length() - 1; i >= 0; i--) {
            char c = shortUrl.charAt(i);
            int index = BASE62_CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("无效的Base62字符: " + c);
            }
            result += index * power;
            power *= BASE;
        }
        
        return result;
    }
    
    /**
     * 生成指定长度的短链（补零）
     * 
     * @param id 要编码的数字ID
     * @param minLength 最小长度
     * @return 指定长度的Base62字符串
     */
    public String encodeWithPadding(long id, int minLength) {
        String encoded = encode(id);
        if (encoded.length() >= minLength) {
            return encoded;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < minLength - encoded.length(); i++) {
            sb.append('0');
        }
        sb.append(encoded);
        
        return sb.toString();
    }
}