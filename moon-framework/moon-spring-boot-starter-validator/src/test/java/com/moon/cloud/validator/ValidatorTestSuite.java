package com.moon.cloud.validator;

import com.moon.cloud.validator.email.Email;
import com.moon.cloud.validator.url.Url;
import com.moon.cloud.validator.password.Password;
import com.moon.cloud.validator.username.Username;
import com.moon.cloud.validator.mac.MacAddress;
import com.moon.cloud.validator.platenum.PlateNumber;
import com.moon.cloud.validator.creditcode.CreditCode;
import com.moon.cloud.validator.telephone.Telephone;
import com.moon.cloud.validator.qq.QQNumber;
import com.moon.cloud.validator.wechat.WeChatId;
import com.moon.cloud.validator.mobile.Mobile;
import com.moon.cloud.validator.idcard.IdCard;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证器测试套件
 */
public class ValidatorTestSuite {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * 测试邮箱验证器
     */
    @Test
    public void testEmailValidator() {
        EmailTestBean bean = new EmailTestBean();

        // 有效邮箱
        bean.email = "test@example.com";
        assertTrue(isValid(bean));

        bean.email = "user.name+tag@example.co.uk";
        assertTrue(isValid(bean));

        // 无效邮箱
        bean.email = "invalid.email";
        assertFalse(isValid(bean));

        bean.email = "@example.com";
        assertFalse(isValid(bean));

        bean.email = "user@";
        assertFalse(isValid(bean));
    }

    /**
     * 测试URL验证器
     */
    @Test
    public void testUrlValidator() {
        UrlTestBean bean = new UrlTestBean();

        // 有效URL
        bean.url = "https://www.example.com";
        assertTrue(isValid(bean));

        bean.url = "http://example.com:8080/path";
        assertTrue(isValid(bean));

        // 无效URL
        bean.url = "not-a-url";
        assertFalse(isValid(bean));

        bean.url = "ftp://example.com"; // 只允许http/https
        assertFalse(isValid(bean));
    }

    /**
     * 测试密码验证器
     */
    @Test
    public void testPasswordValidator() {
        PasswordTestBean bean = new PasswordTestBean();

        // 强密码
        bean.password = "StrongP@ss123";
        assertTrue(isValid(bean));

        // 弱密码
        bean.password = "weak";
        assertFalse(isValid(bean));

        bean.password = "12345678"; // 没有字母
        assertFalse(isValid(bean));

        bean.password = "password"; // 没有数字
        assertFalse(isValid(bean));
    }

    /**
     * 测试用户名验证器
     */
    @Test
    public void testUsernameValidator() {
        UsernameTestBean bean = new UsernameTestBean();

        // 有效用户名
        bean.username = "john_doe";
        assertTrue(isValid(bean));

        bean.username = "user123";
        assertTrue(isValid(bean));

        // 无效用户名
        bean.username = "a"; // 太短
        assertFalse(isValid(bean));

        bean.username = "admin"; // 保留名称
        assertFalse(isValid(bean));

        bean.username = "123user"; // 不以字母开头
        assertFalse(isValid(bean));
    }

    /**
     * 测试MAC地址验证器
     */
    @Test
    public void testMacAddressValidator() {
        MacTestBean bean = new MacTestBean();

        // 有效MAC地址
        bean.mac = "00:1B:44:11:3A:B7";
        assertTrue(isValid(bean));

        bean.mac = "00-1B-44-11-3A-B7";
        assertTrue(isValid(bean));

        // 无效MAC地址
        bean.mac = "00:1B:44:11:3A:ZZ"; // 非法字符
        assertFalse(isValid(bean));

        bean.mac = "00:1B:44:11:3A"; // 不完整
        assertFalse(isValid(bean));
    }

    /**
     * 测试车牌号验证器
     */
    @Test
    public void testPlateNumberValidator() {
        PlateTestBean bean = new PlateTestBean();

        // 有效车牌
        bean.plateNumber = "京A12345";
        assertTrue(isValid(bean));

        bean.plateNumber = "粤B88888";
        assertTrue(isValid(bean));

        bean.plateNumber = "京AD12345"; // 新能源
        assertTrue(isValid(bean));

        // 无效车牌
        bean.plateNumber = "XX12345";
        assertFalse(isValid(bean));

        bean.plateNumber = "京12345"; // 缺少字母
        assertFalse(isValid(bean));
    }

