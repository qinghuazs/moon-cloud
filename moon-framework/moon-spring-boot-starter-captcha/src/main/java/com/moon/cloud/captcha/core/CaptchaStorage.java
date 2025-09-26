package com.moon.cloud.captcha.core;

import java.util.concurrent.TimeUnit;

/**
 * 验证码存储接口
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public interface CaptchaStorage {

    /**
     * 保存验证码
     *
     * @param key     键
     * @param captcha 验证码对象
     * @param timeout 超时时间
     * @param unit    时间单位
     */
    void save(String key, Captcha captcha, long timeout, TimeUnit unit);

    /**
     * 获取验证码
     *
     * @param key 键
     * @return 验证码对象
     */
    Captcha get(String key);

    /**
     * 删除验证码
     *
     * @param key 键
     * @return 是否删除成功
     */
    boolean remove(String key);

    /**
     * 判断验证码是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 更新验证码
     *
     * @param key     键
     * @param captcha 验证码对象
     */
    void update(String key, Captcha captcha);

    /**
     * 记录验证失败
     *
     * @param key 键
     * @return 失败次数
     */
    int recordFailure(String key);

    /**
     * 清空所有验证码
     */
    void clear();
}