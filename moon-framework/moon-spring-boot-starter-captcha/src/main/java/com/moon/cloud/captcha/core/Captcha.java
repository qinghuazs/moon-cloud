package com.moon.cloud.captcha.core;

import com.moon.cloud.captcha.enums.CaptchaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码实体
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Captcha implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 验证码唯一标识
     */
    private String id;

    /**
     * 验证码内容
     */
    private String code;

    /**
     * 验证码答案（用于算术验证码等）
     */
    private String answer;

    /**
     * 验证码类型
     */
    private CaptchaType type;

    /**
     * 验证码图片Base64（用于图形验证码）
     */
    private String image;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 验证失败次数
     */
    private Integer failCount;

    /**
     * 是否已使用
     */
    private Boolean used;

    /**
     * 附加数据
     */
    private Object extra;

    /**
     * 判断验证码是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 判断验证码是否有效
     */
    public boolean isValid() {
        return !isExpired() && !used && (failCount == null || failCount < 5);
    }
}