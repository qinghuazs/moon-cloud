package com.moon.cloud.captcha.core;

/**
 * 验证码验证器接口
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public interface CaptchaValidator {

    /**
     * 验证验证码
     *
     * @param key  键
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean validate(String key, String code);

    /**
     * 验证验证码（忽略大小写）
     *
     * @param key            键
     * @param code           验证码
     * @param caseSensitive  是否大小写敏感
     * @return 是否验证成功
     */
    boolean validate(String key, String code, boolean caseSensitive);

    /**
     * 记录验证失败
     *
     * @param key 键
     */
    void recordFailure(String key);

    /**
     * 判断是否被锁定
     *
     * @param key 键
     * @return 是否被锁定
     */
    boolean isLocked(String key);

    /**
     * 获取失败次数
     *
     * @param key 键
     * @return 失败次数
     */
    int getFailureCount(String key);

    /**
     * 清除失败记录
     *
     * @param key 键
     */
    void clearFailureRecord(String key);
}