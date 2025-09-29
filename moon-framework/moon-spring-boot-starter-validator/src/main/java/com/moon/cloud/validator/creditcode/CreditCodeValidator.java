package com.moon.cloud.validator.creditcode;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 统一社会信用代码验证器实现
 * 验证18位统一社会信用代码
 */
public class CreditCodeValidator implements ConstraintValidator<CreditCode, String> {

    private static final Pattern CREDIT_CODE_PATTERN = Pattern.compile(
        "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$"
    );

    // 代码字符集
    private static final String CODE_INDEX = "0123456789ABCDEFGHJKLMNPQRTUWXY";

    // 加权因子
    private static final int[] WEIGHT_FACTOR = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};

    // 字符值映射
    private static final Map<Character, Integer> CHAR_TO_NUM = new HashMap<>();

    static {
        for (int i = 0; i < CODE_INDEX.length(); i++) {
            CHAR_TO_NUM.put(CODE_INDEX.charAt(i), i);
        }
    }

    @Override
    public void initialize(CreditCode constraintAnnotation) {
        // 无需初始化
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // 转换为大写
        value = value.toUpperCase();

        // 长度检查
        if (value.length() != 18) {
            return false;
        }

        // 格式检查
        if (!CREDIT_CODE_PATTERN.matcher(value).matches()) {
            return false;
        }

        // 校验码验证
        return validateCheckCode(value);
    }

    private boolean validateCheckCode(String creditCode) {
        try {
            char[] chars = creditCode.toCharArray();
            int sum = 0;

            // 计算前17位的加权和
            for (int i = 0; i < 17; i++) {
                Integer num = CHAR_TO_NUM.get(chars[i]);
                if (num == null) {
                    return false;
                }
                sum += num * WEIGHT_FACTOR[i];
            }

            // 计算校验码
            int checkCode = 31 - sum % 31;
            if (checkCode == 31) {
                checkCode = 0;
            }

            // 比较校验码
            return CODE_INDEX.charAt(checkCode) == chars[17];
        } catch (Exception e) {
            return false;
        }
    }
}