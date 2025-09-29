package com.moon.cloud.validator.username;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 用户名验证器实现
 */
public class UsernameValidator implements ConstraintValidator<Username, String> {

    private int minLength;
    private int maxLength;
    private boolean allowChinese;
    private boolean allowSpecialChar;
    private String specialChars;
    private boolean mustStartWithLetter;
    private Set<String> reservedNames;
    private Pattern pattern;

    @Override
    public void initialize(Username constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.allowChinese = constraintAnnotation.allowChinese();
        this.allowSpecialChar = constraintAnnotation.allowSpecialChar();
        this.specialChars = constraintAnnotation.specialChars();
        this.mustStartWithLetter = constraintAnnotation.mustStartWithLetter();
        this.reservedNames = new HashSet<>(Arrays.asList(constraintAnnotation.reservedNames()));

        // 构建正则表达式
        String patternStr = buildPattern();
        this.pattern = Pattern.compile(patternStr);
    }

    private String buildPattern() {
        StringBuilder sb = new StringBuilder("^");

        // 开头字符
        if (mustStartWithLetter) {
            sb.append("[a-zA-Z");
            if (allowChinese) {
                sb.append("\\u4e00-\\u9fa5");
            }
            sb.append("]");
        } else {
            sb.append("[a-zA-Z0-9");
            if (allowChinese) {
                sb.append("\\u4e00-\\u9fa5");
            }
            if (allowSpecialChar && !specialChars.isEmpty()) {
                sb.append(Pattern.quote(specialChars));
            }
            sb.append("]");
        }

        // 后续字符
        sb.append("[a-zA-Z0-9");
        if (allowChinese) {
            sb.append("\\u4e00-\\u9fa5");
        }
        if (allowSpecialChar && !specialChars.isEmpty()) {
            for (char c : specialChars.toCharArray()) {
                sb.append("\\").append(c);
            }
        }
        sb.append("]*$");

        return sb.toString();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // 长度检查
        if (value.length() < minLength || value.length() > maxLength) {
            return false;
        }

        // 保留用户名检查
        if (reservedNames.contains(value.toLowerCase())) {
            return false;
        }

        // 格式检查
        return pattern.matcher(value).matches();
    }
}