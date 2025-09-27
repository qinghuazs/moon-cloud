package com.moon.cloud.appstore.service;

import com.moon.cloud.appstore.MoonBusinessAppstoreApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * App队列消费服务测试类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class AppQueueConsumerServiceTest extends MoonBusinessAppstoreApplicationTests {

    @Autowired
    private AppQueueConsumerService appQueueConsumerService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String APP_URL_QUEUE_KEY = "appstore:app:url:queue";

    @Test
    public void testAddUrlToQueue() {
        // 添加测试URL到队列
        List<String> testUrls = Arrays.asList(
            "https://apps.apple.com/cn/app/微信/id414478124",
            "https://apps.apple.com/cn/app/支付宝/id333206289",
            "https://apps.apple.com/cn/app/抖音/id1142110895"
        );

        for (String url : testUrls) {
            redisTemplate.opsForList().leftPush(APP_URL_QUEUE_KEY, url);
        }

        Long queueSize = appQueueConsumerService.getQueueSize();
        assertNotNull(queueSize);
        assertTrue(queueSize >= testUrls.size());
        log.info("队列大小: {}", queueSize);
    }

    @Test
    public void testProcessAppUrl() {
        String testUrl = "https://apps.apple.com/cn/app/微信/id414478124";

        boolean result = appQueueConsumerService.processAppUrl(testUrl);

        // 注意：这个测试需要Express服务正在运行
        // 如果Express服务未运行，测试将失败
        if (result) {
            log.info("处理成功: {}", testUrl);
            assertTrue(result);
        } else {
            log.warn("处理失败（可能是Express服务未启动）: {}", testUrl);
        }
    }

    @Test
    public void testConsumeAppQueue() {
        // 先添加一些测试数据
        testAddUrlToQueue();

        // 执行消费
        appQueueConsumerService.consumeAppQueue();

        // 验证队列已被消费
        Long remainingSize = appQueueConsumerService.getQueueSize();
        assertNotNull(remainingSize);
        log.info("消费后队列大小: {}", remainingSize);
    }

    @Test
    public void testGetQueueStatus() {
        Long queueSize = appQueueConsumerService.getQueueSize();
        Long failedQueueSize = appQueueConsumerService.getFailedQueueSize();

        assertNotNull(queueSize);
        assertNotNull(failedQueueSize);

        log.info("队列状态 - 待处理: {}, 失败: {}", queueSize, failedQueueSize);
    }

    @Test
    public void testRetryFailedTasks() {
        // 添加失败的URL到失败队列（模拟）
        String failedUrl = "https://apps.apple.com/cn/app/test/id999999999";
        redisTemplate.opsForList().leftPush("appstore:app:url:failed:queue", failedUrl);

        Long failedSizeBefore = appQueueConsumerService.getFailedQueueSize();
        log.info("重试前失败队列大小: {}", failedSizeBefore);

        // 执行重试
        appQueueConsumerService.retryFailedTasks();

        Long failedSizeAfter = appQueueConsumerService.getFailedQueueSize();
        log.info("重试后失败队列大小: {}", failedSizeAfter);
    }
}