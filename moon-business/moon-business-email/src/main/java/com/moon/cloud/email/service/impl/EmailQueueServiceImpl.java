package com.moon.cloud.email.service.impl;

import com.moon.cloud.email.config.EmailConfig;
import com.moon.cloud.email.entity.EmailRecord;
import com.moon.cloud.email.enums.EmailStatus;
import com.moon.cloud.email.mapper.EmailRecordMapper;
import com.moon.cloud.email.service.EmailQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 邮件队列服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class EmailQueueServiceImpl implements EmailQueueService {

    private static final String EMAIL_QUEUE_KEY = "email:queue";
    private static final String EMAIL_PROCESSING_KEY = "email:processing";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EmailRecordMapper emailRecordMapper;

    @Autowired
    private EmailConfig emailConfig;

    @Override
    public boolean enqueue(EmailRecord emailRecord) {
        try {
            if (!emailConfig.getEnableQueue()) {
                return false;
            }

            // 检查队列大小限制
            Long queueSize = redisTemplate.opsForList().size(EMAIL_QUEUE_KEY);
            if (queueSize != null && queueSize >= emailConfig.getQueueMaxSize()) {
                throw new RuntimeException("邮件队列已满，无法添加新邮件");
            }

            // 将邮件记录ID加入队列
            redisTemplate.opsForList().rightPush(EMAIL_QUEUE_KEY, emailRecord.getId());
            return true;
        } catch (Exception e) {
            System.err.println("邮件入队失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public EmailRecord dequeue() {
        try {
            // 从队列左侧取出邮件ID
            Object emailId = redisTemplate.opsForList().leftPop(EMAIL_QUEUE_KEY);
            if (emailId == null) {
                return null;
            }

            // 将邮件ID加入处理中集合
            redisTemplate.opsForSet().add(EMAIL_PROCESSING_KEY, emailId);
            redisTemplate.expire(EMAIL_PROCESSING_KEY, 1, TimeUnit.HOURS);

            // 查询邮件记录
            return emailRecordMapper.selectById((Long) emailId);
        } catch (Exception e) {
            System.err.println("邮件出队失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int getQueueSize() {
        try {
            Long size = redisTemplate.opsForList().size(EMAIL_QUEUE_KEY);
            return size != null ? size.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean clearQueue() {
        try {
            redisTemplate.delete(EMAIL_QUEUE_KEY);
            redisTemplate.delete(EMAIL_PROCESSING_KEY);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Async
    public void processQueue() {
        if (!emailConfig.getEnableQueue()) {
            return;
        }

        try {
            while (getQueueSize() > 0) {
                EmailRecord emailRecord = dequeue();
                if (emailRecord == null) {
                    break;
                }

                // 处理邮件发送
                processEmailRecord(emailRecord);
            }
        } catch (Exception e) {
            System.err.println("处理邮件队列失败: " + e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void processScheduledEmails() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<EmailRecord> scheduledEmails = emailRecordMapper.selectScheduledEmails(now, 50);

            for (EmailRecord emailRecord : scheduledEmails) {
                // 更新状态为待发送
                emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.PENDING.getCode(), null);

                // 加入队列
                if (emailConfig.getEnableQueue()) {
                    enqueue(emailRecord);
                } else {
                    // 直接发送
                    processEmailRecord(emailRecord);
                }
            }
        } catch (Exception e) {
            System.err.println("处理定时邮件失败: " + e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    public void processRetryEmails() {
        try {
            Integer maxRetryCount = emailConfig.getRetryCount();
            Integer retryInterval = (int) (emailConfig.getRetryInterval() / 60000); // 转换为分钟

            List<EmailRecord> retryEmails = emailRecordMapper.selectRetryEmails(maxRetryCount, retryInterval, 20);

            for (EmailRecord emailRecord : retryEmails) {
                // 增加重试次数
                emailRecordMapper.incrementRetryCount(emailRecord.getId());

                // 加入队列重新发送
                if (emailConfig.getEnableQueue()) {
                    enqueue(emailRecord);
                } else {
                    // 直接发送
                    processEmailRecord(emailRecord);
                }
            }
        } catch (Exception e) {
            System.err.println("处理重试邮件失败: " + e.getMessage());
        }
    }

    @Override
    public Object getQueueStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            status.put("queueSize", getQueueSize());
            status.put("processingSize", redisTemplate.opsForSet().size(EMAIL_PROCESSING_KEY));
            status.put("queueEnabled", emailConfig.getEnableQueue());
            status.put("maxQueueSize", emailConfig.getQueueMaxSize());

            // 统计各状态邮件数量
            List<Map<String, Object>> statusStats = emailRecordMapper.selectStatusStatistics();
            status.put("statusStatistics", statusStats);

        } catch (Exception e) {
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * 处理单个邮件记录
     */
    private void processEmailRecord(EmailRecord emailRecord) {
        try {
            // 更新状态为发送中
            emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.SENDING.getCode(), null);

            // 这里应该调用实际的邮件发送服务
            // emailSendService.sendEmail(emailRecord);

            // 模拟发送过程
            Thread.sleep(1000);

            // 更新状态为发送成功
            emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.SUCCESS.getCode(), null);

            // 从处理中集合移除
            redisTemplate.opsForSet().remove(EMAIL_PROCESSING_KEY, emailRecord.getId());

        } catch (Exception e) {
            // 更新状态为发送失败
            emailRecordMapper.updateStatus(emailRecord.getId(), EmailStatus.FAILED.getCode(), e.getMessage());

            // 从处理中集合移除
            redisTemplate.opsForSet().remove(EMAIL_PROCESSING_KEY, emailRecord.getId());

            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 定时清理处理中的过期邮件
     */
    @Scheduled(fixedDelay = 3600000) // 每小时执行一次
    public void cleanProcessingEmails() {
        try {
            // 清理超过1小时还在处理中的邮件
            redisTemplate.delete(EMAIL_PROCESSING_KEY);
        } catch (Exception e) {
            System.err.println("清理处理中邮件失败: " + e.getMessage());
        }
    }
}