package com.moon.algorithm.slidingwindow;

import java.util.concurrent.atomic.AtomicInteger;

public class SegmentedTimeSlidingWindow {

    private final int maxRequests;
    private final long windowSizeMs;
    private final int segments;
    private final long segmentSizeMs;
    private final AtomicInteger[] counters;
    private volatile long lastUpdateTime;

    public SegmentedTimeSlidingWindow(int maxRequests, long windowSizeMs, int segments) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
        this.segments = segments;
        this.segmentSizeMs = windowSizeMs / segments;
        this.counters = new AtomicInteger[segments];
        this.lastUpdateTime = System.currentTimeMillis();

        for (int i = 0; i < segments; i++) {
            counters[i] = new AtomicInteger(0);
        }
    }

    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        updateCounters(currentTime);

        int totalRequests = getTotalRequests();
        if (totalRequests >= maxRequests) {
            return false;
        }

        int currentSegment = (int) ((currentTime / segmentSizeMs) % segments);
        counters[currentSegment].incrementAndGet();
        return true;
    }

    private void updateCounters(long currentTime) {
        long timeDiff = currentTime - lastUpdateTime;
        if (timeDiff >= segmentSizeMs) {
            int segmentsToReset = (int) Math.min(timeDiff / segmentSizeMs, segments);
            int startSegment = (int) ((lastUpdateTime / segmentSizeMs + 1) % segments);

            for (int i = 0; i < segmentsToReset; i++) {
                int segmentIndex = (startSegment + i) % segments;
                counters[segmentIndex].set(0);
            }

            lastUpdateTime = currentTime;
        }
    }

    private int getTotalRequests() {
        int total = 0;
        for (AtomicInteger counter : counters) {
            total += counter.get();
        }
        return total;
    }
}
