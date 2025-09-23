package com.mooncloud.shorturl.exception;

/**
 * 短链已过期异常
 *
 * @author mooncloud
 */
public class ExpiredException extends ShortUrlException {

    public ExpiredException(String message) {
        super(410, message);
    }
}