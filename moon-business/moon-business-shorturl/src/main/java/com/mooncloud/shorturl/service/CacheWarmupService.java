package com.mooncloud.shorturl.service;

import com.mooncloud.shorturl.dto.CacheWarmupRequest;
import com.mooncloud.shorturl.dto.CacheWarmupResponse;
import com.mooncloud.shorturl.dto.HotDataScore;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.enums.UrlStatus;
import com.mooncloud.shorturl.metrics.CacheWarmupMetrics;
import com.mooncloud.shorturl.mapper.UrlMappingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存预热服务
 *
 * 支持多种智能预热策略：
 * 1. 热门短链预热 - 基于热度分析算法，智能识别热点数据
 * 2. 最近创建预热 - 基于创建时间，优先预热新兴热点
 * 3. 最近访问预热 - 基于访问时间，保持热点数据活跃
 * 4. 时间范围预热 - 指定时间范围内的热点数据
 * 5. 用户维度预热 - 特定用户的热点短链
 * 6. 全量预热 - 按热度分数排序的全量预热
 *
 * @author mooncloud
 */
@Service
@Slf4j
public class CacheWarmupService {

    @Autowired
    private UrlMappingMapper urlMappingMapper;

    @Autowired
    private MultiLevelCacheService multiLevelCacheService;

    @Autowired
    private CacheWarmupMetrics cacheWarmupMetrics;

    @Autowired
    private HotDataDetectionService hotDataDetectionService;

    /**
     * 任务状态跟踪
     */
    private final ConcurrentHashMap<String, CacheWarmupResponse> taskStatus = new ConcurrentHashMap<>();

