package com.moon.cloud.validator.plate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 车牌号验证器实现
 * 支持中国大陆各种类型的车牌号
 */
public class PlateNumberValidator implements ConstraintValidator<PlateNumber, String> {

    /**
     * 普通车牌（蓝牌、黄牌）
     * 格式：省份简称 + 城市代码 + 5位字符（字母数字）
     */
    private static final Pattern NORMAL_PLATE_PATTERN = Pattern.compile(
        "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]$"
    );

    /**
     * 新能源车牌（小型车）
     * 格式：省份简称 + 城市代码 + D/F + 5位数字
     */
    private static final Pattern NEW_ENERGY_SMALL_PATTERN = Pattern.compile(
        "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][DF][A-HJ-NP-Z0-9][0-9]{4}$"
    );

    /**
     * 新能源车牌（大型车）
     * 格式：省份简称 + 城市代码 + 5位数字 + D/F
     */
    private static final Pattern NEW_ENERGY_LARGE_PATTERN = Pattern.compile(
        "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][0-9]{5}[DF]$"
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

        // 转换为大写，去除空格
        value = value.toUpperCase().replaceAll("\\s+", "");

        switch (type) {
            case NORMAL:
                return isNormalPlate(value);
            case NEW_ENERGY:
                return isNewEnergyPlate(value);
            case ALL:
            default:
                return isNormalPlate(value) || isNewEnergyPlate(value);
        }
    }

    /**
     * 验证普通车牌
     */
    private boolean isNormalPlate(String plateNumber) {
        return NORMAL_PLATE_PATTERN.matcher(plateNumber).matches();
    }

    /**
     * 验证新能源车牌
     */
    private boolean isNewEnergyPlate(String plateNumber) {
        return NEW_ENERGY_SMALL_PATTERN.matcher(plateNumber).matches() ||
               NEW_ENERGY_LARGE_PATTERN.matcher(plateNumber).matches();
    }

    /**
     * 获取车牌类型描述
     */
    public static String getPlateType(String plateNumber) {
        if (StringUtils.isEmpty(plateNumber)) {
            return "未知";
        }

        plateNumber = plateNumber.toUpperCase().replaceAll("\\s+", "");

        if (NORMAL_PLATE_PATTERN.matcher(plateNumber).matches()) {
            return "普通车牌";
        } else if (NEW_ENERGY_SMALL_PATTERN.matcher(plateNumber).matches()) {
            return "新能源小型车";
        } else if (NEW_ENERGY_LARGE_PATTERN.matcher(plateNumber).matches()) {
            return "新能源大型车";
        }

        return "未知";
    }
}