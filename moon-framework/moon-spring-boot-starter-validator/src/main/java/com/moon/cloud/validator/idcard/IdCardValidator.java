package com.moon.cloud.validator.idcard;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * 身份证号验证器实现
 * 支持18位身份证号的完整验证，包括：
 * 1. 格式验证
 * 2. 地区码验证
 * 3. 出生日期验证
 * 4. 校验码验证
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$"
    );

    private static final int[] WEIGHT_FACTORS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void initialize(IdCard constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // 转换为大写
        value = value.toUpperCase();

        // 基本格式验证
        if (!ID_CARD_PATTERN.matcher(value).matches()) {
            return false;
        }

        // 验证地区码（前6位）
        if (!isValidAreaCode(value.substring(0, 6))) {
            return false;
        }

        // 验证出生日期（第7-14位）
        if (!isValidBirthDate(value.substring(6, 14))) {
            return false;
        }

        // 验证校验码（第18位）
        return isValidCheckCode(value);
    }

    /**
     * 验证地区码是否有效
     *
     * @param areaCode 地区码
     * @return 是否有效
     */
    private boolean isValidAreaCode(String areaCode) {
        // 前两位表示省份，不能为00
        int provinceCode = Integer.parseInt(areaCode.substring(0, 2));
        if (provinceCode < 11 || provinceCode > 91) {
            return false;
        }

        // 省份代码的有效范围
        int[] validProvinceCodes = {11, 12, 13, 14, 15, 21, 22, 23, 31, 32, 33, 34, 35, 36, 37,
                                    41, 42, 43, 44, 45, 46, 50, 51, 52, 53, 54, 61, 62, 63, 64, 65, 71, 81, 82, 91};

        boolean isValidProvince = false;
        for (int validCode : validProvinceCodes) {
            if (provinceCode == validCode) {
                isValidProvince = true;
                break;
            }
        }

        return isValidProvince;
    }

    /**
     * 验证出生日期是否有效
     *
     * @param birthDate 出生日期字符串
     * @return 是否有效
     */
    private boolean isValidBirthDate(String birthDate) {
        try {
            LocalDate date = LocalDate.parse(birthDate, DATE_FORMATTER);
            LocalDate now = LocalDate.now();

            // 出生日期不能晚于当前日期
            if (date.isAfter(now)) {
                return false;
            }

            // 出生日期不能早于1900年
            if (date.getYear() < 1900) {
                return false;
            }

            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证校验码是否正确
     *
     * @param idCard 身份证号
     * @return 是否正确
     */
    private boolean isValidCheckCode(String idCard) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHT_FACTORS[i];
        }

        int checkCodeIndex = sum % 11;
        char expectedCheckCode = CHECK_CODES[checkCodeIndex];
        char actualCheckCode = idCard.charAt(17);

        return expectedCheckCode == actualCheckCode;
    }
}