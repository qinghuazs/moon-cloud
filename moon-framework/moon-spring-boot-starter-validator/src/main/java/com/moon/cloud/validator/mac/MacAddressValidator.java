package com.moon.cloud.validator.mac;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * MAC地址验证器实现
 */
public class MacAddressValidator implements ConstraintValidator<MacAddress, String> {

    private static final Pattern MAC_COLON_PATTERN = Pattern.compile(
        "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$"
    );

    private static final Pattern MAC_DASH_PATTERN = Pattern.compile(
        "^([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}$"
    );

    private static final Pattern MAC_NO_SEPARATOR_PATTERN = Pattern.compile(
        "^[0-9A-Fa-f]{12}$"
    );

    private String separator;
    private boolean allowNoSeparator;

    @Override
    public void initialize(MacAddress constraintAnnotation) {
        this.separator = constraintAnnotation.separator();
        this.allowNoSeparator = constraintAnnotation.allowNoSeparator();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // 转换为大写
        value = value.toUpperCase();

        // 无分隔符格式
        if (allowNoSeparator && MAC_NO_SEPARATOR_PATTERN.matcher(value).matches()) {
            return true;
        }

        // 根据分隔符验证
        if (":".equals(separator)) {
            return MAC_COLON_PATTERN.matcher(value).matches();
        } else if ("-".equals(separator)) {
            return MAC_DASH_PATTERN.matcher(value).matches();
        } else {
            // 同时支持两种格式
            return MAC_COLON_PATTERN.matcher(value).matches() ||
                   MAC_DASH_PATTERN.matcher(value).matches();
        }
    }
}