package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.AppPriceHistory;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.mapper.AppMapper;
import com.moon.cloud.appstore.mapper.AppPriceHistoryMapper;
import com.moon.cloud.appstore.mapper.FreePromotionMapper;
import com.moon.cloud.appstore.service.AppDetailService;
import com.moon.cloud.appstore.vo.AppDetailVO;
import com.moon.cloud.appstore.vo.AppPriceChartVO;
import com.moon.cloud.appstore.vo.AppSimilarVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用详情服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppDetailServiceImpl implements AppDetailService {

    private final AppMapper appMapper;
    private final AppPriceHistoryMapper appPriceHistoryMapper;
    private final FreePromotionMapper freePromotionMapper;

    @Override
    @Cacheable(value = "app:detail", key = "#appId", unless = "#result == null")
    public AppDetailVO getAppDetail(String appId) {
        log.info("获取应用详情: {}", appId);

        // 查询应用信息
        App app = getAppByIdOrAppStoreId(appId);
        if (app == null) {
            log.warn("应用不存在: {}", appId);
            return null;
        }

        // 转换为VO
        AppDetailVO detailVO = convertToDetailVO(app);

        // 查询限免信息
        FreePromotion activePromotion = getActivePromotion(app.getAppId());
        if (activePromotion != null) {
            detailVO.setIsFreeNow(true);
            detailVO.setFreePromotion(buildPromotionInfo(activePromotion));
        }

        // 查询价格历史
        List<AppPriceHistory> priceHistory = getPriceHistory(app.getAppId(), 30);
        if (!priceHistory.isEmpty()) {
            detailVO.setPriceHistory(buildPriceHistoryInfo(priceHistory));
        }

        return detailVO;
    }

    @Override
    @Cacheable(value = "app:price:chart", key = "#appId + ':' + #days", unless = "#result == null")
    public AppPriceChartVO getAppPriceChart(String appId, Integer days) {
        log.info("获取应用价格图表: appId={}, days={}", appId, days);

        if (days == null || days <= 0) {
            days = 90; // 默认90天
        }

        App app = getAppByIdOrAppStoreId(appId);
        if (app == null) {
            return null;
        }

        AppPriceChartVO chartVO = new AppPriceChartVO();
        chartVO.setAppId(app.getAppId());
        chartVO.setAppName(app.getName());
        chartVO.setDays(days);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        chartVO.setStartTime(startTime);
        chartVO.setEndTime(endTime);

        // 获取价格历史数据
        List<AppPriceHistory> priceHistoryList = appPriceHistoryMapper.getPriceHistoryByTimeRange(
                app.getAppId(), startTime, endTime);

        if (!priceHistoryList.isEmpty()) {
            // 设置当前价格和统计信息
            chartVO.setCurrentPrice(app.getCurrentPrice());
            chartVO.setOriginalPrice(app.getOriginalPrice());

            // 计算统计数据
            BigDecimal lowestPrice = priceHistoryList.stream()
                    .map(AppPriceHistory::getNewPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(app.getCurrentPrice());
            chartVO.setLowestPrice(lowestPrice);

            BigDecimal highestPrice = priceHistoryList.stream()
                    .map(AppPriceHistory::getNewPrice)
                    .max(BigDecimal::compareTo)
                    .orElse(app.getOriginalPrice());
            chartVO.setHighestPrice(highestPrice);

            // 计算平均价格
            BigDecimal totalPrice = priceHistoryList.stream()
                    .map(AppPriceHistory::getNewPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal averagePrice = totalPrice.divide(
                    new BigDecimal(priceHistoryList.size()), 2, RoundingMode.HALF_UP);
            chartVO.setAveragePrice(averagePrice);

            chartVO.setChangeCount(priceHistoryList.size());

            // 统计限免次数
            long freeCount = priceHistoryList.stream()
                    .filter(h -> "FREE".equals(h.getChangeType()))
                    .count();
            chartVO.setFreeCount((int) freeCount);

            // 构建价格点和事件
            chartVO.setPricePoints(buildPricePoints(priceHistoryList));
            chartVO.setPriceEvents(buildPriceEvents(priceHistoryList));
        }

        return chartVO;
    }

    @Override
    public List<AppSimilarVO> getSimilarApps(String appId, Integer limit) {
        log.info("获取相似应用: appId={}, limit={}", appId, limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        App currentApp = getAppByIdOrAppStoreId(appId);
        if (currentApp == null) {
            return new ArrayList<>();
        }

        // 基于分类查找相似应用
        LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(App::getPrimaryCategoryId, currentApp.getPrimaryCategoryId())
                   .ne(App::getAppId, currentApp.getAppId())
                   .orderByDesc(App::getRating)
                   .last("LIMIT " + limit);

        List<App> similarApps = appMapper.selectList(queryWrapper);

        return similarApps.stream()
                .map(app -> convertToSimilarVO(app, "同分类高评分应用"))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppSimilarVO> getDeveloperApps(String appId, Integer limit) {
        log.info("获取同开发商应用: appId={}, limit={}", appId, limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        App currentApp = getAppByIdOrAppStoreId(appId);
        if (currentApp == null || currentApp.getDeveloperName() == null) {
            return new ArrayList<>();
        }

        // 查找同开发商的其他应用
        LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(App::getDeveloperName, currentApp.getDeveloperName())
                   .ne(App::getAppId, currentApp.getAppId())
                   .orderByDesc(App::getRating)
                   .last("LIMIT " + limit);

        List<App> developerApps = appMapper.selectList(queryWrapper);

        return developerApps.stream()
                .map(app -> convertToSimilarVO(app, "同一开发商"))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppSimilarVO> getCategoryTopApps(String appId, Integer limit) {
        log.info("获取分类热门应用: appId={}, limit={}", appId, limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        App currentApp = getAppByIdOrAppStoreId(appId);
        if (currentApp == null) {
            return new ArrayList<>();
        }

        // 查找分类下的热门应用
        LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(App::getPrimaryCategoryId, currentApp.getPrimaryCategoryId())
                   .ne(App::getAppId, currentApp.getAppId())
                   .orderByDesc(App::getRatingCount)
                   .orderByDesc(App::getRating)
                   .last("LIMIT " + limit);

        List<App> topApps = appMapper.selectList(queryWrapper);

        return topApps.stream()
                .map(app -> convertToSimilarVO(app, "分类热门应用"))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean increaseViewCount(String appId) {
        try {
            App app = getAppByIdOrAppStoreId(appId);
            if (app == null) {
                return false;
            }

            // 这里可以添加查看次数统计逻辑
            // 例如：更新统计表或Redis计数器
            log.info("记录应用查看: {}", appId);
            return true;

        } catch (Exception e) {
            log.error("增加查看次数失败: {}", appId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean recordDownload(String appId) {
        try {
            App app = getAppByIdOrAppStoreId(appId);
            if (app == null) {
                return false;
            }

            // 这里可以添加下载统计逻辑
            log.info("记录应用下载: {}", appId);
            return true;

        } catch (Exception e) {
            log.error("记录下载失败: {}", appId, e);
            return false;
        }
    }

    @Override
    public String getAppStoreUrl(String appId) {
        App app = getAppByIdOrAppStoreId(appId);
        if (app != null) {
            return String.format("https://apps.apple.com/cn/app/id%s", app.getAppId());
        }
        return null;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 根据ID或App Store ID获取应用
     */
    private App getAppByIdOrAppStoreId(String appId) {
        // 先尝试作为内部ID查询
        App app = appMapper.selectById(appId);
        if (app != null) {
            return app;
        }

        // 再尝试作为App Store ID查询
        LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(App::getAppId, appId);
        return appMapper.selectOne(queryWrapper);
    }

    /**
     * 获取活跃的限免信息
     */
    private FreePromotion getActivePromotion(String appStoreId) {
        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FreePromotion::getAppstoreAppId, appStoreId)
                   .eq(FreePromotion::getStatus, "ACTIVE")
                   .orderByDesc(FreePromotion::getStartTime)
                   .last("LIMIT 1");
        return freePromotionMapper.selectOne(queryWrapper);
    }

    /**
     * 获取价格历史
     */
    private List<AppPriceHistory> getPriceHistory(String appStoreId, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return appPriceHistoryMapper.getPriceHistoryByTimeRange(
                appStoreId, startTime, LocalDateTime.now());
    }

    /**
     * 转换为详情VO
     */
    private AppDetailVO convertToDetailVO(App app) {
        AppDetailVO vo = new AppDetailVO();
        vo.setAppId(app.getId());
        vo.setAppstoreId(app.getAppId());
        vo.setBundleId(app.getBundleId());
        vo.setName(app.getName());
        vo.setDescription(app.getDescription());
        vo.setDeveloperName(app.getDeveloperName());
        vo.setDeveloperId(app.getDeveloperId());
        vo.setDeveloperUrl(app.getDeveloperUrl());
        vo.setIconUrl(app.getIconUrl());
        vo.setScreenshots(app.getScreenshots());
        vo.setIpadScreenshots(app.getIpadScreenshots());
        vo.setVersion(app.getVersion());
        vo.setReleaseDate(app.getReleaseDate());
        vo.setUpdatedDate(app.getUpdatedDate());
        vo.setReleaseNotes(app.getReleaseNotes());
        vo.setFileSize(app.getFileSize());
        vo.setMinimumOsVersion(app.getMinimumOsVersion());
        vo.setRating(app.getRating());
        vo.setRatingCount(app.getRatingCount());
        vo.setCurrentVersionRating(app.getCurrentVersionRating());
        vo.setCurrentVersionRatingCount(app.getCurrentVersionRatingCount());
        vo.setOriginalPrice(app.getOriginalPrice());
        vo.setCurrentPrice(app.getCurrentPrice());
        vo.setCurrency(app.getCurrency());
        vo.setIsFreeNow(app.getCurrentPrice() != null &&
                       app.getCurrentPrice().compareTo(BigDecimal.ZERO) == 0);
        vo.setContentRating(app.getContentRating());
        vo.setLanguages(app.getLanguages());
        vo.setSupportedDevices(app.getSupportedDevices());

        // 设置分类信息
        if (app.getPrimaryCategoryId() != null) {
            AppDetailVO.CategoryInfo primaryCategory = new AppDetailVO.CategoryInfo();
            primaryCategory.setCategoryId(app.getPrimaryCategoryId());
            primaryCategory.setName(app.getPrimaryCategoryName());
            vo.setPrimaryCategory(primaryCategory);
        }

        // 设置所有分类
        if (app.getCategories() != null && !app.getCategories().isEmpty()) {
            List<AppDetailVO.CategoryInfo> categories = new ArrayList<>();
            for (App.CategoryInfo catInfo : app.getCategories()) {
                AppDetailVO.CategoryInfo category = new AppDetailVO.CategoryInfo();
                category.setCategoryId(catInfo.getId());
                category.setName(catInfo.getName());
                categories.add(category);
            }
            vo.setCategories(categories);
        }

        // 格式化文件大小
        if (app.getFileSize() != null) {
            vo.setFileSizeFormatted(formatFileSize(app.getFileSize()));
        }

        return vo;
    }

    /**
     * 转换为相似应用VO
     */
    private AppSimilarVO convertToSimilarVO(App app, String recommendReason) {
        AppSimilarVO vo = new AppSimilarVO();
        vo.setId(app.getId());
        vo.setAppId(app.getAppId());
        vo.setName(app.getName());
        vo.setIconUrl(app.getIconUrl());
        vo.setBundleId(app.getBundleId());
        vo.setDeveloperName(app.getDeveloperName());
        vo.setCategoryName(app.getPrimaryCategoryName());
        vo.setCurrentPrice(app.getCurrentPrice());
        vo.setOriginalPrice(app.getOriginalPrice());
        vo.setIsFree(app.getIsFree());
        vo.setRating(app.getRating());
        vo.setRatingCount(app.getRatingCount());
        vo.setFileSize(app.getFileSize());
        vo.setVersion(app.getVersion());
        vo.setRecommendReason(recommendReason);

        // 检查是否有限免
        FreePromotion promotion = getActivePromotion(app.getAppId());
        vo.setHasPromotion(promotion != null);

        // 生成简短描述
        if (app.getDescription() != null && app.getDescription().length() > 100) {
            vo.setShortDescription(app.getDescription().substring(0, 100) + "...");
        } else {
            vo.setShortDescription(app.getDescription());
        }

        return vo;
    }

    /**
     * 构建限免信息
     */
    private AppDetailVO.FreePromotionInfo buildPromotionInfo(FreePromotion promotion) {
        AppDetailVO.FreePromotionInfo info = new AppDetailVO.FreePromotionInfo();
        info.setStartTime(promotion.getStartTime());
        info.setEndTime(promotion.getEndTime());
        info.setSavingsAmount(promotion.getSavingsAmount());

        // 计算剩余时间
        if (promotion.getEndTime() != null) {
            long hours = java.time.Duration.between(LocalDateTime.now(), promotion.getEndTime()).toHours();
            info.setRemainingHours(Math.max(0, (int) hours));
            info.setIsEndingSoon(hours <= 6);
        }

        return info;
    }

    /**
     * 构建价格历史信息
     */
    private List<AppDetailVO.PriceHistoryInfo> buildPriceHistoryInfo(List<AppPriceHistory> historyList) {
        return historyList.stream()
                .sorted(Comparator.comparing(AppPriceHistory::getChangeTime).reversed())
                .limit(10) // 只取最近10条
                .map(history -> {
                    AppDetailVO.PriceHistoryInfo info = new AppDetailVO.PriceHistoryInfo();
                    info.setPrice(history.getNewPrice());
                    info.setRecordTime(history.getChangeTime());
                    info.setChangeType(history.getChangeType());
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建价格数据点
     */
    private List<AppPriceChartVO.PricePoint> buildPricePoints(List<AppPriceHistory> historyList) {
        return historyList.stream()
                .map(history -> {
                    AppPriceChartVO.PricePoint point = new AppPriceChartVO.PricePoint();
                    point.setTime(history.getChangeTime());
                    point.setPrice(history.getNewPrice());
                    point.setIsFree(history.getNewPrice().compareTo(BigDecimal.ZERO) == 0);
                    point.setChangeType(history.getChangeType());
                    return point;
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建价格事件
     */
    private List<AppPriceChartVO.PriceEvent> buildPriceEvents(List<AppPriceHistory> historyList) {
        return historyList.stream()
                .filter(h -> h.getChangeType() != null && !"INITIAL".equals(h.getChangeType()))
                .map(history -> {
                    AppPriceChartVO.PriceEvent event = new AppPriceChartVO.PriceEvent();
                    event.setEventTime(history.getChangeTime());
                    event.setEventType(history.getChangeType());
                    event.setOldPrice(history.getOldPrice());
                    event.setNewPrice(history.getNewPrice());
                    event.setChangeAmount(history.getPriceChange());
                    event.setChangePercent(history.getChangePercent());
                    return event;
                })
                .collect(Collectors.toList());
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        double fileSize = size;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", fileSize, units[unitIndex]);
    }
}