package com.moon.cloud.validator.isbn;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * ISBN号验证器实现
 */
public class ISBNValidator implements ConstraintValidator<ISBN, String> {

    private static final Pattern ISBN_PATTERN = Pattern.compile(
        "^\\d{13}$"
    );

    @Override
    public void initialize(ISBN constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        return ISBN_PATTERN.matcher(value).matches();
    }
}