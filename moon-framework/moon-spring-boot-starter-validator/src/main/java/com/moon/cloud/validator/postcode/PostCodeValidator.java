package com.moon.cloud.validator.postcode;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 邮政编码验证器实现
 */
public class PostCodeValidator implements ConstraintValidator<PostCode, String> {

    private static final Pattern POST_CODE_PATTERN = Pattern.compile(
        "^\\d{6}$"
    );

    @Override
    public void initialize(PostCode constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        return POST_CODE_PATTERN.matcher(value).matches();
    }
}