    /**
     * 执行缓存预热
     *
     * @param request 预热请求
     * @return 预热响应
     */
    public CacheWarmupResponse executeWarmup(CacheWarmupRequest request) {
        String taskId = UUID.randomUUID().toString();

        CacheWarmupResponse response = CacheWarmupResponse.builder()
                .taskId(taskId)
                .strategy(request.getStrategy().name())
                .status(CacheWarmupResponse.WarmupStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .warmedCount(0)
                .successCount(0)
                .failedCount(0)
                .build();

        taskStatus.put(taskId, response);

        // 记录任务启动指标
        cacheWarmupMetrics.recordTaskStarted(request.getStrategy().name());

        if (request.getAsync()) {
            // 异步执行
            executeWarmupAsync(taskId, request);
            return response;
        } else {
            // 同步执行
            return executeWarmupSync(taskId, request);
        }
    }

    /**
     * 异步执行预热
     */
    @Async
    public CompletableFuture<Void> executeWarmupAsync(String taskId, CacheWarmupRequest request) {
        try {
            executeWarmupSync(taskId, request);
        } catch (Exception e) {
            log.error("异步预热任务执行失败: {}", taskId, e);
            updateTaskStatus(taskId, CacheWarmupResponse.WarmupStatus.FAILED, e.getMessage());
            CacheWarmupResponse response = taskStatus.get(taskId);
            if (response != null) {
                cacheWarmupMetrics.recordTaskFailed(response.getStrategy(), e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 同步执行预热
     */
    private CacheWarmupResponse executeWarmupSync(String taskId, CacheWarmupRequest request) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            log.info("开始执行缓存预热任务: {}, 策略: {}", taskId, request.getStrategy());

            // 根据策略获取待预热的短链列表
            List<UrlMappingEntity> urlMappings = getUrlMappingsByStrategy(request);

            CacheWarmupResponse response = taskStatus.get(taskId);
            response.setTotalCount(urlMappings.size());

            if (CollectionUtils.isEmpty(urlMappings)) {
                log.info("没有找到需要预热的数据: {}", taskId);
                response.setStatus(CacheWarmupResponse.WarmupStatus.COMPLETED);
                return response;
            }

            // 分批预热
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);
            AtomicInteger processedCount = new AtomicInteger(0);

            int batchSize = request.getBatchSize();
            for (int i = 0; i < urlMappings.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, urlMappings.size());
                List<UrlMappingEntity> batch = urlMappings.subList(i, endIndex);

                // 预热批次数据
                warmupBatch(batch, successCount, failedCount, processedCount);

                // 更新进度
                response.setWarmedCount(processedCount.get());
                response.setSuccessCount(successCount.get());
                response.setFailedCount(failedCount.get());

                // 避免过于频繁的更新，每100条记录输出一次日志
                if (processedCount.get() % 100 == 0 || processedCount.get() == urlMappings.size()) {
                    log.info("预热进度: {}/{}, 成功: {}, 失败: {}",
                            processedCount.get(), urlMappings.size(),
                            successCount.get(), failedCount.get());
                }
            }

            stopWatch.stop();

            // 更新最终状态
            response.setStatus(CacheWarmupResponse.WarmupStatus.COMPLETED);
            response.setEndTime(LocalDateTime.now());
            response.setDuration(stopWatch.getTotalTimeMillis());

            // 记录任务完成指标
            cacheWarmupMetrics.recordTaskCompleted(
                    request.getStrategy().name(),
                    stopWatch.getTotalTimeMillis(),
                    successCount.get(),
                    failedCount.get()
            );

            log.info("缓存预热任务完成: {}, 总数: {}, 成功: {}, 失败: {}, 耗时: {}ms",
                    taskId, urlMappings.size(), successCount.get(), failedCount.get(),
                    stopWatch.getTotalTimeMillis());

            return response;

        } catch (Exception e) {
            log.error("缓存预热任务执行失败: {}", taskId, e);
            stopWatch.stop();
            updateTaskStatus(taskId, CacheWarmupResponse.WarmupStatus.FAILED, e.getMessage());
            cacheWarmupMetrics.recordTaskFailed(request.getStrategy().name(), e.getMessage());
            throw e;
        }
    }

    /**
     * 根据策略获取URL映射列表（智能化预热策略）
     */
    private List<UrlMappingEntity> getUrlMappingsByStrategy(CacheWarmupRequest request) {
        return switch (request.getStrategy()) {
            case HOT_LINKS -> {
                // 热门短链 - 基于热度分析算法智能排序
                log.info("使用智能热度分析算法获取热门短链");
                yield getHotDataSortedUrls(request.getLimit());
            }

            case RECENT_CREATED -> {
                // 最近创建 - 优先预热新兴热点
                log.info("获取最近创建的短链，优先预热新兴热点");
                QueryWrapper<UrlMappingEntity> recentWrapper = new QueryWrapper<>();
                recentWrapper.eq("status", UrlStatus.ACTIVE)
                           .orderByDesc("created_at");
                Page<UrlMappingEntity> recentPage = new Page<>(1, request.getLimit() * 2);
                List<UrlMappingEntity> recentUrls = urlMappingMapper.selectPage(recentPage, recentWrapper).getRecords();
                yield filterAndSortByHotScore(recentUrls, request.getLimit());
            }

            case RECENT_ACCESSED -> {
                // 最近访问 - 按更新时间降序，结合热度分析
                log.info("获取最近访问的短链，保持热点数据活跃");
                QueryWrapper<UrlMappingEntity> accessedWrapper = new QueryWrapper<>();
                accessedWrapper.eq("status", UrlStatus.ACTIVE)
                              .orderByDesc("updated_at");
                Page<UrlMappingEntity> accessedPage = new Page<>(1, request.getLimit() * 2);
                List<UrlMappingEntity> accessedUrls = urlMappingMapper.selectPage(accessedPage, accessedWrapper).getRecords();
                yield filterAndSortByHotScore(accessedUrls, request.getLimit());
            }

            case TIME_RANGE -> {
                // 时间范围内的热点数据
                if (request.getStartTime() == null || request.getEndTime() == null) {
                    throw new IllegalArgumentException("时间范围策略需要指定开始和结束时间");
                }
                log.info("获取指定时间范围内的热点数据: {} - {}", request.getStartTime(), request.getEndTime());
                Date startDate = Date.from(request.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
                Date endDate = Date.from(request.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
                QueryWrapper<UrlMappingEntity> timeRangeWrapper = new QueryWrapper<>();
                timeRangeWrapper.eq("status", UrlStatus.ACTIVE)
                               .between("created_at", startDate, endDate)
                               .orderByDesc("created_at");
                Page<UrlMappingEntity> timeRangePage = new Page<>(1, request.getLimit() * 2);
                List<UrlMappingEntity> timeRangeUrls = urlMappingMapper.selectPage(timeRangePage, timeRangeWrapper).getRecords();
                yield filterAndSortByHotScore(timeRangeUrls, request.getLimit());
            }

            case USER_BASED -> {
                // 用户维度的热点短链
                if (request.getUserId() == null) {
                    throw new IllegalArgumentException("用户维度策略需要指定用户ID");
                }
                log.info("获取用户 {} 的热点短链", request.getUserId());
                QueryWrapper<UrlMappingEntity> userWrapper = new QueryWrapper<>();
                userWrapper.eq("status", UrlStatus.ACTIVE)
                          .eq("user_id", request.getUserId())
                          .orderByDesc("click_count");
                Page<UrlMappingEntity> userPage = new Page<>(1, request.getLimit() * 2);
                List<UrlMappingEntity> userUrls = urlMappingMapper.selectPage(userPage, userWrapper).getRecords();
                yield filterAndSortByHotScore(userUrls, request.getLimit());
            }

            case FULL_WARMUP -> {
                // 全量预热 - 按热度分数排序
                log.info("执行全量预热，按热度分数智能排序");
                yield getHotDataSortedUrls(request.getLimit());
            }
        };
    }

    /**
     * 获取按热度分数排序的URL列表
     */
    private List<UrlMappingEntity> getHotDataSortedUrls(int limit) {
        try {
            // 获取热点排行榜
            List<HotDataScore> hotRanking = hotDataDetectionService.getHotRanking(limit);

            // 转换为URL映射对象
            List<String> shortCodes = hotRanking.stream()
                    .map(HotDataScore::getShortCode)
                    .toList();

            QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
            wrapper.in("short_url", shortCodes)
                  .eq("status", UrlStatus.ACTIVE);
            return urlMappingMapper.selectList(wrapper);

        } catch (Exception e) {
            log.warn("获取热度排序失败，降级为按点击次数排序", e);
            // 降级策略：按点击次数排序
            QueryWrapper<UrlMappingEntity> fallbackWrapper = new QueryWrapper<>();
            fallbackWrapper.eq("status", UrlStatus.ACTIVE)
                          .orderByDesc("click_count");
            Page<UrlMappingEntity> fallbackPage = new Page<>(1, limit);
            return urlMappingMapper.selectPage(fallbackPage, fallbackWrapper).getRecords();
        }
    }

    /**
     * 过滤并按热度分数排序
     */
    private List<UrlMappingEntity> filterAndSortByHotScore(List<UrlMappingEntity> urls, int limit) {
        if (urls.isEmpty()) {
            return urls;
        }

        try {
            // 批量计算热度分数
            List<String> shortCodes = urls.stream()
                    .map(UrlMappingEntity::getShortUrl)
                    .toList();

            List<HotDataScore> hotScores = hotDataDetectionService.batchCalculateHotScore(shortCodes);

            // 按热度分数降序排序，取前limit个
            List<String> sortedShortCodes = hotScores.stream()
                    .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
                    .limit(limit)
                    .map(HotDataScore::getShortCode)
                    .toList();

            // 按排序结果重新组织URL列表
            return sortedShortCodes.stream()
                    .map(shortCode -> urls.stream()
                            .filter(url -> url.getShortUrl().equals(shortCode))
                            .findFirst()
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            log.warn("热度分数排序失败，使用原始顺序", e);
            // 降级策略：返回原始顺序的前limit个
            return urls.stream().limit(limit).toList();
        }
    }

    /**
     * 预热批次数据
     */
    private void warmupBatch(List<UrlMappingEntity> batch,
                           AtomicInteger successCount,
                           AtomicInteger failedCount,
                           AtomicInteger processedCount) {
        for (UrlMappingEntity mapping : batch) {
            try {
                // 预热到多级缓存
                multiLevelCacheService.cacheOriginalUrl(mapping.getShortUrl(), mapping.getOriginalUrl());
                successCount.incrementAndGet();
            } catch (Exception e) {
                log.warn("预热单条记录失败: {}, 错误: {}", mapping.getShortUrl(), e.getMessage());
                failedCount.incrementAndGet();
            } finally {
                processedCount.incrementAndGet();
            }
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, CacheWarmupResponse.WarmupStatus status, String errorMessage) {
        CacheWarmupResponse response = taskStatus.get(taskId);
        if (response != null) {
            response.setStatus(status);
            response.setEndTime(LocalDateTime.now());
            if (errorMessage != null) {
                response.setErrorMessage(errorMessage);
            }
        }
    }

    /**
     * 获取任务状态
     */
    public CacheWarmupResponse getTaskStatus(String taskId) {
        return taskStatus.get(taskId);
    }

    /**
     * 取消预热任务
     */
    public boolean cancelTask(String taskId) {
        CacheWarmupResponse response = taskStatus.get(taskId);
        if (response != null && response.getStatus() == CacheWarmupResponse.WarmupStatus.RUNNING) {
            updateTaskStatus(taskId, CacheWarmupResponse.WarmupStatus.CANCELLED, "任务被用户取消");
            cacheWarmupMetrics.recordTaskCancelled(response.getStrategy());
            return true;
        }
        return false;
    }

    /**
     * 清理已完成的任务
     */
    public void cleanupCompletedTasks() {
        taskStatus.entrySet().removeIf(entry -> {
            CacheWarmupResponse.WarmupStatus status = entry.getValue().getStatus();
            return status == CacheWarmupResponse.WarmupStatus.COMPLETED ||
                   status == CacheWarmupResponse.WarmupStatus.FAILED ||
                   status == CacheWarmupResponse.WarmupStatus.CANCELLED;
        });
    }

    /**
     * 获取所有任务状态
     */
    public List<CacheWarmupResponse> getAllTaskStatus() {
        return taskStatus.values().stream().toList();
    }

    /**
     * 智能热点预热 - 基于热点检测算法的自动预热
     *
     * @param minHotLevel 最小热点级别
     * @param limit 预热数量限制
     * @return 预热响应
     */
    public CacheWarmupResponse smartHotDataWarmup(HotDataScore.HotLevel minHotLevel, int limit) {
        CacheWarmupRequest request = CacheWarmupRequest.builder()
                .strategy(CacheWarmupRequest.WarmupStrategy.HOT_LINKS)
                .limit(limit)
                .async(false)
                .batchSize(50)
                .build();

        log.info("开始智能热点预热，最小热点级别: {}, 限制数量: {}", minHotLevel, limit);

        String taskId = UUID.randomUUID().toString();

        try {
            // 获取指定热点级别以上的数据
            List<HotDataScore> hotData = getAllHotData(limit * 2); // 获取更多数据用于筛选

            // 过滤出符合条件的热点数据
            List<String> hotShortCodes = hotData.stream()
                    .filter(score -> score.getHotLevel().ordinal() <= minHotLevel.ordinal()) // 级别越高ordinal越小
                    .limit(limit)
                    .map(HotDataScore::getShortCode)
                    .toList();

            log.info("筛选出 {} 个符合热点级别 {} 的短链", hotShortCodes.size(), minHotLevel);

            // 预热这些热点数据
            CacheWarmupResponse response = CacheWarmupResponse.builder()
                    .taskId(taskId)
                    .strategy("SMART_HOT_DATA")
                    .status(CacheWarmupResponse.WarmupStatus.RUNNING)
                    .startTime(LocalDateTime.now())
                    .totalCount(hotShortCodes.size())
                    .warmedCount(0)
                    .successCount(0)
                    .failedCount(0)
                    .build();

            taskStatus.put(taskId, response);

            // 执行预热
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);

            for (String shortCode : hotShortCodes) {
                try {
                    QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("short_url", shortCode);
                Optional<UrlMappingEntity> urlMapping = Optional.ofNullable(urlMappingMapper.selectOne(wrapper));
                    if (urlMapping.isPresent()) {
                        multiLevelCacheService.cacheOriginalUrl(shortCode, urlMapping.get().getOriginalUrl());
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.warn("智能预热失败: {}", shortCode, e);
                    failedCount.incrementAndGet();
                }
            }

            // 更新任务状态
            response.setStatus(CacheWarmupResponse.WarmupStatus.COMPLETED);
            response.setEndTime(LocalDateTime.now());
            response.setWarmedCount(hotShortCodes.size());
            response.setSuccessCount(successCount.get());
            response.setFailedCount(failedCount.get());

            log.info("智能热点预热完成，成功: {}, 失败: {}", successCount.get(), failedCount.get());

            return response;

        } catch (Exception e) {
            log.error("智能热点预热失败", e);
            updateTaskStatus(taskId, CacheWarmupResponse.WarmupStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取所有热点数据
     */
    private List<HotDataScore> getAllHotData(int limit) {
        try {
            return hotDataDetectionService.getHotRanking(limit);
        } catch (Exception e) {
            log.warn("获取热点数据失败，返回空列表", e);
            return List.of();
        }
    }

    /**
     * 新兴热点自动预热 - 检测并预热新兴热点
     *
     * @param limit 预热数量限制
     * @return 预热响应
     */
    public CacheWarmupResponse emergingHotspotsWarmup(int limit) {
        log.info("开始新兴热点自动预热，限制数量: {}", limit);

        String taskId = UUID.randomUUID().toString();

        try {
            // 检测新兴热点
            List<HotDataScore> emergingHotspots = hotDataDetectionService.detectEmergingHotspots();

            List<String> emergingShortCodes = emergingHotspots.stream()
                    .limit(limit)
                    .map(HotDataScore::getShortCode)
                    .toList();

            log.info("检测到 {} 个新兴热点", emergingShortCodes.size());

            // 创建预热任务
            CacheWarmupResponse response = CacheWarmupResponse.builder()
                    .taskId(taskId)
                    .strategy("EMERGING_HOTSPOTS")
                    .status(CacheWarmupResponse.WarmupStatus.RUNNING)
                    .startTime(LocalDateTime.now())
                    .totalCount(emergingShortCodes.size())
                    .warmedCount(0)
                    .successCount(0)
                    .failedCount(0)
                    .build();

            taskStatus.put(taskId, response);

            // 执行预热
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);

            for (String shortCode : emergingShortCodes) {
                try {
                    QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("short_url", shortCode);
                Optional<UrlMappingEntity> urlMapping = Optional.ofNullable(urlMappingMapper.selectOne(wrapper));
                    if (urlMapping.isPresent()) {
                        multiLevelCacheService.cacheOriginalUrl(shortCode, urlMapping.get().getOriginalUrl());
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.warn("新兴热点预热失败: {}", shortCode, e);
                    failedCount.incrementAndGet();
                }
            }

            // 更新任务状态
            response.setStatus(CacheWarmupResponse.WarmupStatus.COMPLETED);
            response.setEndTime(LocalDateTime.now());
            response.setWarmedCount(emergingShortCodes.size());
            response.setSuccessCount(successCount.get());
            response.setFailedCount(failedCount.get());

            log.info("新兴热点预热完成，成功: {}, 失败: {}", successCount.get(), failedCount.get());

            return response;

        } catch (Exception e) {
            log.error("新兴热点预热失败", e);
            updateTaskStatus(taskId, CacheWarmupResponse.WarmupStatus.FAILED, e.getMessage());
            throw e;
        }
    }
}