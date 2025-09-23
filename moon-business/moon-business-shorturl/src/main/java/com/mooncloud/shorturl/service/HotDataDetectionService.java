package com.mooncloud.shorturl.service;

import com.mooncloud.shorturl.dto.HotDataScore;
import com.mooncloud.shorturl.entity.UrlAccessLogEntity;
import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.mapper.UrlAccessLogMapper;
import com.mooncloud.shorturl.mapper.UrlMappingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 热点数据检测服务
 *
 * 基于多维度指标算法识别热点数据：
 * 1. 访问频次 (40% 权重) - 点击量是最直接的热度指标
 * 2. 时效性 (25% 权重) - 最近的访问更有价值
 * 3. 趋势性 (20% 权重) - 访问量增长趋势
 * 4. 用户分布 (10% 权重) - 独立用户数量
 * 5. 地域分布 (5% 权重) - IP地址分布广度
 *
 * @author mooncloud
 */
@Service
@Slf4j
public class HotDataDetectionService {

    @Autowired
    private UrlMappingMapper urlMappingMapper;

    @Autowired
    private UrlAccessLogMapper urlAccessLogMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 权重配置
    private static final double FREQUENCY_WEIGHT = 0.40;    // 访问频次权重
    private static final double TIMELINESS_WEIGHT = 0.25;   // 时效性权重
    private static final double TREND_WEIGHT = 0.20;        // 趋势权重
    private static final double USER_DISTRIBUTION_WEIGHT = 0.10; // 用户分布权重
    private static final double GEOGRAPHIC_WEIGHT = 0.05;   // 地域分布权重

    // 缓存键前缀
    private static final String HOT_SCORE_CACHE_PREFIX = "hot_score:";
    private static final String HOT_RANKING_CACHE_KEY = "hot_ranking";

    /**
     * 计算单个短链的热度分数
     *
     * @param shortCode 短链标识符
     * @return 热度分数对象
     */
    public HotDataScore calculateHotScore(String shortCode) {
        log.debug("开始计算短链热度分数: {}", shortCode);

        // 检查缓存
        String cacheKey = HOT_SCORE_CACHE_PREFIX + shortCode;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof HotDataScore) {
            log.debug("从缓存获取热度分数: {}", shortCode);
            return (HotDataScore) cached;
        }

        // 获取URL映射信息
        QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("short_url", shortCode);
        Optional<UrlMappingEntity> urlMappingOpt = Optional.ofNullable(urlMappingMapper.selectOne(wrapper));
        if (urlMappingOpt.isEmpty()) {
            return createEmptyScore(shortCode);
        }

        UrlMappingEntity urlMapping = urlMappingOpt.get();

        // 获取访问统计数据
        AccessStats stats = getAccessStats(shortCode);

        // 计算各维度分数
        double frequencyScore = calculateFrequencyScore(stats);
        double timelinessScore = calculateTimelinessScore(stats, urlMapping.getCreatedAt());
        double trendScore = calculateTrendScore(stats);
        double userDistributionScore = calculateUserDistributionScore(stats);
        double geographicScore = calculateGeographicScore(stats);

        // 计算综合分数
        double totalScore = frequencyScore * FREQUENCY_WEIGHT +
                           timelinessScore * TIMELINESS_WEIGHT +
                           trendScore * TREND_WEIGHT +
                           userDistributionScore * USER_DISTRIBUTION_WEIGHT +
                           geographicScore * GEOGRAPHIC_WEIGHT;

        // 构建结果
        HotDataScore hotScore = HotDataScore.builder()
                .shortCode(shortCode)
                .totalScore(Math.round(totalScore * 100.0) / 100.0)
                .accessFrequencyScore(Math.round(frequencyScore * 100.0) / 100.0)
                .timelinessScore(Math.round(timelinessScore * 100.0) / 100.0)
                .trendScore(Math.round(trendScore * 100.0) / 100.0)
                .userDistributionScore(Math.round(userDistributionScore * 100.0) / 100.0)
                .geographicScore(Math.round(geographicScore * 100.0) / 100.0)
                .hotLevel(HotDataScore.HotLevel.fromScore(totalScore))
                .totalClicks(stats.totalClicks)
                .recentClicks(stats.recentClicks)
                .uniqueUsers(stats.uniqueUsers)
                .uniqueIps(stats.uniqueIps)
                .createdAt(urlMapping.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .lastAccessTime(stats.lastAccessTime)
                .calculatedAt(LocalDateTime.now())
                .build();

        // 缓存结果（缓存10分钟）
        redisTemplate.opsForValue().set(cacheKey, hotScore, 10, TimeUnit.MINUTES);

        log.debug("计算完成 - 短链: {}, 总分: {}, 级别: {}",
                shortCode, hotScore.getTotalScore(), hotScore.getHotLevel());

        return hotScore;
    }

