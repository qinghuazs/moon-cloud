package com.mooncloud.shorturl.exception;

/**
 * 业务异常
 *
 * @author mooncloud
 */
public class BusinessException extends ShortUrlException {

    public BusinessException(String message) {
        super(400, message);
    }

    public BusinessException(int code, String message) {
        super(code, message);
    }
}