package com.moon.cloud.appstore.service;

/**
 * App队列消费服务接口
 * 负责从Redis队列中消费URL，获取App详情并保存到数据库
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
public interface AppQueueConsumerService {

    /**
     * 消费所有分类的Redis队列
     */
    void consumeAllCategoryQueues();

    /**
     * 消费指定分类的Redis队列
     *
     * @param categoryId 分类ID
     */
    void consumeCategoryQueue(String categoryId);

    /**
     * 消费Redis队列中的App URL
     * 支持并发处理和失败重试
     *
     * @param queueName 队列名称
     */
    void consumeAppQueue(String queueName);

    /**
     * 处理单个App URL
     *
     * @param appUrl App的URL
     * @return 处理是否成功
     */
    boolean processAppUrl(String appUrl);

    /**
     * 获取指定分类队列的大小
     *
     * @param categoryId 分类ID
     * @return 队列大小
     */
    Long getCategoryQueueSize(String categoryId);

    /**
     * 获取所有分类队列的总大小
     *
     * @return 所有队列总大小
     */
    Long getAllQueuesSize();

    /**
     * 获取失败队列大小
     *
     * @return 失败队列大小
     */
    Long getFailedQueueSize();

    /**
     * 重试失败的任务
     */
    void retryFailedTasks();
}