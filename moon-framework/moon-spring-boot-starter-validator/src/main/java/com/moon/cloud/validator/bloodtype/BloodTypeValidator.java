package com.moon.cloud.validator.bloodtype;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 血型验证器实现
 */
public class BloodTypeValidator implements ConstraintValidator<BloodType, String> {

    private static final Pattern BLOOD_TYPE_PATTERN = Pattern.compile(
        "^(A|B|AB|O)[+-]$"
    );

    @Override
    public void initialize(BloodType constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        return BLOOD_TYPE_PATTERN.matcher(value).matches();
    }
}