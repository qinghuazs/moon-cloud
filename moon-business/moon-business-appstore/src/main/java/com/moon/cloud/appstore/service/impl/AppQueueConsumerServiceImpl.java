package com.moon.cloud.appstore.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.AppCrawlFailure;
import com.moon.cloud.appstore.entity.AppPriceHistory;
import com.moon.cloud.appstore.entity.Category;
import com.moon.cloud.appstore.mapper.AppCrawlFailureMapper;
import com.moon.cloud.appstore.mapper.AppMapper;
import com.moon.cloud.appstore.mapper.AppPriceHistoryMapper;
import com.moon.cloud.appstore.mapper.CategoryMapper;
import com.moon.cloud.appstore.service.AppQueueConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * App队列消费服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppQueueConsumerServiceImpl implements AppQueueConsumerService {

    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final AppMapper appMapper;
    private final AppCrawlFailureMapper appCrawlFailureMapper;
    private final CategoryMapper categoryMapper;
    private final AppPriceHistoryMapper appPriceHistoryMapper;

    // Redis队列key前缀（与CrawlerService保持一致）
    private static final String APP_QUEUE_PREFIX = "appstore:queue:";
    private static final String APP_URL_FAILED_QUEUE_KEY = "appstore:app:url:failed:queue";
    private static final String APP_URL_PROCESSING_SET_KEY = "appstore:app:url:processing";

    // 配置参数
    @Value("${appstore.express.url:http://localhost:3090}")
    private String expressServerUrl;

    @Value("${appstore.consumer.batch-size:10}")
    private Integer batchSize;

    @Value("${appstore.consumer.thread-pool-size:5}")
    private Integer threadPoolSize;

    @Value("${appstore.consumer.max-retry:3}")
    private Integer maxRetry;

    @Value("${appstore.consumer.timeout:30}")
    private Integer requestTimeout;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public void consumeAllCategoryQueues() {
        log.info("开始消费所有分类的App URL队列");

        // 获取所有分类
        List<Category> categories = categoryMapper.selectList(null);
        if (categories == null || categories.isEmpty()) {
            log.warn("没有找到任何分类，跳过消费");
            return;
        }

        log.info("发现 {} 个分类需要处理", categories.size());

        // 记录各分类处理统计
        AtomicInteger processedCategories = new AtomicInteger(0);
        AtomicInteger failedCategories = new AtomicInteger(0);

        // 并发消费各个分类队列
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Category category : categories) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    consumeCategoryQueue(category.getCategoryId());
                    processedCategories.incrementAndGet();
                    log.debug("分类 {} 处理完成", category.getCategoryId());
                } catch (Exception e) {
                    failedCategories.incrementAndGet();
                    log.error("分类 {} 处理失败", category.getCategoryId(), e);
                }
            }, executorService);
            futures.add(future);
        }

        // 等待所有分类队列消费完成
        // 每个分类预留10分钟，最少1小时
        long totalTimeout = Math.max(3600, categories.size() * 600L);

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(totalTimeout, TimeUnit.SECONDS);
            log.info("所有分类队列处理完成 - 成功: {}, 失败: {}, 总计: {}",
                processedCategories.get(), failedCategories.get(), categories.size());
        } catch (TimeoutException e) {
            log.error("处理所有分类队列超时（{}秒），部分分类可能未完成", totalTimeout);
            // 取消未完成的任务
            futures.forEach(f -> {
                if (!f.isDone()) {
                    f.cancel(true);
                }
            });
            log.info("超时后统计 - 已处理: {}, 失败: {}, 总计: {}",
                processedCategories.get(), failedCategories.get(), categories.size());
        } catch (Exception e) {
            log.error("消费所有分类队列时发生异常", e);
        }
    }

    @Override
    public void consumeCategoryQueue(String categoryId) {
        String queueName = APP_QUEUE_PREFIX + categoryId;
        Long queueSize = redisTemplate.opsForList().size(queueName);

        if (queueSize == null || queueSize == 0) {
            log.debug("分类 {} 的队列为空，跳过消费", categoryId);
            return;
        }

        log.info("开始消费分类 {} 的队列，队列大小：{}", categoryId, queueSize);
        consumeAppQueue(queueName);
    }

    @Override
    public void consumeAppQueue(String queueName) {
        log.info("开始消费队列：{}", queueName);

        ExecutorService executor = null;
        try {
            // 获取批量URL进行处理
            List<String> urls = fetchUrlsFromQueue(queueName, batchSize);
            if (urls.isEmpty()) {
                log.debug("队列 {} 为空，跳过本次消费", queueName);
                return;
            }

            log.info("队列 {} 获取到 {} 个URL待处理", queueName, urls.size());

            // 创建线程池进行并发处理
            executor = Executors.newFixedThreadPool(threadPoolSize);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 记录处理统计
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (String url : urls) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        // 添加到处理中集合
                        redisTemplate.opsForSet().add(APP_URL_PROCESSING_SET_KEY, url);

                        boolean success = processAppUrl(url);

                        if (success) {
                            successCount.incrementAndGet();
                            log.debug("成功处理URL: {}", url);
                        } else {
                            failCount.incrementAndGet();
                            // 处理失败，添加到失败队列
                            handleFailedUrl(url, "处理失败", 1);
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("处理App URL异常: {}", url, e);
                        handleFailedUrl(url, e.getMessage(), 1);
                    } finally {
                        // 从处理中集合移除
                        redisTemplate.opsForSet().remove(APP_URL_PROCESSING_SET_KEY, url);
                    }
                }, executor);
                futures.add(future);
            }

            // 等待所有任务完成，增加超时时间
            // 每个URL预留60秒，最少300秒
            long timeout = Math.max(300, urls.size() * 60L);

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(timeout, TimeUnit.SECONDS);
                log.info("队列 {} 处理完成 - 成功: {}, 失败: {}, 总计: {}",
                    queueName, successCount.get(), failCount.get(), urls.size());
            } catch (TimeoutException e) {
                log.error("队列 {} 处理超时（{}秒），部分任务可能未完成", queueName, timeout);
                // 取消未完成的任务
                futures.forEach(f -> {
                    if (!f.isDone()) {
                        f.cancel(true);
                    }
                });
                log.info("超时后统计 - 成功: {}, 失败: {}, 总计: {}",
                    successCount.get(), failCount.get(), urls.size());
            }

        } catch (Exception e) {
            log.error("消费队列 {} 异常", queueName, e);
        } finally {
            // 确保线程池关闭
            if (executor != null) {
                executor.shutdown();
                try {
                    // 等待线程池关闭
                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    @Transactional
    public boolean processAppUrl(String appUrl) {
        try {
            log.info("开始处理App URL: {}", appUrl);

            // 1. 从URL中提取App ID
            String appId = extractAppIdFromUrl(appUrl);
            if (appId == null) {
                log.error("无法从URL提取App ID: {}", appUrl);
                return false;
            }

            // 2. 检查是否已存在
            LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(App::getAppId, appId);
            App existingApp = appMapper.selectOne(queryWrapper);

            boolean isUpdate = existingApp != null;

            // 3. 调用Express服务获取App详情
            JSONObject appDetail = fetchAppDetail(appId);
            if (appDetail == null) {
                log.error("获取App详情失败: {}", appId);
                return false;
            }

            // 4. 转换数据并保存
            App app = convertToApp(appDetail, existingApp);

            // 检测并记录价格变化
            checkAndRecordPriceChange(app, existingApp, appDetail);

            if (isUpdate) {
                app.setUpdatedAt(LocalDateTime.now());
                appMapper.updateById(app);
                log.info("更新App信息成功: {}", appId);
            } else {
                app.setCreatedAt(LocalDateTime.now());
                app.setUpdatedAt(LocalDateTime.now());
                appMapper.insert(app);
                log.info("保存新App信息成功: {}", appId);
            }

            // 5. 记录最后爬取时间
            app.setLastCrawledAt(LocalDateTime.now());
            appMapper.updateById(app);

            return true;

        } catch (Exception e) {
            log.error("处理App URL失败: {}", appUrl, e);
            return false;
        }
    }

    @Override
    public Long getCategoryQueueSize(String categoryId) {
        String queueName = APP_QUEUE_PREFIX + categoryId;
        return redisTemplate.opsForList().size(queueName);
    }

    @Override
    public Long getAllQueuesSize() {
        // 获取所有分类队列的总大小
        List<Category> categories = categoryMapper.selectList(null);
        if (categories == null || categories.isEmpty()) {
            return 0L;
        }

        long totalSize = 0;
        for (Category category : categories) {
            String queueName = APP_QUEUE_PREFIX + category.getCategoryId();
            Long size = redisTemplate.opsForList().size(queueName);
            if (size != null) {
                totalSize += size;
            }
        }
        return totalSize;
    }

    @Override
    public Long getFailedQueueSize() {
        return redisTemplate.opsForList().size(APP_URL_FAILED_QUEUE_KEY);
    }

    @Override
    @Async
    public void retryFailedTasks() {
        log.info("开始重试失败任务，失败队列大小：{}", getFailedQueueSize());

        try {
            List<String> failedUrls = new ArrayList<>();
            String url;

            // 从失败队列中获取URL
            while ((url = redisTemplate.opsForList().rightPop(APP_URL_FAILED_QUEUE_KEY)) != null) {
                failedUrls.add(url);
            }

            if (failedUrls.isEmpty()) {
                log.debug("失败队列为空，跳过重试");
                return;
            }

            // 查询失败记录并重新分配到对应的分类队列
            for (String failedUrl : failedUrls) {
                AppCrawlFailure failure = appCrawlFailureMapper.selectOne(
                    new LambdaQueryWrapper<AppCrawlFailure>()
                        .eq(AppCrawlFailure::getUrl, failedUrl)
                        .orderByDesc(AppCrawlFailure::getCreatedAt)
                        .last("LIMIT 1")
                );

                if (failure != null && failure.getRetryCount() < maxRetry) {
                    // 尝试从URL推断分类或使用默认队列
                    String queueName = determineQueueForUrl(failedUrl);
                    redisTemplate.opsForList().leftPush(queueName, failedUrl);
                    log.info("重新加入处理队列: {}, 队列: {}, 重试次数: {}",
                            failedUrl, queueName, failure.getRetryCount() + 1);
                } else {
                    log.warn("URL已达最大重试次数，放弃重试: {}", failedUrl);
                }
            }

        } catch (Exception e) {
            log.error("重试失败任务异常", e);
        }
    }

    /**
     * 从指定Redis队列获取批量URL
     */
    private List<String> fetchUrlsFromQueue(String queueName, int count) {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String url = redisTemplate.opsForList().rightPop(queueName);
            if (url != null) {
                urls.add(url);
            } else {
                break;
            }
        }
        return urls;
    }

    /**
     * 根据URL确定应该使用的队列
     */
    private String determineQueueForUrl(String url) {
        // 这里可以根据URL特征或其他逻辑来确定队列
        // 暂时返回默认队列
        return APP_QUEUE_PREFIX + "default";
    }

    /**
     * 从URL中提取App ID
     */
    private String extractAppIdFromUrl(String url) {
        // 匹配 id + 数字的模式
        Pattern pattern = Pattern.compile("/id(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 调用Express服务获取App详情
     */
    private JSONObject fetchAppDetail(String appId) {
        try {
            String url = expressServerUrl + "/appstore/app/detail?appId=" + appId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return JSON.parseObject(response.getBody());
            }

        } catch (Exception e) {
            log.error("调用Express服务获取App详情失败: {}", appId, e);
        }
        return null;
    }

    /**
     * 转换JSON数据为App实体
     */
    private App convertToApp(JSONObject json, App existingApp) {
        App app = existingApp != null ? existingApp : new App();

        // 基本信息
        app.setAppId(json.getString("id"));
        app.setBundleId(json.getString("appId"));
        app.setName(json.getString("title"));
        app.setDescription(json.getString("description"));
        app.setIconUrl(json.getString("icon"));
        app.setVersion(json.getString("version"));
        app.setReleaseNotes(json.getString("releaseNotes"));

        // 开发者信息
        app.setDeveloperName(json.getString("developer"));
        app.setDeveloperId(json.getString("developerId"));
        app.setDeveloperUrl(json.getString("developerUrl"));

        // 分类信息
        app.setPrimaryCategoryId(json.getString("primaryGenreId"));
        app.setPrimaryCategoryName(json.getString("primaryGenre"));

        // 构建categories列表
        JSONArray genres = json.getJSONArray("genres");
        JSONArray genreIds = json.getJSONArray("genreIds");
        if (genres != null && genreIds != null) {
            List<App.CategoryInfo> categories = new ArrayList<>();
            for (int i = 0; i < Math.min(genres.size(), genreIds.size()); i++) {
                App.CategoryInfo category = new App.CategoryInfo();
                category.setId(genreIds.getString(i));
                category.setName(genres.getString(i));
                categories.add(category);
            }
            app.setCategories(categories);
        }

        // 版本和时间信息
        String releasedStr = json.getString("released");
        if (StringUtils.hasText(releasedStr)) {
            app.setReleaseDate(parseDateTime(releasedStr));
        }

        String updatedStr = json.getString("updated");
        if (StringUtils.hasText(updatedStr)) {
            app.setUpdatedDate(parseDateTime(updatedStr));
        }

        // 大小和系统要求
        String sizeStr = json.getString("size");
        if (StringUtils.hasText(sizeStr)) {
            app.setFileSize(Long.parseLong(sizeStr));
        }
        app.setMinimumOsVersion(json.getString("requiredOsVersion"));

        // 评分信息
        app.setRating(json.getBigDecimal("score"));
        app.setRatingCount(json.getInteger("reviews"));
        app.setCurrentVersionRating(json.getBigDecimal("currentVersionScore"));
        app.setCurrentVersionRatingCount(json.getInteger("currentVersionReviews"));

        // 价格信息
        app.setCurrentPrice(json.getBigDecimal("price"));
        app.setCurrency(json.getString("currency"));
        app.setIsFree(json.getBoolean("free"));

        // 内容分级
        app.setContentRating(json.getString("contentRating"));

        // 语言
        JSONArray languages = json.getJSONArray("languages");
        if (languages != null) {
            app.setLanguages(languages.toJavaList(String.class));
        }

        // 截图
        JSONArray screenshots = json.getJSONArray("screenshots");
        if (screenshots != null) {
            app.setScreenshots(screenshots.toJavaList(String.class));
        }

        JSONArray ipadScreenshots = json.getJSONArray("ipadScreenshots");
        if (ipadScreenshots != null) {
            app.setIpadScreenshots(ipadScreenshots.toJavaList(String.class));
        }

        // 支持的设备
        JSONArray supportedDevices = json.getJSONArray("supportedDevices");
        if (supportedDevices != null) {
            app.setSupportedDevices(supportedDevices.toJavaList(String.class));
        }

        // 设置状态为正常
        app.setStatus(1);

        return app;
    }

    /**
     * 解析ISO 8601时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            return zdt.toLocalDateTime();
        } catch (Exception e) {
            log.error("解析时间失败: {}", dateTimeStr, e);
            return LocalDateTime.now();
        }
    }

    /**
     * 检测并记录价格变化
     */
    private void checkAndRecordPriceChange(App newApp, App existingApp, JSONObject appDetail) {
        try {
            BigDecimal newPrice = newApp.getCurrentPrice();
            BigDecimal oldPrice = null;
            Boolean oldIsFree = null;

            // 如果是更新，获取原价格
            if (existingApp != null) {
                oldPrice = existingApp.getCurrentPrice();
                oldIsFree = existingApp.getIsFree();
            }

            // 判断是否需要记录价格历史
            boolean shouldRecord = false;
            String changeType = null;

            if (existingApp == null) {
                // 新App，记录初始价格
                shouldRecord = true;
                changeType = "INITIAL";
            } else if (oldPrice != null && newPrice != null) {
                // 价格发生变化
                int priceComparison = newPrice.compareTo(oldPrice);

                if (priceComparison != 0) {
                    shouldRecord = true;

                    if (newPrice.compareTo(BigDecimal.ZERO) == 0 && oldPrice.compareTo(BigDecimal.ZERO) > 0) {
                        changeType = "FREE";  // 变为限免
                    } else if (oldPrice.compareTo(BigDecimal.ZERO) == 0 && newPrice.compareTo(BigDecimal.ZERO) > 0) {
                        changeType = "RESTORE";  // 恢复收费
                    } else if (priceComparison > 0) {
                        changeType = "INCREASE";  // 涨价
                    } else {
                        changeType = "DECREASE";  // 降价
                    }
                }
            }

            // 记录价格历史
            if (shouldRecord) {
                AppPriceHistory priceHistory = new AppPriceHistory();
                priceHistory.setAppId(newApp.getAppId());
                priceHistory.setBundleId(newApp.getBundleId());
                priceHistory.setAppName(newApp.getName());
                priceHistory.setOldPrice(oldPrice);
                priceHistory.setNewPrice(newPrice);
                priceHistory.setCurrency(newApp.getCurrency());
                priceHistory.setChangeType(changeType);
                priceHistory.setIsFree(newApp.getIsFree());
                priceHistory.setOldIsFree(oldIsFree);
                priceHistory.setVersion(newApp.getVersion());
                priceHistory.setCategoryId(newApp.getPrimaryCategoryId());
                priceHistory.setCategoryName(newApp.getPrimaryCategoryName());
                priceHistory.setDeveloperName(newApp.getDeveloperName());
                priceHistory.setChangeTime(LocalDateTime.now());
                priceHistory.setCreatedAt(LocalDateTime.now());
                priceHistory.setSource("CRAWLER");

                // 计算价格类型
                if (newPrice.compareTo(BigDecimal.ZERO) == 0) {
                    priceHistory.setPriceType("FREE");
                } else {
                    priceHistory.setPriceType("NORMAL");
                }

                // 保存价格历史
                appPriceHistoryMapper.insert(priceHistory);

                log.info("记录价格变化 - App: {}, 变化类型: {}, 原价: {}, 新价: {}",
                    newApp.getName(), changeType, oldPrice, newPrice);

                // 如果是限免或大幅降价，可以发送通知
                if ("FREE".equals(changeType) ||
                    ("DECREASE".equals(changeType) && oldPrice != null &&
                     newPrice.divide(oldPrice, 2, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("0.5")) < 0)) {
                    log.info("重要价格变化！App: {} 现在 {}",
                        newApp.getName(),
                        "FREE".equals(changeType) ? "限免" : "降价超过50%");
                    // TODO: 发送通知给用户
                }
            }

        } catch (Exception e) {
            log.error("记录价格变化失败", e);
        }
    }

    /**
     * 处理失败的URL
     */
    private void handleFailedUrl(String url, String errorMessage, int retryCount) {
        try {
            // 添加到失败队列
            redisTemplate.opsForList().leftPush(APP_URL_FAILED_QUEUE_KEY, url);

            // 记录失败信息到数据库
            AppCrawlFailure failure = new AppCrawlFailure();
            failure.setUrl(url);
            failure.setAppId(extractAppIdFromUrl(url));
            failure.setErrorMessage(errorMessage);
            failure.setRetryCount(retryCount);
            failure.setCreatedAt(LocalDateTime.now());

            appCrawlFailureMapper.insert(failure);

            log.warn("URL处理失败已记录: {}, 错误: {}", url, errorMessage);

        } catch (Exception e) {
            log.error("记录失败信息异常", e);
        }
    }
}