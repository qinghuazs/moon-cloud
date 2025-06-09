package com.moon.cloud.validator.ip;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * IP地址验证器实现
 */
public class IpAddressValidator implements ConstraintValidator<IpAddress, String> {

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
        "^(\\d{1,3}\\.){3}\\d{1,3}$"
    );

    @Override
    public void initialize(IpAddress constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        
        if (!IP_ADDRESS_PATTERN.matcher(value).matches()) {
            return false;
        }
        
        // 进一步验证每个数字段是否在0-255范围内
        String[] parts = value.split("\\.");
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
}