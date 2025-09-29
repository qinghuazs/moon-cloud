package com.moon.cloud.validator.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 邮箱地址验证器实现
 */
public class EmailValidator implements ConstraintValidator<Email, String> {

    private static final Pattern STANDARD_EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );

    private static final Pattern CHINESE_EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*\\u4e00-\\u9fa5-]+(?:\\.[a-zA-Z0-9_+&*\\u4e00-\\u9fa5-]+)*@(?:[a-zA-Z0-9\\u4e00-\\u9fa5-]+\\.)+[a-zA-Z\\u4e00-\\u9fa5]{2,}$"
    );

    private boolean allowSubdomain;
    private boolean allowChinese;

    @Override
    public void initialize(Email constraintAnnotation) {
        this.allowSubdomain = constraintAnnotation.allowSubdomain();
        this.allowChinese = constraintAnnotation.allowChinese();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        Pattern pattern = allowChinese ? CHINESE_EMAIL_PATTERN : STANDARD_EMAIL_PATTERN;

        if (!pattern.matcher(value).matches()) {
            return false;
        }

        // 检查子域名
        if (!allowSubdomain) {
            String domain = value.substring(value.indexOf('@') + 1);
            long dotCount = domain.chars().filter(ch -> ch == '.').count();
            if (dotCount > 1) {
                return false;
            }
        }

        return true;
    }
}