    /**
     * 批量计算热度分数
     *
     * @param shortCodes 短链列表
     * @return 热度分数列表
     */
    public List<HotDataScore> batchCalculateHotScore(List<String> shortCodes) {
        return shortCodes.parallelStream()
                .map(this::calculateHotScore)
                .collect(Collectors.toList());
    }

    /**
     * 获取热点排行榜
     *
     * @param limit 返回数量限制
     * @return 热点数据排行榜
     */
    public List<HotDataScore> getHotRanking(int limit) {
        // 检查缓存
        List<HotDataScore> cached = (List<HotDataScore>) redisTemplate.opsForValue().get(HOT_RANKING_CACHE_KEY);
        if (cached != null && !cached.isEmpty()) {
            return cached.stream().limit(limit).collect(Collectors.toList());
        }

        // 获取活跃的短链
        QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", com.mooncloud.shorturl.enums.UrlStatus.ACTIVE);
        wrapper.orderByDesc("click_count");
        Page<UrlMappingEntity> page = new Page<>(1, Math.min(limit * 3, 1000));
        List<UrlMappingEntity> activeUrls = urlMappingMapper.selectPage(page, wrapper).getRecords();

        // 批量计算热度分数
        List<HotDataScore> hotScores = activeUrls.parallelStream()
                .map(url -> calculateHotScore(url.getShortUrl()))
                .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
                .limit(limit)
                .collect(Collectors.toList());

        // 缓存结果（缓存5分钟）
        redisTemplate.opsForValue().set(HOT_RANKING_CACHE_KEY, hotScores, 5, TimeUnit.MINUTES);

        return hotScores;
    }

