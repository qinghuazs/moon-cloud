package com.moon.cloud.validator.mobile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 手机号验证器实现
 * 支持中国大陆所有运营商的手机号段验证
 *
 * 号段分配：
 * - 中国移动：134-139, 147, 148, 150-152, 157-159, 165, 172, 178, 182-184, 187-188, 195, 197, 198
 * - 中国联通：130-132, 145, 146, 155-156, 166, 167, 171, 175-176, 185-186, 196
 * - 中国电信：133, 149, 153, 173-174, 177, 180-181, 189, 190, 191, 193, 199
 * - 中国广电：192
 * - 虚拟运营商：162, 165, 167, 170, 171
 */
public class MobileValidator implements ConstraintValidator<Mobile, String> {

    /**
     * 更精确的手机号验证正则表达式
     * 包含2024年最新的号段
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "^1(3[0-9]|4[01456789]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])\\d{8}$"
    );

    /**
     * 中国移动号段
     */
    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile(
        "^1(3[4-9]|4[7-8]|5[0-2]|5[7-9]|65|72|78|8[2-4]|8[7-8]|95|97|98)\\d{8}$"
    );

    /**
     * 中国联通号段
     */
    private static final Pattern CHINA_UNICOM_PATTERN = Pattern.compile(
        "^1(3[0-2]|4[5-6]|5[5-6]|6[67]|7[1]|7[5-6]|8[5-6]|96)\\d{8}$"
    );

    /**
     * 中国电信号段
     */
    private static final Pattern CHINA_TELECOM_PATTERN = Pattern.compile(
        "^1(33|49|53|7[37]|8[019]|9[0139])\\d{8}$"
    );

    /**
     * 中国广电号段
     */
    private static final Pattern CHINA_BROADCAST_PATTERN = Pattern.compile(
        "^192\\d{8}$"
    );

    @Override
    public void initialize(Mobile constraintAnnotation) {
        // 初始化，如果需要的话
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // 去除可能的空格和连字符
        value = value.replaceAll("[\\s-]", "");

        // 支持+86前缀
        if (value.startsWith("+86")) {
            value = value.substring(3);
        } else if (value.startsWith("86") && value.length() == 13) {
            value = value.substring(2);
        }

        // 验证手机号格式
        return MOBILE_PATTERN.matcher(value).matches();
    }

    /**
     * 获取运营商类型
     *
     * @param mobile 手机号
     * @return 运营商名称，如果无法识别返回"未知"
     */
    public static String getCarrier(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return "未知";
        }

        // 清理手机号
        mobile = mobile.replaceAll("[\\s-]", "");
        if (mobile.startsWith("+86")) {
            mobile = mobile.substring(3);
        } else if (mobile.startsWith("86") && mobile.length() == 13) {
            mobile = mobile.substring(2);
        }

        if (CHINA_MOBILE_PATTERN.matcher(mobile).matches()) {
            return "中国移动";
        } else if (CHINA_UNICOM_PATTERN.matcher(mobile).matches()) {
            return "中国联通";
        } else if (CHINA_TELECOM_PATTERN.matcher(mobile).matches()) {
            return "中国电信";
        } else if (CHINA_BROADCAST_PATTERN.matcher(mobile).matches()) {
            return "中国广电";
        }

        return "未知";
    }
}