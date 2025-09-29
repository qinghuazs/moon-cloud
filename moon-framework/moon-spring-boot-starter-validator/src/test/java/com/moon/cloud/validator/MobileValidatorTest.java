package com.moon.cloud.validator;

import com.moon.cloud.validator.mobile.MobileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 手机号验证器测试类
 */
@DisplayName("手机号验证器测试")
class MobileValidatorTest {

    private MobileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MobileValidator();
        validator.initialize(null);
    }

    @Test
    @DisplayName("验证空值应返回true")
    void testNullValue() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @Test
    @DisplayName("验证有效的中国移动号码")
    void testValidChinaMobileNumbers() {
        assertTrue(validator.isValid("13812345678", null));
        assertTrue(validator.isValid("13912345678", null));
        assertTrue(validator.isValid("14712345678", null));
        assertTrue(validator.isValid("14812345678", null));
        assertTrue(validator.isValid("15012345678", null));
        assertTrue(validator.isValid("15912345678", null));
        assertTrue(validator.isValid("17212345678", null));
        assertTrue(validator.isValid("17812345678", null));
        assertTrue(validator.isValid("18212345678", null));
        assertTrue(validator.isValid("19512345678", null));
        assertTrue(validator.isValid("19812345678", null));
    }

    @Test
    @DisplayName("验证有效的中国联通号码")
    void testValidChinaUnicomNumbers() {
        assertTrue(validator.isValid("13012345678", null));
        assertTrue(validator.isValid("13112345678", null));
        assertTrue(validator.isValid("13212345678", null));
        assertTrue(validator.isValid("14512345678", null));
        assertTrue(validator.isValid("14612345678", null));
        assertTrue(validator.isValid("15512345678", null));
        assertTrue(validator.isValid("15612345678", null));
        assertTrue(validator.isValid("16612345678", null));
        assertTrue(validator.isValid("17512345678", null));
        assertTrue(validator.isValid("17612345678", null));
        assertTrue(validator.isValid("18512345678", null));
        assertTrue(validator.isValid("18612345678", null));
        assertTrue(validator.isValid("19612345678", null));
    }

    @Test
    @DisplayName("验证有效的中国电信号码")
    void testValidChinaTelecomNumbers() {
        assertTrue(validator.isValid("13312345678", null));
        assertTrue(validator.isValid("14912345678", null));
        assertTrue(validator.isValid("15312345678", null));
        assertTrue(validator.isValid("17312345678", null));
        assertTrue(validator.isValid("17712345678", null));
        assertTrue(validator.isValid("18012345678", null));
        assertTrue(validator.isValid("18112345678", null));
        assertTrue(validator.isValid("18912345678", null));
        assertTrue(validator.isValid("19012345678", null));
        assertTrue(validator.isValid("19112345678", null));
        assertTrue(validator.isValid("19312345678", null));
        assertTrue(validator.isValid("19912345678", null));
    }

    @Test
    @DisplayName("验证有效的中国广电号码")
    void testValidChinaBroadcastNumbers() {
        assertTrue(validator.isValid("19212345678", null));
    }

    @Test
    @DisplayName("验证带+86前缀的手机号")
    void testMobileWithCountryCode() {
        assertTrue(validator.isValid("+8613812345678", null));
        assertTrue(validator.isValid("8613812345678", null));
    }

    @Test
    @DisplayName("验证带空格和连字符的手机号")
    void testMobileWithSpacesAndDashes() {
        assertTrue(validator.isValid("138 1234 5678", null));
        assertTrue(validator.isValid("138-1234-5678", null));
    }

    @Test
    @DisplayName("验证无效的手机号")
    void testInvalidMobileNumbers() {
        assertFalse(validator.isValid("12345678901", null));  // 不是1开头
        assertFalse(validator.isValid("1381234567", null));   // 位数不够
        assertFalse(validator.isValid("138123456789", null)); // 位数太多
        assertFalse(validator.isValid("11012345678", null));  // 无效的第二位
        assertFalse(validator.isValid("12012345678", null));  // 无效的第二位
        assertFalse(validator.isValid("abcdefghijk", null));  // 非数字
    }

    @Test
    @DisplayName("测试获取运营商信息")
    void testGetCarrier() {
        assertEquals("中国移动", MobileValidator.getCarrier("13812345678"));
        assertEquals("中国移动", MobileValidator.getCarrier("14712345678"));
        assertEquals("中国移动", MobileValidator.getCarrier("15012345678"));
        assertEquals("中国移动", MobileValidator.getCarrier("17812345678"));
        assertEquals("中国移动", MobileValidator.getCarrier("19812345678"));

        assertEquals("中国联通", MobileValidator.getCarrier("13012345678"));
        assertEquals("中国联通", MobileValidator.getCarrier("14512345678"));
        assertEquals("中国联通", MobileValidator.getCarrier("15512345678"));
        assertEquals("中国联通", MobileValidator.getCarrier("18612345678"));

        assertEquals("中国电信", MobileValidator.getCarrier("13312345678"));
        assertEquals("中国电信", MobileValidator.getCarrier("14912345678"));
        assertEquals("中国电信", MobileValidator.getCarrier("18012345678"));
        assertEquals("中国电信", MobileValidator.getCarrier("19912345678"));

        assertEquals("中国广电", MobileValidator.getCarrier("19212345678"));

        assertEquals("未知", MobileValidator.getCarrier("12345678901"));
        assertEquals("未知", MobileValidator.getCarrier(null));
        assertEquals("未知", MobileValidator.getCarrier(""));
    }

    @Test
    @DisplayName("测试带国际区号的运营商识别")
    void testGetCarrierWithCountryCode() {
        assertEquals("中国移动", MobileValidator.getCarrier("+8613812345678"));
        assertEquals("中国联通", MobileValidator.getCarrier("8613012345678"));
        assertEquals("中国电信", MobileValidator.getCarrier("+8618912345678"));
    }
}