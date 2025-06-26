package com.moon.algorithm.slidingwindow.test;

import com.moon.algorithm.slidingwindow.SlidingWindowTimeRateLimiter;

public class SlidingWindowTimeRateLimiterTest {

    public static void main(String[] args) throws InterruptedException {
        // 创建限流器：1秒内最多5个请求
        SlidingWindowTimeRateLimiter limiter = new SlidingWindowTimeRateLimiter(5, 1000);

        // 模拟请求
        for (int i = 0; i < 20; i++) {
            boolean allowed = limiter.allowRequest();
            System.out.println("请求 " + (i + 1) + ": " +
                    (allowed ? "通过" : "被限流") +
                    ", 当前窗口请求数: " + limiter.getCurrentRequestCount());

            Thread.sleep(20); // 间隔200ms
        }
    }
}
