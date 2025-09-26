package com.moon.cloud.user.service.impl;

import com.moon.cloud.user.event.EmailEvent;
import com.moon.cloud.user.exception.BusinessException;
import com.moon.cloud.user.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 基于Kafka的邮件服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
@Service("kafkaEmailService")
@ConditionalOnProperty(name = "email.service.type", havingValue = "kafka", matchIfMissing = true)
public class KafkaEmailServiceImpl implements EmailService {

    @Autowired
    private KafkaTemplate<String, EmailEvent> emailEventKafkaTemplate;

    @Value("${kafka.topics.email:email-events}")
    private String emailTopic;

    @Value("${spring.application.name:Moon Cloud}")
    private String appName;

    @Override
    public void sendVerificationCode(String to, String code) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("code", code);
        templateParams.put("appName", appName);
        templateParams.put("expirationMinutes", 10);

        EmailEvent event = EmailEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .emailType(EmailEvent.EmailType.VERIFICATION_CODE)
                .to(to)
                .subject(appName + " - 密码重置验证码")
                .templateParams(templateParams)
                .createdAt(LocalDateTime.now())
                .build();

        sendEmailEvent(event);
    }

    @Override
    public void sendPasswordResetNotification(String to) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("appName", appName);
        templateParams.put("timestamp", LocalDateTime.now());

        EmailEvent event = EmailEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .emailType(EmailEvent.EmailType.PASSWORD_RESET_SUCCESS)
                .to(to)
                .subject(appName + " - 密码重置成功")
                .templateParams(templateParams)
                .createdAt(LocalDateTime.now())
                .build();

        sendEmailEvent(event);
    }

    /**
     * 发送邮件事件到Kafka
     */
    private void sendEmailEvent(EmailEvent event) {
        try {
            CompletableFuture<SendResult<String, EmailEvent>> future =
                emailEventKafkaTemplate.send(emailTopic, event.getEventId(), event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送邮件事件到Kafka失败: eventId={}, to={}",
                        event.getEventId(), event.getTo(), ex);
                } else {
                    log.info("邮件事件已发送到Kafka: eventId={}, to={}, partition={}, offset={}",
                        event.getEventId(), event.getTo(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("发送邮件事件异常: eventId={}, to={}", event.getEventId(), event.getTo(), e);
            throw new BusinessException("发送邮件失败，请稍后重试");
        }
    }
}