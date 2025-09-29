package com.moon.cloud.validator.qq;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * QQ号验证器实现
 * QQ号规则：5-11位数字，首位不能为0
 */
public class QQNumberValidator implements ConstraintValidator<QQNumber, String> {

    private static final Pattern QQ_PATTERN = Pattern.compile(
        "^[1-9][0-9]{4,10}$"
    );

    @Override
    public void initialize(QQNumber constraintAnnotation) {
        // 无需初始化
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        return QQ_PATTERN.matcher(value).matches();
    }
}