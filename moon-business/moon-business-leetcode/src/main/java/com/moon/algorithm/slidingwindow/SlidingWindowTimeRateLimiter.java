package com.moon.algorithm.slidingwindow;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SlidingWindowTimeRateLimiter {

    private final int maxRequests; // 最大请求数
    private final long windowSizeMs; // 窗口大小（毫秒）
    private final ConcurrentLinkedQueue<Long> requestTimes; // 请求时间队列

    public SlidingWindowTimeRateLimiter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
        this.requestTimes = new ConcurrentLinkedQueue<>();
    }

    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();

        // 清理过期的请求记录
        cleanExpiredRequests(currentTime);

        // 检查是否超过限制
        if (requestTimes.size() >= maxRequests) {
            return false;
        }

        // 记录当前请求
        requestTimes.offer(currentTime);
        return true;
    }

    private void cleanExpiredRequests(long currentTime) {
        while (!requestTimes.isEmpty()) {
            //头结点的时间戳
            Long oldestRequest = requestTimes.peek();
            //当前时间和头结点时间戳的差值，如果超过了窗口大小，则已经过期了，需要出队
            if (currentTime - oldestRequest > windowSizeMs) {
                requestTimes.poll();
            } else {
                break;
            }
        }
    }

    public int getCurrentRequestCount() {
        cleanExpiredRequests(System.currentTimeMillis());
        return requestTimes.size();
    }
}
