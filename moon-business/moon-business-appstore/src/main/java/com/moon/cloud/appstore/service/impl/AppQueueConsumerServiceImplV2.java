package com.moon.cloud.appstore.service.impl;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 改进版的消费实现示例
 * 展示更健壮的错误处理方式
 */
@Slf4j
public class AppQueueConsumerServiceImplV2 {

    /**
     * 方案1：记录每个任务的结果
     */
    public void consumeAppQueueWithResultTracking(String queueName) {
        List<String> urls = fetchUrlsFromQueue(queueName, 10);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // 记录成功和失败的数量
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        for (String url : urls) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    boolean result = processAppUrl(url);
                    if (result) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                    return result;
                } catch (Exception e) {
                    log.error("处理失败: {}", url, e);
                    failCount.incrementAndGet();
                    return false;
                }
            }, executor);
            futures.add(future);
        }

        try {
            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allFutures.get(30, TimeUnit.SECONDS);

            log.info("批处理完成 - 成功: {}, 失败: {}",
                successCount.get(), failCount.get());

        } catch (TimeoutException e) {
            log.error("批处理超时，部分任务可能未完成");
            // 取消未完成的任务
            futures.forEach(f -> f.cancel(true));
        } catch (Exception e) {
            log.error("批处理异常", e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 方案2：使用 handle 处理每个任务的结果
     */
    public void consumeAppQueueWithHandle(String queueName) {
        List<String> urls = fetchUrlsFromQueue(queueName, 10);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (String url : urls) {
            CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    // 执行处理
                    boolean success = processAppUrl(url);
                    if (!success) {
                        throw new RuntimeException("处理失败: " + url);
                    }
                    return url;
                }, executor)
                .handle((result, ex) -> {
                    // 统一处理成功和失败情况
                    if (ex != null) {
                        log.error("URL处理失败: {}", url, ex);
                        handleFailedUrl(url, ex.getMessage(), 1);
                        return "FAILED: " + url;
                    } else {
                        log.info("URL处理成功: {}", result);
                        return "SUCCESS: " + result;
                    }
                });
            futures.add(future);
        }

        try {
            // 收集所有结果
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allFutures.get(30, TimeUnit.SECONDS);

            // 打印所有结果
            futures.forEach(future -> {
                try {
                    String result = future.get();
                    log.info("任务结果: {}", result);
                } catch (Exception e) {
                    log.error("获取任务结果失败", e);
                }
            });

        } catch (Exception e) {
            log.error("批处理异常", e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 方案3：使用 exceptionally 处理异常
     */
    public void consumeAppQueueWithExceptionally(String queueName) {
        List<String> urls = fetchUrlsFromQueue(queueName, 10);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String url : urls) {
            CompletableFuture<Void> future = CompletableFuture
                .runAsync(() -> {
                    if (!processAppUrl(url)) {
                        throw new RuntimeException("处理失败");
                    }
                }, executor)
                .exceptionally(ex -> {
                    // 异常处理，返回null表示已处理
                    log.error("处理URL异常: {}", url, ex);
                    handleFailedUrl(url, ex.getMessage(), 1);
                    return null;
                });
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);
            log.info("所有任务执行完成");
        } catch (TimeoutException e) {
            log.error("执行超时");
            futures.forEach(f -> f.cancel(true));
        } catch (Exception e) {
            log.error("执行异常", e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 方案4：快速失败模式（任何一个失败就终止其他）
     */
    public void consumeAppQueueFailFast(String queueName) {
        List<String> urls = fetchUrlsFromQueue(queueName, 10);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CompletableFuture<Void> failureFuture = new CompletableFuture<>();

        for (String url : urls) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    if (!processAppUrl(url)) {
                        failureFuture.completeExceptionally(
                            new RuntimeException("处理失败: " + url)
                        );
                    }
                } catch (Exception e) {
                    failureFuture.completeExceptionally(e);
                }
            }, executor);
            futures.add(future);
        }

        // 添加失败触发器
        futures.add(failureFuture);

        try {
            // 任何一个失败都会导致整体失败
            CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("批处理失败，取消所有任务", e);
            // 取消所有未完成的任务
            futures.forEach(f -> f.cancel(true));
        } finally {
            executor.shutdown();
        }
    }

    // 模拟方法
    private List<String> fetchUrlsFromQueue(String queueName, int count) {
        return new ArrayList<>();
    }

    private boolean processAppUrl(String url) {
        return true;
    }

    private void handleFailedUrl(String url, String error, int retry) {
        log.error("处理失败URL: {}, 错误: {}, 重试: {}", url, error, retry);
    }
}