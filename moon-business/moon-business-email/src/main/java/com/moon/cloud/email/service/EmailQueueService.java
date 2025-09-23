package com.moon.cloud.email.service;

import com.moon.cloud.email.entity.EmailRecord;

/**
 * 邮件队列服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface EmailQueueService {

    /**
     * 将邮件加入队列
     *
     * @param emailRecord 邮件记录
     * @return 是否成功
     */
    boolean enqueue(EmailRecord emailRecord);

    /**
     * 从队列取出邮件
     *
     * @return 邮件记录
     */
    EmailRecord dequeue();

    /**
     * 获取队列大小
     *
     * @return 队列大小
     */
    int getQueueSize();

    /**
     * 清空队列
     *
     * @return 是否成功
     */
    boolean clearQueue();

    /**
     * 处理队列中的邮件
     */
    void processQueue();

    /**
     * 处理定时邮件
     */
    void processScheduledEmails();

    /**
     * 处理重试邮件
     */
    void processRetryEmails();

    /**
     * 检查队列状态
     *
     * @return 队列状态信息
     */
    Object getQueueStatus();
}