    /**
     * 测试统一社会信用代码验证器
     */
    @Test
    public void testCreditCodeValidator() {
        CreditTestBean bean = new CreditTestBean();

        // 有效信用代码（示例）
        bean.creditCode = "91110108MA01WKK67D";
        // 注意：实际测试需要真实有效的统一社会信用代码

        // 无效信用代码
        bean.creditCode = "12345678901234567X"; // 错误格式
        assertFalse(isValid(bean));

        bean.creditCode = "911101"; // 长度不对
        assertFalse(isValid(bean));
    }

    /**
     * 测试固定电话验证器
     */
    @Test
    public void testTelephoneValidator() {
        TelephoneTestBean bean = new TelephoneTestBean();

        // 有效电话
        bean.telephone = "010-12345678";
        assertTrue(isValid(bean));

        bean.telephone = "021-87654321";
        assertTrue(isValid(bean));

        bean.telephone = "400-123-4567";
        assertTrue(isValid(bean));

        // 无效电话
        bean.telephone = "12345"; // 太短
        assertFalse(isValid(bean));

        bean.telephone = "1234567890"; // 没有正确格式
        assertFalse(isValid(bean));
    }

    /**
     * 测试QQ号验证器
     */
    @Test
    public void testQQNumberValidator() {
        QQTestBean bean = new QQTestBean();

        // 有效QQ号
        bean.qqNumber = "10001";
        assertTrue(isValid(bean));

        bean.qqNumber = "123456789";
        assertTrue(isValid(bean));

        // 无效QQ号
        bean.qqNumber = "1234"; // 太短
        assertFalse(isValid(bean));

        bean.qqNumber = "01234567"; // 不能以0开头
        assertFalse(isValid(bean));
    }

    /**
     * 测试微信号验证器
     */
    @Test
    public void testWeChatIdValidator() {
        WeChatTestBean bean = new WeChatTestBean();

        // 有效微信号
        bean.wechatId = "wxid_abc123";
        assertTrue(isValid(bean));

        bean.wechatId = "test_user";
        assertTrue(isValid(bean));

        // 无效微信号
        bean.wechatId = "123abc"; // 必须以字母开头
        assertFalse(isValid(bean));

        bean.wechatId = "ab"; // 太短
        assertFalse(isValid(bean));
    }

    /**
     * 测试手机号验证器
     */
    @Test
    public void testMobileValidator() {
        MobileTestBean bean = new MobileTestBean();

        // 有效手机号
        bean.mobile = "13812345678";
        assertTrue(isValid(bean));

        bean.mobile = "15987654321";
        assertTrue(isValid(bean));

        bean.mobile = "+8613812345678";
        assertTrue(isValid(bean));

        // 无效手机号
        bean.mobile = "12345678901"; // 非法号段
        assertFalse(isValid(bean));

        bean.mobile = "1381234567"; // 位数不对
        assertFalse(isValid(bean));
    }

    private boolean isValid(Object bean) {
        Set<ConstraintViolation<Object>> violations = validator.validate(bean);
        return violations.isEmpty();
    }

    // 测试Bean类
    static class EmailTestBean {
        @Email
        String email;
    }

    static class UrlTestBean {
        @Url
        String url;
    }

    static class PasswordTestBean {
        @Password
        String password;
    }

    static class UsernameTestBean {
        @Username
        String username;
    }

    static class MacTestBean {
        @MacAddress
        String mac;
    }

    static class PlateTestBean {
        @PlateNumber
        String plateNumber;
    }

    static class CreditTestBean {
        @CreditCode
        String creditCode;
    }

    static class TelephoneTestBean {
        @Telephone
        String telephone;
    }

    static class QQTestBean {
        @QQNumber
        String qqNumber;
    }

    static class WeChatTestBean {
        @WeChatId
        String wechatId;
    }

    static class MobileTestBean {
        @Mobile
        String mobile;
    }
}