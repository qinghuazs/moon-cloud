package com.mooncloud.shorturl.exception;

/**
 * 短链系统异常基类
 *
 * @author mooncloud
 */
public class ShortUrlException extends RuntimeException {

    private final int code;

    public ShortUrlException(String message) {
        super(message);
        this.code = 500;
    }

    public ShortUrlException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ShortUrlException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public int getCode() {
        return code;
    }
}