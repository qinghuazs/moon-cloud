package com.moon.cloud.validator.url;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * URL地址验证器实现
 */
public class UrlValidator implements ConstraintValidator<Url, String> {

    private Set<String> allowedProtocols;
    private boolean allowLocal;
    private boolean requirePort;

    @Override
    public void initialize(Url constraintAnnotation) {
        this.allowedProtocols = new HashSet<>(Arrays.asList(constraintAnnotation.protocols()));
        this.allowLocal = constraintAnnotation.allowLocal();
        this.requirePort = constraintAnnotation.requirePort();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        try {
            URL url = new URL(value);

            // 验证协议
            if (!allowedProtocols.contains(url.getProtocol().toLowerCase())) {
                return false;
            }

            // 验证主机名
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return false;
            }

            // 检查本地地址
            if (!allowLocal && isLocalHost(host)) {
                return false;
            }

            // 检查端口
            if (requirePort && url.getPort() == -1) {
                return false;
            }

            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private boolean isLocalHost(String host) {
        return "localhost".equalsIgnoreCase(host) ||
               "127.0.0.1".equals(host) ||
               "0.0.0.0".equals(host) ||
               "::1".equals(host) ||
               host.startsWith("192.168.") ||
               host.startsWith("10.") ||
               host.startsWith("172.");
    }
}