package com.moon.cloud.validator.wechat;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 微信号验证器实现
 * 微信号规则：
 * 1. 可以使用6-20个字母、数字、下划线和减号
 * 2. 必须以字母开头（字母不区分大小写）
 * 3. 不支持设置中文
 */
public class WeChatIdValidator implements ConstraintValidator<WeChatId, String> {

    private static final Pattern WECHAT_ID_PATTERN = Pattern.compile(
        "^[a-zA-Z][a-zA-Z0-9_-]{5,19}$"
    );

    @Override
    public void initialize(WeChatId constraintAnnotation) {
        // 无需初始化
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        return WECHAT_ID_PATTERN.matcher(value).matches();
    }
}