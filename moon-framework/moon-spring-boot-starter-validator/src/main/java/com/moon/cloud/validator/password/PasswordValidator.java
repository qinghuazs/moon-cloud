package com.moon.cloud.validator.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * 密码强度验证器实现
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;
    private String specialChars;
    private Password.PasswordStrength strength;

    @Override
    public void initialize(Password constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
        this.specialChars = constraintAnnotation.specialChars();
        this.strength = constraintAnnotation.strength();

        // 根据强度等级设置默认要求
        applyStrengthDefaults();
    }

    private void applyStrengthDefaults() {
        switch (strength) {
            case WEAK:
                if (minLength < 6) minLength = 6;
                requireUppercase = false;
                requireLowercase = false;
                requireSpecialChar = false;
                break;
            case MEDIUM:
                if (minLength < 8) minLength = 8;
                requireUppercase = true;
                requireLowercase = true;
                requireDigit = true;
                requireSpecialChar = false;
                break;
            case STRONG:
                if (minLength < 10) minLength = 10;
                requireUppercase = true;
                requireLowercase = true;
                requireDigit = true;
                requireSpecialChar = true;
                break;
        }
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

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (specialChars.indexOf(c) >= 0) {
                hasSpecialChar = true;
            }
        }

        // 根据要求验证
        if (requireUppercase && !hasUppercase) {
            return false;
        }
        if (requireLowercase && !hasLowercase) {
            return false;
        }
        if (requireDigit && !hasDigit) {
            return false;
        }
        if (requireSpecialChar && !hasSpecialChar) {
            return false;
        }

        // 对于弱密码，至少需要字母或数字
        if (strength == Password.PasswordStrength.WEAK) {
            return hasUppercase || hasLowercase || hasDigit;
        }

        return true;
    }
}