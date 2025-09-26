package com.moon.cloud.user.service.impl;

import com.moon.cloud.user.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@mooncloud.com}")
    private String from;

    @Value("${spring.application.name:Moon Cloud}")
    private String appName;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(appName + " - 密码重置验证码");

            String content = buildVerificationCodeEmail(code);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", to);
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败: {}", to, e);
            throw new RuntimeException("发送邮件失败，请稍后重试");
        }
    }

    @Override
    public void sendPasswordResetNotification(String to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(appName + " - 密码重置成功");

            String content = buildPasswordResetNotificationEmail();
            helper.setText(content, true);

            mailSender.send(message);
            log.info("密码重置通知邮件已发送至: {}", to);
        } catch (MessagingException e) {
            log.error("发送密码重置通知邮件失败: {}", to, e);
        }
    }

    private String buildVerificationCodeEmail(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .code-box { background: white; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .code { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    .warning { background: #fff3cd; color: #856404; padding: 10px; border-radius: 5px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <p>密码重置验证码</p>
                    </div>
                    <div class="content">
                        <p>您好！</p>
                        <p>您正在申请重置密码。请使用以下验证码完成密码重置：</p>
                        <div class="code-box">
                            <div class="code">%s</div>
                        </div>
                        <p>验证码有效期为 <strong>10分钟</strong>，请尽快使用。</p>
                        <div class="warning">
                            ⚠️ 安全提醒：如果这不是您本人的操作，请忽略此邮件。切勿将验证码告知他人。
                        </div>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>&copy; 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, code, appName);
    }

    private String buildPasswordResetNotificationEmail() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-box { background: #d4edda; color: #155724; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    .security-tips { background: white; padding: 15px; border-radius: 5px; margin-top: 20px; }
                    .security-tips h3 { color: #667eea; margin-bottom: 10px; }
                    .security-tips ul { margin: 0; padding-left: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <p>密码重置成功</p>
                    </div>
                    <div class="content">
                        <p>您好！</p>
                        <div class="success-box">
                            ✅ 您的密码已成功重置
                        </div>
                        <p>您的账户密码已经成功更改。现在您可以使用新密码登录系统。</p>

                        <div class="security-tips">
                            <h3>🔒 安全建议</h3>
                            <ul>
                                <li>请妥善保管您的新密码</li>
                                <li>建议定期更换密码以确保账户安全</li>
                                <li>不要将密码告知他人或在公共场所输入</li>
                                <li>如果这不是您本人的操作，请立即联系我们</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿回复</p>
                        <p>&copy; 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, appName);
    }
}