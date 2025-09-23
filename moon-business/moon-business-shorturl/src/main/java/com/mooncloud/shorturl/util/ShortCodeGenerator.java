package com.mooncloud.shorturl.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 短码生成器 - 参考文档的实现
 * 支持多种生成策略，确保短码的唯一性和安全性
 *
 * @author mooncloud
 */
@Component
public class ShortCodeGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();
    private static final int SHORT_CODE_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    /**
     * 基于数据库ID生成短码（Base62编码）
     */
    public String generateByBase62(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }

        StringBuilder shortCode = new StringBuilder();
        while (id > 0) {
            shortCode.append(ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }

        // 补足长度
        while (shortCode.length() < SHORT_CODE_LENGTH) {
            shortCode.append(ALPHABET.charAt(0));
        }

        return shortCode.reverse().toString();
    }

    /**
     * 生成随机短码
     */
    public String generateRandom() {
        StringBuilder shortCode = new StringBuilder();
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            shortCode.append(ALPHABET.charAt(random.nextInt(BASE)));
        }
        return shortCode.toString();
    }

    /**
     * 基于URL哈希生成短码
     */
    public String generateByHash(String url) {
        long hash = Math.abs(url.hashCode()) & 0x7FFFFFFFL; // 确保为正数
        return generateByBase62(hash % (long) Math.pow(BASE, SHORT_CODE_LENGTH));
    }

    /**
     * 基于URL哈希生成短码（带盐值）
     */
    public String generateByHash(String url, int salt) {
        String saltedUrl = url + salt;
        return generateByHash(saltedUrl);
    }

    /**
     * 解码短码为数字
     */
    public Long decodeToId(String shortCode) {
        if (shortCode == null || shortCode.isEmpty()) {
            throw new IllegalArgumentException("Short code cannot be null or empty");
        }

        long id = 0;
        for (char c : shortCode.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in short code: " + c);
            }
            id = id * BASE + index;
        }
        return id;
    }

    /**
     * 验证短码格式
     */
    public boolean isValidShortCode(String shortCode) {
        if (shortCode == null || shortCode.length() != SHORT_CODE_LENGTH) {
            return false;
        }

        for (char c : shortCode.toCharArray()) {
            if (ALPHABET.indexOf(c) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取短码长度
     */
    public int getShortCodeLength() {
        return SHORT_CODE_LENGTH;
    }

    /**
     * 获取字符集大小
     */
    public int getBase() {
        return BASE;
    }
}