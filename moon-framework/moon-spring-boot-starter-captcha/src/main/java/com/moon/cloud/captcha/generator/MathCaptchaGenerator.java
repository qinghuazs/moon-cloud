package com.moon.cloud.captcha.generator;

import com.moon.cloud.captcha.config.CaptchaProperties;
import com.moon.cloud.captcha.core.Captcha;
import com.moon.cloud.captcha.enums.CaptchaType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 算术验证码生成器
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Component
public class MathCaptchaGenerator extends AbstractCaptchaGenerator {

    @Override
    public Captcha generate(CaptchaProperties properties) {
        CaptchaProperties.Math mathConfig = properties.getMath();
        String operators = mathConfig.getOperators();
        int minNumber = mathConfig.getMinNumber();
        int maxNumber = mathConfig.getMaxNumber();

        // 生成两个随机数
        int num1 = SECURE_RANDOM.nextInt(maxNumber - minNumber + 1) + minNumber;
        int num2 = SECURE_RANDOM.nextInt(maxNumber - minNumber + 1) + minNumber;

        // 随机选择运算符
        char operator = operators.charAt(SECURE_RANDOM.nextInt(operators.length()));

        // 计算答案
        int answer;
        String equation;

        switch (operator) {
            case '+':
                answer = num1 + num2;
                equation = String.format("%d + %d", num1, num2);
                break;
            case '-':
                // 确保结果为正数
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                answer = num1 - num2;
                equation = String.format("%d - %d", num1, num2);
                break;
            case '*':
                answer = num1 * num2;
                equation = String.format("%d × %d", num1, num2);
                break;
            case '/':
                // 确保能够整除
                num1 = num2 * (SECURE_RANDOM.nextInt(maxNumber / 2) + 1);
                answer = num1 / num2;
                equation = String.format("%d ÷ %d", num1, num2);
                break;
            default:
                answer = num1 + num2;
                equation = String.format("%d + %d", num1, num2);
        }

        // 是否显示等式
        String code = mathConfig.isShowEquation() ? equation + " = ?" : equation;

        return Captcha.builder()
                .id(UUID.randomUUID().toString())
                .code(code)
                .answer(String.valueOf(answer))
                .type(CaptchaType.MATH)
                .createTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusSeconds(properties.getExpireTime()))
                .failCount(0)
                .used(false)
                .build();
    }

    @Override
    protected String generateCode(CaptchaProperties properties) {
        // 不使用此方法
        return null;
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.MATH;
    }
}