    /**
     * 获取指定热点级别的数据
     *
     * @param hotLevel 热点级别
     * @param limit 数量限制
     * @return 指定级别的热点数据
     */
    public List<HotDataScore> getHotDataByLevel(HotDataScore.HotLevel hotLevel, int limit) {
        return getHotRanking(limit * 2).stream()
                .filter(score -> score.getHotLevel() == hotLevel)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 实时检测新兴热点
     *
     * @return 新兴热点列表
     */
    public List<HotDataScore> detectEmergingHotspots() {
        // 获取最近1小时创建的短链
        Date oneHourAgo = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        Date now = new Date();

        QueryWrapper<UrlMappingEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", com.mooncloud.shorturl.enums.UrlStatus.ACTIVE)
               .between("created_at", oneHourAgo, now)
               .orderByDesc("created_at");
        Page<UrlMappingEntity> page = new Page<>(1, 100);
        List<UrlMappingEntity> recentUrls = urlMappingMapper.selectPage(page, wrapper).getRecords();

        // 计算热度分数，筛选出趋势分数高的
        return recentUrls.parallelStream()
                .map(url -> calculateHotScore(url.getShortUrl()))
                .filter(score -> score.getTrendScore() > 70) // 趋势分数大于70
                .sorted((a, b) -> Double.compare(b.getTrendScore(), a.getTrendScore()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 获取访问统计数据
     */
    private AccessStats getAccessStats(String shortCode) {
        AccessStats stats = new AccessStats();

        // 总访问次数
        QueryWrapper<UrlAccessLogEntity> countWrapper = new QueryWrapper<>();
        countWrapper.eq("short_url", shortCode);
        stats.totalClicks = urlAccessLogMapper.selectCount(countWrapper);

        // 最近24小时访问次数
        Date yesterday = Date.from(LocalDateTime.now().minusHours(24).atZone(ZoneId.systemDefault()).toInstant());
        Date now = new Date();
        QueryWrapper<UrlAccessLogEntity> recentWrapper = new QueryWrapper<>();
        recentWrapper.eq("short_url", shortCode)
                    .between("access_time", yesterday, now);
        stats.recentClicks = urlAccessLogMapper.selectCount(recentWrapper);

        // 最近7天每日访问量（用于趋势计算）
        stats.dailyClicksLast7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Date dayStart = Date.from(LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0)
                    .atZone(ZoneId.systemDefault()).toInstant());
            Date dayEnd = Date.from(LocalDateTime.now().minusDays(i).withHour(23).withMinute(59).withSecond(59)
                    .atZone(ZoneId.systemDefault()).toInstant());
            QueryWrapper<UrlAccessLogEntity> dayWrapper = new QueryWrapper<>();
            dayWrapper.eq("short_url", shortCode)
                     .between("access_time", dayStart, dayEnd);
            Long dayClicks = urlAccessLogMapper.selectCount(dayWrapper);
            stats.dailyClicksLast7Days.add(dayClicks);
        }

        // 独立用户数（基于IP地址简化计算）
        stats.uniqueIps = urlAccessLogMapper.countDistinctIpsByShortUrl(shortCode);
        stats.uniqueUsers = stats.uniqueIps; // 简化处理，实际可以基于用户ID

        // 最后访问时间
        QueryWrapper<UrlAccessLogEntity> recentAccessWrapper = new QueryWrapper<>();
        recentAccessWrapper.eq("short_url", shortCode)
                          .orderByDesc("access_time")
                          .last("LIMIT 1");
        List<UrlAccessLogEntity> recentAccess = urlAccessLogMapper.selectList(recentAccessWrapper);
        if (!recentAccess.isEmpty()) {
            stats.lastAccessTime = recentAccess.get(0).getAccessTime().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return stats;
    }

    /**
     * 计算访问频次分数 (0-100)
     */
    private double calculateFrequencyScore(AccessStats stats) {
        if (stats.totalClicks == 0) return 0;

        // 使用对数函数避免分数过于集中
        double score = Math.log(stats.totalClicks + 1) * 20;
        return Math.min(score, 100);
    }

    /**
     * 计算时效性分数 (0-100)
     */
    private double calculateTimelinessScore(AccessStats stats, Date createdAt) {
        if (stats.lastAccessTime == null) return 0;

        // 最近访问时间越近，分数越高
        long hoursFromLastAccess = java.time.Duration.between(
                stats.lastAccessTime, LocalDateTime.now()).toHours();

        if (hoursFromLastAccess == 0) return 100;
        if (hoursFromLastAccess >= 168) return 0; // 7天以上为0分

        return Math.max(0, 100 - hoursFromLastAccess * 0.6); // 每小时减少0.6分
    }

    /**
     * 计算趋势分数 (0-100)
     */
    private double calculateTrendScore(AccessStats stats) {
        if (stats.dailyClicksLast7Days.size() < 3) return 0;

        // 计算最近3天的增长趋势
        List<Long> recent3Days = stats.dailyClicksLast7Days.subList(4, 7); // 最近3天
        List<Long> previous3Days = stats.dailyClicksLast7Days.subList(1, 4); // 前3天

        double recentAvg = recent3Days.stream().mapToLong(Long::longValue).average().orElse(0);
        double previousAvg = previous3Days.stream().mapToLong(Long::longValue).average().orElse(0);

        if (previousAvg == 0) {
            return recentAvg > 0 ? 100 : 0;
        }

        double growthRate = (recentAvg - previousAvg) / previousAvg;

        // 增长率转换为分数
        if (growthRate >= 1.0) return 100; // 100%以上增长
        if (growthRate <= -0.5) return 0;  // 50%以上下降

        return Math.max(0, 50 + growthRate * 50);
    }

    /**
     * 计算用户分布分数 (0-100)
     */
    private double calculateUserDistributionScore(AccessStats stats) {
        if (stats.totalClicks == 0 || stats.uniqueUsers == 0) return 0;

        // 独立用户数与总访问数的比例，越高说明分布越广
        double ratio = stats.uniqueUsers.doubleValue() / stats.totalClicks.doubleValue();

        // 比例转换为分数
        return Math.min(ratio * 200, 100); // 50%比例为满分
    }

    /**
     * 计算地域分布分数 (0-100)
     */
    private double calculateGeographicScore(AccessStats stats) {
        if (stats.uniqueIps == 0) return 0;

        // 简化计算：基于独立IP数量
        double score = Math.log(stats.uniqueIps + 1) * 25;
        return Math.min(score, 100);
    }

    /**
     * 创建空分数对象
     */
    private HotDataScore createEmptyScore(String shortCode) {
        return HotDataScore.builder()
                .shortCode(shortCode)
                .totalScore(0.0)
                .accessFrequencyScore(0.0)
                .timelinessScore(0.0)
                .trendScore(0.0)
                .userDistributionScore(0.0)
                .geographicScore(0.0)
                .hotLevel(HotDataScore.HotLevel.COLD)
                .totalClicks(0L)
                .recentClicks(0L)
                .uniqueUsers(0L)
                .uniqueIps(0L)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 清理热度分数缓存
     */
    public void clearHotScoreCache() {
        Set<String> keys = redisTemplate.keys(HOT_SCORE_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        redisTemplate.delete(HOT_RANKING_CACHE_KEY);
        log.info("热度分数缓存已清理");
    }

    /**
     * 访问统计数据内部类
     */
    private static class AccessStats {
        Long totalClicks = 0L;
        Long recentClicks = 0L;
        Long uniqueUsers = 0L;
        Long uniqueIps = 0L;
        LocalDateTime lastAccessTime;
        List<Long> dailyClicksLast7Days = new ArrayList<>();
    }
}