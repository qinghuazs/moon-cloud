package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.moon.cloud.appstore.entity.AppPriceHistory;
import com.moon.cloud.appstore.mapper.AppPriceHistoryMapper;
import com.moon.cloud.appstore.service.AppPriceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * APP价格历史服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppPriceHistoryServiceImpl implements AppPriceHistoryService {

    private final AppPriceHistoryMapper appPriceHistoryMapper;

    @Override
    public List<AppPriceHistory> getAppPriceHistory(String appId, LocalDateTime startTime, LocalDateTime endTime) {
        return appPriceHistoryMapper.getPriceHistoryByTimeRange(appId, startTime, endTime);
    }

    @Override
    @Cacheable(value = "app:price:latest", key = "#appId", unless = "#result == null")
    public AppPriceHistory getLatestPrice(String appId) {
        return appPriceHistoryMapper.getLatestPriceRecord(appId);
    }

    @Override
    public List<AppPriceHistory> getFreeApps(int limit) {
        return appPriceHistoryMapper.getRecentFreeApps(limit);
    }

    @Override
    public List<AppPriceHistory> getRecentPriceDrops(int days, int limit) {
        return appPriceHistoryMapper.getRecentPriceDrops(days, limit);
    }

    @Override
    public Map<String, Object> getAppPriceStatistics(String appId) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取价格变化次数
        Integer changeCount = appPriceHistoryMapper.countPriceChanges(appId);
        statistics.put("changeCount", changeCount);

        // 获取历史最低价
        BigDecimal lowestPrice = appPriceHistoryMapper.getHistoricalLowestPrice(appId);
        statistics.put("lowestPrice", lowestPrice);

        // 获取历史最高价
        BigDecimal highestPrice = appPriceHistoryMapper.getHistoricalHighestPrice(appId);
        statistics.put("highestPrice", highestPrice);

        // 获取当前价格
        AppPriceHistory latestRecord = getLatestPrice(appId);
        if (latestRecord != null) {
            statistics.put("currentPrice", latestRecord.getNewPrice());
            statistics.put("lastChangeTime", latestRecord.getChangeTime());
            statistics.put("changeType", latestRecord.getChangeType());
        }

        // 计算价格区间
        if (lowestPrice != null && highestPrice != null && highestPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceRange = highestPrice.subtract(lowestPrice);
            statistics.put("priceRange", priceRange);
            BigDecimal volatility = priceRange.divide(highestPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            statistics.put("volatility", volatility);  // 波动率百分比
        }

        return statistics;
    }

    @Override
    @Cacheable(value = "app:price:lowest", key = "#appId", unless = "#result == null")
    public BigDecimal getHistoricalLowestPrice(String appId) {
        return appPriceHistoryMapper.getHistoricalLowestPrice(appId);
    }

    @Override
    @Cacheable(value = "app:price:highest", key = "#appId", unless = "#result == null")
    public BigDecimal getHistoricalHighestPrice(String appId) {
        return appPriceHistoryMapper.getHistoricalHighestPrice(appId);
    }

    @Override
    @Transactional
    public boolean recordPriceChange(AppPriceHistory priceHistory) {
        try {
            // 设置创建时间
            if (priceHistory.getCreatedAt() == null) {
                priceHistory.setCreatedAt(LocalDateTime.now());
            }
            if (priceHistory.getChangeTime() == null) {
                priceHistory.setChangeTime(LocalDateTime.now());
            }

            // 计算价格变化量和百分比
            if (priceHistory.getOldPrice() != null && priceHistory.getNewPrice() != null) {
                BigDecimal change = priceHistory.getNewPrice().subtract(priceHistory.getOldPrice());
                priceHistory.setPriceChange(change);

                if (priceHistory.getOldPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal changePercent = change.divide(priceHistory.getOldPrice(), 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"));
                    priceHistory.setChangePercent(changePercent);
                }
            }

            int result = appPriceHistoryMapper.insert(priceHistory);
            return result > 0;

        } catch (Exception e) {
            log.error("记录价格变化失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public int batchRecordPriceChanges(List<AppPriceHistory> priceHistoryList) {
        int successCount = 0;
        for (AppPriceHistory priceHistory : priceHistoryList) {
            if (recordPriceChange(priceHistory)) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public List<AppPriceHistory> getCategoryPriceChanges(String categoryId, int days, int limit) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<AppPriceHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppPriceHistory::getCategoryId, categoryId)
                .ge(AppPriceHistory::getChangeTime, startTime)
                .ne(AppPriceHistory::getChangeType, "INITIAL")
                .orderByDesc(AppPriceHistory::getChangeTime)
                .last("LIMIT " + limit);

        return appPriceHistoryMapper.selectList(queryWrapper);
    }

    @Override
    public List<AppPriceHistory> getDeveloperPriceChanges(String developerName, int days, int limit) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<AppPriceHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppPriceHistory::getDeveloperName, developerName)
                .ge(AppPriceHistory::getChangeTime, startTime)
                .ne(AppPriceHistory::getChangeType, "INITIAL")
                .orderByDesc(AppPriceHistory::getChangeTime)
                .last("LIMIT " + limit);

        return appPriceHistoryMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean markAsNotified(String id) {
        try {
            LambdaUpdateWrapper<AppPriceHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AppPriceHistory::getId, id)
                    .set(AppPriceHistory::getIsNotified, true)
                    .set(AppPriceHistory::getNotifiedAt, LocalDateTime.now());

            int result = appPriceHistoryMapper.update(null, updateWrapper);
            return result > 0;

        } catch (Exception e) {
            log.error("标记通知状态失败", e);
            return false;
        }
    }

    @Override
    public List<AppPriceHistory> getPendingNotifications(List<String> changeTypes, int limit) {
        LambdaQueryWrapper<AppPriceHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AppPriceHistory::getChangeType, changeTypes)
                .eq(AppPriceHistory::getIsNotified, false)
                .ge(AppPriceHistory::getChangeTime, LocalDateTime.now().minusHours(24))
                .orderByDesc(AppPriceHistory::getChangeTime)
                .last("LIMIT " + limit);

        return appPriceHistoryMapper.selectList(queryWrapper);
    }
}