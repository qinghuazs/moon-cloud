package com.moon.cloud.validator.nickname;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 中文昵称验证器实现
 */
public class ChineseNicknameValidator implements ConstraintValidator<ChineseNickname, String> {

    private static final Pattern CHINESE_NICKNAME_PATTERN = Pattern.compile(
        "^[\\w\\u4e00-\\u9fa5]+$"
    );
    
    private int min;
    private int max;

    @Override
    public void initialize(ChineseNickname constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        
        // 检查长度
        if (value.length() < min || value.length() > max) {
            return false;
        }
        
        // 检查格式
        return CHINESE_NICKNAME_PATTERN.matcher(value).matches();
    }
}