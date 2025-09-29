package com.moon.cloud.validator.telephone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 固定电话验证器实现（中国）
 */
public class TelephoneValidator implements ConstraintValidator<Telephone, String> {

    // 带区号的固定电话（支持3-4位区号，7-8位电话号码，可选分机号）
    private static final Pattern WITH_AREA_CODE_PATTERN = Pattern.compile(
        "^(0[0-9]{2,3}-)?([2-9][0-9]{6,7})(-[0-9]{1,6})?$"
    );

    // 不带区号的固定电话
    private static final Pattern WITHOUT_AREA_CODE_PATTERN = Pattern.compile(
        "^([2-9][0-9]{6,7})(-[0-9]{1,6})?$"
    );

    // 400/800电话
    private static final Pattern SERVICE_NUMBER_PATTERN = Pattern.compile(
        "^(400|800)(-)?[0-9]{3}(-)?[0-9]{4}$"
    );

    private boolean requireAreaCode;
    private boolean allowExtension;

    @Override
    public void initialize(Telephone constraintAnnotation) {
        this.requireAreaCode = constraintAnnotation.requireAreaCode();
        this.allowExtension = constraintAnnotation.allowExtension();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // 移除空格
        value = value.replaceAll("\\s+", "");

        // 检查是否为服务电话
        if (SERVICE_NUMBER_PATTERN.matcher(value).matches()) {
            return true;
        }

        // 检查分机号
        if (!allowExtension && value.contains("-") && value.lastIndexOf("-") != value.indexOf("-")) {
            return false;
        }

        if (requireAreaCode) {
            // 必须有区号
            return value.startsWith("0") && WITH_AREA_CODE_PATTERN.matcher(value).matches();
        } else {
            // 可以有区号也可以没有
            return WITH_AREA_CODE_PATTERN.matcher(value).matches() ||
                   WITHOUT_AREA_CODE_PATTERN.matcher(value).matches();
        }
    }
}