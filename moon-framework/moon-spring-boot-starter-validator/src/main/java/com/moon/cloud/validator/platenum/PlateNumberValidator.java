package com.moon.cloud.validator.platenum;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 车牌号验证器实现（中国车牌）
 */
public class PlateNumberValidator implements ConstraintValidator<PlateNumber, String> {

    // 普通车牌（含港澳车牌）
    private static final Pattern NORMAL_PATTERN = Pattern.compile(
        "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]$"
    );

    // 新能源车牌（8位）
    private static final Pattern NEW_ENERGY_PATTERN = Pattern.compile(
        "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z]([0-9]{5}[DF]|[DF][A-HJ-NP-Z0-9][0-9]{4})$"
    );

    // 警车车牌
    private static final Pattern POLICE_PATTERN = Pattern.compile(
        "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][0-9]{4}[警]$"
    );

    // 军车车牌
    private static final Pattern MILITARY_PATTERN = Pattern.compile(
        "^[军空海北沈兰济南广成][A-Z][0-9]{5}$"
    );

    private PlateNumber.PlateType type;

    @Override
    public void initialize(PlateNumber constraintAnnotation) {
        this.type = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // 转换为大写
        value = value.toUpperCase();

        switch (type) {
            case NORMAL:
                return NORMAL_PATTERN.matcher(value).matches();
            case NEW_ENERGY:
                return NEW_ENERGY_PATTERN.matcher(value).matches();
            case POLICE:
                return POLICE_PATTERN.matcher(value).matches();
            case MILITARY:
                return MILITARY_PATTERN.matcher(value).matches();
            case ALL:
            default:
                return NORMAL_PATTERN.matcher(value).matches() ||
                       NEW_ENERGY_PATTERN.matcher(value).matches() ||
                       POLICE_PATTERN.matcher(value).matches() ||
                       MILITARY_PATTERN.matcher(value).matches();
        }
    }
}