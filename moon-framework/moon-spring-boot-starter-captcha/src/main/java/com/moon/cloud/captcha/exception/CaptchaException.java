package com.moon.cloud.captcha.exception;

/**
 * 验证码异常
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public class CaptchaException extends RuntimeException {

    public CaptchaException(String message) {
        super(message);
    }

    public CaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}