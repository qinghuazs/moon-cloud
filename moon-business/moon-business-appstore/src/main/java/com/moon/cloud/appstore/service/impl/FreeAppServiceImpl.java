package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppListDTO;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.entity.PriceHistory;
import com.moon.cloud.appstore.mapper.AppMapper;
import com.moon.cloud.appstore.mapper.FreePromotionMapper;
import com.moon.cloud.appstore.mapper.PriceHistoryMapper;
import com.moon.cloud.appstore.service.FreeAppService;
import com.moon.cloud.appstore.vo.AppDetailVO;
import com.moon.cloud.appstore.vo.FreeAppVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 限免应用服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FreeAppServiceImpl implements FreeAppService {

    private final AppMapper appMapper;
    private final FreePromotionMapper freePromotionMapper;
    private final PriceHistoryMapper priceHistoryMapper;

    @Override
    public Page<FreeAppVO> getTodayFreeApps(FreeAppListDTO dto) {
        // 构建查询条件
        LambdaQueryWrapper<FreePromotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FreePromotion::getStatus, "ACTIVE");
        wrapper.ge(FreePromotion::getStartTime, LocalDate.now());

        // 筛选条件
        if ("featured".equals(dto.getFilter())) {
            wrapper.eq(FreePromotion::getIsFeatured, true);
        } else if ("hot".equals(dto.getFilter())) {
            wrapper.eq(FreePromotion::getIsHot, true);
        } else if ("ending".equals(dto.getFilter())) {
            LocalDateTime endingSoon = LocalDateTime.now().plusHours(6);
            wrapper.le(FreePromotion::getEndTime, endingSoon);
        }

        // 先查询总记录数
        Long total = freePromotionMapper.selectCount(wrapper);

        // 排序方式
        if ("savings".equals(dto.getSortBy())) {
            wrapper.orderByDesc(FreePromotion::getSavingsAmount);
        } else if ("rating".equals(dto.getSortBy())) {
            // 需要关联查询，这里简化处理
            wrapper.orderByDesc(FreePromotion::getPriorityScore);
        } else {
            wrapper.orderByDesc(FreePromotion::getDiscoveredAt);
        }

        // 手动设置分页
        int page = dto.getPage() > 0 ? dto.getPage() : 1;
        int pageSize = dto.getPageSize() > 0 ? dto.getPageSize() : 20;
        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        // 执行查询
        List<FreePromotion> promotions = freePromotionMapper.selectList(wrapper);

        // 构建分页结果
        Page<FreeAppVO> voPage = new Page<>();
        voPage.setCurrent(page);           // 当前页码
        voPage.setSize(pageSize);          // 每页大小
        voPage.setTotal(total);            // 总记录数

        // 转换记录
        List<FreeAppVO> voList = promotions.stream()
            .map(this::convertToFreeAppVO)
            .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AppDetailVO getAppDetail(String appId) {
        // 查询应用基本信息
        App app = appMapper.selectById(appId);
        if (app == null) {
            throw new RuntimeException("应用不存在");
        }

        // 转换为详情VO
        AppDetailVO vo = convertToAppDetailVO(app);

        // 查询限免信息
        LambdaQueryWrapper<FreePromotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FreePromotion::getAppId, appId)
               .eq(FreePromotion::getStatus, "ACTIVE")
               .orderByDesc(FreePromotion::getStartTime)
               .last("LIMIT 1");
        FreePromotion promotion = freePromotionMapper.selectOne(wrapper);

        if (promotion != null) {
            vo.setIsFreeNow(true);
            vo.setFreePromotion(convertToFreePromotionInfo(promotion));
        }

        // 查询价格历史
        LambdaQueryWrapper<PriceHistory> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(PriceHistory::getAppId, appId)
                     .orderByDesc(PriceHistory::getRecordTime)
                     .last("LIMIT 30");
        List<PriceHistory> priceHistoryList = priceHistoryMapper.selectList(historyWrapper);
        vo.setPriceHistory(convertToPriceHistoryInfo(priceHistoryList));

        return vo;
    }

    @Override
    @Transactional
    public void increaseViewCount(String appId) {
        LambdaQueryWrapper<FreePromotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FreePromotion::getAppId, appId)
               .eq(FreePromotion::getStatus, "ACTIVE");

        FreePromotion promotion = freePromotionMapper.selectOne(wrapper);
        if (promotion != null) {
            promotion.setViewCount(promotion.getViewCount() + 1);
            freePromotionMapper.updateById(promotion);
        }
    }

    @Override
    @Transactional
    public void increaseClickCount(String appId) {
        LambdaQueryWrapper<FreePromotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FreePromotion::getAppId, appId)
               .eq(FreePromotion::getStatus, "ACTIVE");

        FreePromotion promotion = freePromotionMapper.selectOne(wrapper);
        if (promotion != null) {
            promotion.setClickCount(promotion.getClickCount() + 1);
            freePromotionMapper.updateById(promotion);
        }
    }

    @Override
    @Transactional
    public void increaseShareCount(String appId) {
        LambdaQueryWrapper<FreePromotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FreePromotion::getAppId, appId)
               .eq(FreePromotion::getStatus, "ACTIVE");

        FreePromotion promotion = freePromotionMapper.selectOne(wrapper);
        if (promotion != null) {
            promotion.setShareCount(promotion.getShareCount() + 1);
            freePromotionMapper.updateById(promotion);
        }
    }

    private FreeAppVO convertToFreeAppVO(FreePromotion promotion) {
        // 查询应用信息
        App app = appMapper.selectById(promotion.getAppId());

        FreeAppVO vo = new FreeAppVO();
        vo.setAppId(promotion.getAppId());
        vo.setAppstoreId(promotion.getAppstoreAppId());

        if (app != null) {
            vo.setName(app.getName());
            vo.setSubtitle(app.getSubtitle());
            vo.setDeveloperName(app.getDeveloperName());
            vo.setIconUrl(app.getIconUrl());
            vo.setCategoryName(app.getPrimaryCategoryName());
            vo.setVersion(app.getVersion());
            vo.setRating(app.getRating());
            vo.setRatingCount(app.getRatingCount());
            vo.setSupportedDevices(app.getSupportedDevices());
            vo.setHasInAppPurchase(app.getHasInAppPurchase());
            vo.setHasAds(app.getHasAds());

            // 设置App Store URL
            if (app.getAppUrl() != null) {
                vo.setAppUrl(app.getAppUrl());
            } else {
                // 如果没有保存URL，生成默认的
                vo.setAppUrl(String.format("https://apps.apple.com/cn/app/id%s", promotion.getAppstoreAppId()));
            }

            // 格式化文件大小
            if (app.getFileSize() != null) {
                vo.setFileSizeFormatted(formatFileSize(app.getFileSize()));
            }
        } else {
            // 如果没有找到app记录，生成默认URL
            vo.setAppUrl(String.format("https://apps.apple.com/cn/app/id%s", promotion.getAppstoreAppId()));
        }

        vo.setOriginalPrice(promotion.getOriginalPrice());
        vo.setCurrentPrice(promotion.getPromotionPrice());
        vo.setSavingsAmount(promotion.getSavingsAmount());
        vo.setFreeStartTime(promotion.getStartTime());
        vo.setFreeEndTime(promotion.getEndTime());

        // 计算剩余时间
        if (promotion.getEndTime() != null) {
            long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), promotion.getEndTime());
            vo.setRemainingHours((int) Math.max(0, hours));
            vo.setIsEndingSoon(hours <= 6);
        }

        // 设置状态标签
        vo.setIsFeatured(promotion.getIsFeatured());
        vo.setIsHot(promotion.getIsHot());

        // 新发现标记
        long hoursSinceDiscovered = ChronoUnit.HOURS.between(promotion.getDiscoveredAt(), LocalDateTime.now());
        vo.setIsNewFound(hoursSinceDiscovered <= 6);

        // 生成状态标签
        List<String> tags = new ArrayList<>();
        if (vo.getIsNewFound()) tags.add("新发现");
        if (vo.getIsHot()) tags.add("热门");
        if (vo.getIsEndingSoon()) tags.add("即将结束");
        if (vo.getIsFeatured()) tags.add("编辑推荐");
        vo.setStatusTags(tags);

        return vo;
    }

    private AppDetailVO convertToAppDetailVO(App app) {
        AppDetailVO vo = new AppDetailVO();
        vo.setAppId(app.getId());
        vo.setAppstoreId(app.getAppId());
        vo.setBundleId(app.getBundleId());
        vo.setName(app.getName());
        vo.setSubtitle(app.getSubtitle());
        vo.setDescription(app.getDescription());
        vo.setDeveloperName(app.getDeveloperName());
        vo.setDeveloperId(app.getDeveloperId());
        vo.setDeveloperUrl(app.getDeveloperUrl());
        vo.setIconUrl(app.getIconUrl());
        vo.setScreenshots(app.getScreenshots());
        vo.setIpadScreenshots(app.getIpadScreenshots());
        vo.setPreviewVideoUrl(app.getPreviewVideoUrl());
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
        vo.setContentRating(app.getContentRating());
        vo.setLanguages(app.getLanguages());
        vo.setSupportedDevices(app.getSupportedDevices());
        vo.setFeatures(app.getFeatures());
        vo.setHasInAppPurchase(app.getHasInAppPurchase());
        vo.setHasAds(app.getHasAds());

        // 格式化文件大小
        if (app.getFileSize() != null) {
            vo.setFileSizeFormatted(formatFileSize(app.getFileSize()));
        }

        // 转换分类信息
        if (app.getPrimaryCategoryId() != null) {
            AppDetailVO.CategoryInfo categoryInfo = new AppDetailVO.CategoryInfo();
            categoryInfo.setCategoryId(app.getPrimaryCategoryId());
            categoryInfo.setName(app.getPrimaryCategoryName());
            vo.setPrimaryCategory(categoryInfo);
        }

        // 转换所有分类
        if (app.getCategories() != null) {
            List<AppDetailVO.CategoryInfo> categories = app.getCategories().stream()
                .map(cat -> {
                    AppDetailVO.CategoryInfo info = new AppDetailVO.CategoryInfo();
                    info.setCategoryId(cat.getId());
                    info.setName(cat.getName());
                    return info;
                })
                .collect(Collectors.toList());
            vo.setCategories(categories);
        }

        return vo;
    }

    private AppDetailVO.FreePromotionInfo convertToFreePromotionInfo(FreePromotion promotion) {
        AppDetailVO.FreePromotionInfo info = new AppDetailVO.FreePromotionInfo();
        info.setStartTime(promotion.getStartTime());
        info.setEndTime(promotion.getEndTime());
        info.setSavingsAmount(promotion.getSavingsAmount());

        if (promotion.getEndTime() != null) {
            long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), promotion.getEndTime());
            info.setRemainingHours((int) Math.max(0, hours));
            info.setIsEndingSoon(hours <= 6);
        }

        return info;
    }

    private List<AppDetailVO.PriceHistoryInfo> convertToPriceHistoryInfo(List<PriceHistory> historyList) {
        return historyList.stream()
            .map(history -> {
                AppDetailVO.PriceHistoryInfo info = new AppDetailVO.PriceHistoryInfo();
                info.setPrice(history.getPrice());
                info.setRecordTime(history.getRecordTime());
                info.setChangeType(history.getPriceChangeType());
                return info;
            })
            .collect(Collectors.toList());
    }

    private String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null) {
            return "未知";
        }

        double size = sizeInBytes.doubleValue();
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }
}