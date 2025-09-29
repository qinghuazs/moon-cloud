package com.moon.cloud.validator.example;

import com.moon.cloud.validator.bankcard.BankCard;
import com.moon.cloud.validator.bloodtype.BloodType;
import com.moon.cloud.validator.cvv.CVV;
import com.moon.cloud.validator.email.Email;
import com.moon.cloud.validator.idcard.IdCard;
import com.moon.cloud.validator.ip.IpAddress;
import com.moon.cloud.validator.isbn.ISBN;
import com.moon.cloud.validator.mobile.Mobile;
import com.moon.cloud.validator.nickname.ChineseNickname;
import com.moon.cloud.validator.postcode.PostCode;
import com.moon.cloud.validator.url.Url;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户数据传输对象示例
 * 演示如何使用自定义验证器
 */
@Data
public class UserDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @ChineseNickname(message = "用户名必须是合法的中文昵称")
    private String username;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Mobile(message = "请输入正确的手机号码")
    private String mobile;

    /**
     * 邮箱地址
     */
    @Email(message = "请输入正确的邮箱地址")
    private String email;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @IdCard(message = "请输入正确的身份证号码")
    private String idCard;

    /**
     * 银行卡号
     */
    @BankCard(message = "请输入正确的银行卡号")
    private String bankCard;

    /**
     * CVV码
     */
    @CVV(message = "请输入正确的CVV码")
    private String cvv;

    /**
     * IP地址
     */
    @IpAddress(message = "请输入正确的IP地址")
    private String ipAddress;

    /**
     * 个人主页URL
     */
    @Url(message = "请输入正确的URL地址")
    private String homepage;

    /**
     * 邮政编码
     */
    @PostCode(message = "请输入正确的邮政编码")
    private String postCode;

    /**
     * 血型
     */
    @BloodType(message = "请输入正确的血型")
    private String bloodType;

    /**
     * 图书ISBN号
     */
    @ISBN(message = "请输入正确的ISBN号")
    private String isbn;

    /**
     * 备用邮箱（支持中文域名）
     */
    @Email(allowChinese = true, message = "请输入正确的邮箱地址")
    private String backupEmail;

    /**
     * 公司邮箱（不允许子域名）
     */
    @Email(allowSubdomain = false, message = "公司邮箱格式不正确")
    private String companyEmail;
}