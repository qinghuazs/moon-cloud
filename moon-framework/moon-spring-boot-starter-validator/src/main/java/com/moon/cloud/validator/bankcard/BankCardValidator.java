package com.moon.cloud.validator.bankcard;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 银行卡号验证器实现
 */
public class BankCardValidator implements ConstraintValidator<BankCard, String> {

    private static final Pattern BANK_CARD_PATTERN = Pattern.compile(
        "^\\d{16,19}$"
    );

    @Override
    public void initialize(BankCard constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        return BANK_CARD_PATTERN.matcher(value).matches();
    }
}