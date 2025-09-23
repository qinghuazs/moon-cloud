package com.mooncloud.shorturl.exception;

/**
 * 资源未找到异常
 *
 * @author mooncloud
 */
public class NotFoundException extends ShortUrlException {

    public NotFoundException(String message) {
        super(404, message);
    }
}