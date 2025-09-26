package com.moon.cloud.captcha.service;

import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.enums.CaptchaType;

import java.util.concurrent.TimeUnit;

/**
 * 验证码服务接口
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 验证码对象
     */
    Captcha generate();

    /**
     * 生成指定类型的验证码
     *
     * @param type 验证码类型
     * @return 验证码对象
     */
    Captcha generate(CaptchaType type);

    /**
     * 生成指定长度的验证码
     *
     * @param length 验证码长度
     * @return 验证码对象
     */
    Captcha generate(int length);

    /**
     * 生成指定类型和长度的验证码
     *
     * @param type   验证码类型
     * @param length 验证码长度
     * @return 验证码对象
     */
    Captcha generate(CaptchaType type, int length);

    /**
     * 保存验证码
     *
     * @param key     键
     * @param captcha 验证码对象
     */
    void save(String key, Captcha captcha);

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
     * 验证验证码
     *
     * @param key  键
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean validate(String key, String code);

    /**
     * 验证验证码
     *
     * @param key           键
     * @param code          验证码
     * @param caseSensitive 是否大小写敏感
     * @return 是否验证成功
     */
    boolean validate(String key, String code, boolean caseSensitive);

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
     */
    void remove(String key);

    /**
     * 检查是否被锁定
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
}