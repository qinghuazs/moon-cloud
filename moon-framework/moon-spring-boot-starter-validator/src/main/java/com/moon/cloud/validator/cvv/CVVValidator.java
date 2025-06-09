package com.moon.cloud.validator.cvv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * CVV码验证器实现
 */
public class CVVValidator implements ConstraintValidator<CVV, String> {

    private static final Pattern CVV_PATTERN = Pattern.compile(
        "^\\d{3,4}$"
    );

    @Override
    public void initialize(CVV constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        return CVV_PATTERN.matcher(value).matches();
    }
}