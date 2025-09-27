package com.moon.cloud.appstore.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类队列消费测试
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class CategoryQueueConsumerTest {

    @Autowired
    private AppQueueConsumerService appQueueConsumerService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testAddUrlToCategoryQueue() {
        // 模拟不同分类的URL
        String gamesCategoryId = "6014"; // 游戏分类
        String educationCategoryId = "6000"; // 教育分类

        // 添加URL到游戏分类队列
        String gamesQueue = "appstore:queue:" + gamesCategoryId;
        redisTemplate.opsForList().leftPush(gamesQueue, "https://apps.apple.com/cn/app/game1/id123456");
        redisTemplate.opsForList().leftPush(gamesQueue, "https://apps.apple.com/cn/app/game2/id234567");

        // 添加URL到教育分类队列
        String educationQueue = "appstore:queue:" + educationCategoryId;
        redisTemplate.opsForList().leftPush(educationQueue, "https://apps.apple.com/cn/app/edu1/id345678");
        redisTemplate.opsForList().leftPush(educationQueue, "https://apps.apple.com/cn/app/edu2/id456789");

        // 验证队列大小
        Long gamesQueueSize = appQueueConsumerService.getCategoryQueueSize(gamesCategoryId);
        Long educationQueueSize = appQueueConsumerService.getCategoryQueueSize(educationCategoryId);

        assertTrue(gamesQueueSize >= 2);
        assertTrue(educationQueueSize >= 2);

        log.info("游戏分类队列大小: {}", gamesQueueSize);
        log.info("教育分类队列大小: {}", educationQueueSize);
    }

    @Test
    public void testGetAllQueuesSize() {
        Long totalSize = appQueueConsumerService.getAllQueuesSize();
        assertNotNull(totalSize);
        log.info("所有分类队列总大小: {}", totalSize);
    }

    @Test
    public void testConsumeCategoryQueue() {
        String testCategoryId = "6014";

        // 添加测试数据
        String queueName = "appstore:queue:" + testCategoryId;
        redisTemplate.opsForList().leftPush(queueName, "https://apps.apple.com/cn/app/test/id111111");

        // 消费指定分类队列
        appQueueConsumerService.consumeCategoryQueue(testCategoryId);

        // 验证队列已被消费（注意：需要Express服务运行才能成功）
        Long remainingSize = appQueueConsumerService.getCategoryQueueSize(testCategoryId);
        log.info("分类 {} 消费后队列大小: {}", testCategoryId, remainingSize);
    }

    @Test
    public void testConsumeAllCategoryQueues() {
        // 添加测试数据到多个分类队列
        testAddUrlToCategoryQueue();

        // 获取消费前的总大小
        Long totalSizeBefore = appQueueConsumerService.getAllQueuesSize();
        log.info("消费前所有队列总大小: {}", totalSizeBefore);

        // 消费所有分类队列
        // 注意：这个测试需要Express服务运行才能成功
        appQueueConsumerService.consumeAllCategoryQueues();

        // 获取消费后的总大小
        Long totalSizeAfter = appQueueConsumerService.getAllQueuesSize();
        log.info("消费后所有队列总大小: {}", totalSizeAfter);
    }
}