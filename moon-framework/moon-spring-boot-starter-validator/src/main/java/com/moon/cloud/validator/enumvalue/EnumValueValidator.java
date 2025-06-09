package com.moon.cloud.validator.enumvalue;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 枚举值验证器实现
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private Set<String> enumValues;
    private boolean ignoreCase;
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
        
        // 获取枚举类中所有的枚举值名称
        this.enumValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .map(name -> ignoreCase ? name.toLowerCase() : name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 为空时不进行验证，交给@NotNull或@NotEmpty处理
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        
        String valueToCheck = ignoreCase ? value.toLowerCase() : value;
        
        if (!enumValues.contains(valueToCheck)) {
            // 自定义错误消息，包含可用的枚举值
            String availableValues = Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("提供的值 '%s' 不在枚举类 %s 中，可用值: [%s]", 
                            value, enumClass.getSimpleName(), availableValues))
                    .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}