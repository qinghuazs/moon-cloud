package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppQueryDTO;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.mapper.AppMapper;
import com.moon.cloud.appstore.mapper.FreePromotionMapper;
import com.moon.cloud.appstore.service.FreePromotionService;
import com.moon.cloud.appstore.vo.FreeAppStatisticsVO;
import com.moon.cloud.appstore.vo.FreePromotionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 限免推广服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FreePromotionServiceImpl implements FreePromotionService {

    private final FreePromotionMapper freePromotionMapper;
    private final AppMapper appMapper;

    @Override
    public Page<FreePromotionVO> getTodayFreeApps(FreeAppQueryDTO queryDTO) {
        log.info("查询今日限免应用，参数: {}", queryDTO);

        // 创建分页对象
        Page<FreePromotion> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // 构建查询条件
        LambdaQueryWrapper<FreePromotion> queryWrapper = buildQueryWrapper(queryDTO);

        // 添加今日条件
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        queryWrapper.ge(FreePromotion::getDiscoveredAt, todayStart)
                   .le(FreePromotion::getDiscoveredAt, todayEnd);

        // 执行查询
        Page<FreePromotion> resultPage = freePromotionMapper.selectPage(page, queryWrapper);

        // 转换为VO并填充应用信息
        Page<FreePromotionVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<FreePromotionVO> voList = convertToVOList(resultPage.getRecords());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Cacheable(value = "free:ending", key = "#hours", unless = "#result.isEmpty()")
    public List<FreePromotionVO> getEndingSoonApps(int hours) {
        log.info("查询{}小时内即将结束的限免应用", hours);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hours);

        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FreePromotion::getStatus, "ACTIVE")
                   .isNotNull(FreePromotion::getEndTime)
                   .between(FreePromotion::getEndTime, now, endTime)
                   .orderByAsc(FreePromotion::getEndTime);

        List<FreePromotion> promotions = freePromotionMapper.selectList(queryWrapper);
        return convertToVOList(promotions);
    }

    @Override
    @Cacheable(value = "free:hot", key = "#limit", unless = "#result.isEmpty()")
    public List<FreePromotionVO> getHotFreeApps(int limit) {
        log.info("查询热门限免应用，限制: {}", limit);

        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FreePromotion::getStatus, "ACTIVE")
                   .orderByDesc(FreePromotion::getPriorityScore)
                   .orderByDesc(FreePromotion::getViewCount)
                   .last("LIMIT " + limit);

        List<FreePromotion> promotions = freePromotionMapper.selectList(queryWrapper);

        // 获取并填充应用信息
        List<FreePromotionVO> voList = convertToVOList(promotions);

        // 计算热度分数并重新排序
        voList.forEach(vo -> {
            vo.setHotScore(vo.getHotScore());
            vo.setIsHot(true);
        });

        voList.sort((a, b) -> b.getHotScore().compareTo(a.getHotScore()));

        return voList;
    }

    @Override
    @Cacheable(value = "free:statistics", unless = "#result == null")
    public FreeAppStatisticsVO getFreeAppStatistics() {
        log.info("获取限免统计信息");

        FreeAppStatisticsVO statistics = new FreeAppStatisticsVO();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 今日限免统计
        LambdaQueryWrapper<FreePromotion> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.between(FreePromotion::getDiscoveredAt, todayStart, todayEnd);

        List<FreePromotion> todayPromotions = freePromotionMapper.selectList(todayQuery);

        statistics.setTodayFreeCount((int) todayPromotions.stream()
                .filter(p -> "FREE".equals(p.getPromotionType()))
                .count());

        statistics.setTodayDiscountCount((int) todayPromotions.stream()
                .filter(p -> "DISCOUNT".equals(p.getPromotionType()))
                .count());

        statistics.setTodayNewFreeCount(todayPromotions.size());

        // 活跃限免统计
        LambdaQueryWrapper<FreePromotion> activeQuery = new LambdaQueryWrapper<>();
        activeQuery.eq(FreePromotion::getStatus, "ACTIVE");

        List<FreePromotion> activePromotions = freePromotionMapper.selectList(activeQuery);

        statistics.setActiveFreeCount((int) activePromotions.stream()
                .filter(p -> "FREE".equals(p.getPromotionType()))
                .count());

        statistics.setActiveDiscountCount((int) activePromotions.stream()
                .filter(p -> "DISCOUNT".equals(p.getPromotionType()))
                .count());

        // 即将结束统计
        LocalDateTime sixHoursLater = LocalDateTime.now().plusHours(6);
        statistics.setEndingSoonCount((int) activePromotions.stream()
                .filter(p -> p.getEndTime() != null && p.getEndTime().isBefore(sixHoursLater))
                .count());

        // 计算今日总节省金额
        BigDecimal todayTotalSavings = todayPromotions.stream()
                .map(FreePromotion::getSavingsAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setTodayTotalSavings(todayTotalSavings);

        // 计算平均折扣率
        double avgDiscountRate = todayPromotions.stream()
                .map(FreePromotion::getDiscountRate)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
        statistics.setAverageDiscountRate(BigDecimal.valueOf(avgDiscountRate));

        // 最高节省金额
        todayPromotions.stream()
                .max(Comparator.comparing(p -> p.getSavingsAmount() != null ? p.getSavingsAmount() : BigDecimal.ZERO))
                .ifPresent(maxPromotion -> {
                    statistics.setMaxSavingsAmount(maxPromotion.getSavingsAmount());
                    statistics.setMaxSavingsAppId(maxPromotion.getAppstoreAppId());
                    // 获取应用名称
                    App app = appMapper.selectById(maxPromotion.getAppId());
                    if (app != null) {
                        statistics.setMaxSavingsAppName(app.getName());
                    }
                });

        // 分类分布统计
        Map<String, Integer> categoryDistribution = new HashMap<>();
        for (FreePromotion promotion : activePromotions) {
            App app = appMapper.selectById(promotion.getAppId());
            if (app != null && app.getPrimaryCategoryName() != null) {
                categoryDistribution.merge(app.getPrimaryCategoryName(), 1, Integer::sum);
            }
        }
        statistics.setCategoryDistribution(categoryDistribution);

        // 热门分类Top5
        Map<String, Integer> topCategories = categoryDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        statistics.setTopCategories(topCategories);

        // 本周和本月统计
        LocalDateTime weekStart = today.minusDays(6).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        LambdaQueryWrapper<FreePromotion> weekQuery = new LambdaQueryWrapper<>();
        weekQuery.ge(FreePromotion::getDiscoveredAt, weekStart);
        statistics.setWeeklyFreeCount(freePromotionMapper.selectCount(weekQuery).intValue());

        LambdaQueryWrapper<FreePromotion> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.ge(FreePromotion::getDiscoveredAt, monthStart);
        statistics.setMonthlyFreeCount(freePromotionMapper.selectCount(monthQuery).intValue());

        return statistics;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"free:ending", "free:hot", "free:statistics"}, allEntries = true)
    public void updatePromotionStatus() {
        log.info("开始更新限免状态");

        // 查询所有活跃的限免
        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FreePromotion::getStatus, "ACTIVE");

        List<FreePromotion> activePromotions = freePromotionMapper.selectList(queryWrapper);
        int updatedCount = 0;

        for (FreePromotion promotion : activePromotions) {
            boolean needUpdate = false;

            // 检查是否应该结束
            if (promotion.getEndTime() != null && LocalDateTime.now().isAfter(promotion.getEndTime())) {
                promotion.setStatus("ENDED");
                promotion.setActualEndTime(LocalDateTime.now());
                needUpdate = true;
            } else {
                // 检查应用当前价格是否仍然是优惠价格
                App app = appMapper.selectById(promotion.getAppId());
                if (app != null) {
                    BigDecimal currentPrice = app.getCurrentPrice();
                    BigDecimal originalPrice = app.getOriginalPrice();

                    if (currentPrice != null && originalPrice != null) {
                        // 如果价格恢复了，标记为结束
                        if (currentPrice.compareTo(originalPrice) >= 0) {
                            promotion.setStatus("ENDED");
                            promotion.setActualEndTime(LocalDateTime.now());
                            needUpdate = true;
                        }
                    }
                }
            }

            if (needUpdate) {
                promotion.setUpdatedAt(LocalDateTime.now());
                freePromotionMapper.updateById(promotion);
                updatedCount++;
                log.info("更新限免状态: appId={}, status={}", promotion.getAppstoreAppId(), promotion.getStatus());
            }
        }

        log.info("限免状态更新完成，更新了 {} 条记录", updatedCount);
    }

    @Override
    @Transactional
    public int detectNewPromotions() {
        log.info("开始检测新的限免应用");

        // 查询所有应用，检查价格变化
        List<App> apps = appMapper.selectList(null);
        int newPromotionsCount = 0;

        for (App app : apps) {
            if (app.getCurrentPrice() == null || app.getOriginalPrice() == null) {
                continue;
            }

            // 如果当前价格小于原始价格，可能是限免或打折
            if (app.getCurrentPrice().compareTo(app.getOriginalPrice()) < 0) {
                // 检查是否已经有活跃的限免记录
                LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(FreePromotion::getAppstoreAppId, app.getAppId())
                           .eq(FreePromotion::getStatus, "ACTIVE");

                FreePromotion existingPromotion = freePromotionMapper.selectOne(queryWrapper);

                if (existingPromotion == null) {
                    // 创建新的限免记录
                    FreePromotion promotion = new FreePromotion();
                    promotion.setAppId(app.getId());
                    promotion.setAppstoreAppId(app.getAppId());

                    if (app.getCurrentPrice().compareTo(BigDecimal.ZERO) == 0) {
                        promotion.setPromotionType("FREE");
                    } else {
                        promotion.setPromotionType("DISCOUNT");
                    }

                    promotion.setOriginalPrice(app.getOriginalPrice());
                    promotion.setPromotionPrice(app.getCurrentPrice());

                    BigDecimal savingsAmount = app.getOriginalPrice().subtract(app.getCurrentPrice());
                    promotion.setSavingsAmount(savingsAmount);

                    if (app.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal discountRate = savingsAmount.divide(app.getOriginalPrice(), 4, BigDecimal.ROUND_HALF_UP)
                                                                .multiply(new BigDecimal("100"));
                        promotion.setDiscountRate(discountRate);
                    }

                    promotion.setStartTime(LocalDateTime.now());
                    promotion.setDiscoveredAt(LocalDateTime.now());
                    promotion.setDiscoverySource("AUTO");
                    promotion.setStatus("ACTIVE");
                    promotion.setViewCount(0);
                    promotion.setClickCount(0);
                    promotion.setShareCount(0);
                    promotion.setCreatedAt(LocalDateTime.now());
                    promotion.setUpdatedAt(LocalDateTime.now());

                    freePromotionMapper.insert(promotion);
                    newPromotionsCount++;

                    log.info("发现新的限免应用: appId={}, type={}, savings={}",
                            app.getAppId(), promotion.getPromotionType(), savingsAmount);
                }
            }
        }

        log.info("新限免检测完成，发现 {} 个新限免应用", newPromotionsCount);
        return newPromotionsCount;
    }

    @Override
    public Page<FreePromotionVO> getFreeAppsByCategory(String categoryId, int page, int size) {
        log.info("查询分类 {} 的限免应用", categoryId);

        Page<FreePromotion> pageObj = new Page<>(page, size);

        // 需要关联查询App表来过滤分类
        // 这里简化处理，先查询所有活跃限免，然后过滤
        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FreePromotion::getStatus, "ACTIVE")
                   .orderByDesc(FreePromotion::getDiscoveredAt);

        Page<FreePromotion> resultPage = freePromotionMapper.selectPage(pageObj, queryWrapper);

        // 过滤分类并转换
        List<FreePromotionVO> voList = new ArrayList<>();
        for (FreePromotion promotion : resultPage.getRecords()) {
            App app = appMapper.selectById(promotion.getAppId());
            if (app != null && categoryId.equals(app.getPrimaryCategoryId())) {
                FreePromotionVO vo = convertToVO(promotion, app);
                voList.add(vo);
            }
        }

        Page<FreePromotionVO> voPage = new Page<>(page, size);
        voPage.setRecords(voList);
        voPage.setTotal(voList.size());

        return voPage;
    }

    @Override
    public List<FreePromotionVO> getPromotionHistory(String appId) {
        log.info("查询应用 {} 的限免历史", appId);

        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FreePromotion::getAppstoreAppId, appId)
                   .orderByDesc(FreePromotion::getStartTime);

        List<FreePromotion> promotions = freePromotionMapper.selectList(queryWrapper);
        return convertToVOList(promotions);
    }

    @Override
    @Transactional
    public boolean markAsViewed(String promotionId) {
        try {
            LambdaUpdateWrapper<FreePromotion> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FreePromotion::getId, promotionId)
                        .setSql("view_count = view_count + 1")
                        .set(FreePromotion::getUpdatedAt, LocalDateTime.now());

            int result = freePromotionMapper.update(null, updateWrapper);
            return result > 0;
        } catch (Exception e) {
            log.error("标记查看失败: {}", promotionId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean increaseClickCount(String promotionId) {
        try {
            LambdaUpdateWrapper<FreePromotion> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FreePromotion::getId, promotionId)
                        .setSql("click_count = click_count + 1")
                        .set(FreePromotion::getUpdatedAt, LocalDateTime.now());

            int result = freePromotionMapper.update(null, updateWrapper);
            return result > 0;
        } catch (Exception e) {
            log.error("增加点击次数失败: {}", promotionId, e);
            return false;
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<FreePromotion> buildQueryWrapper(FreeAppQueryDTO queryDTO) {
        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();

        // 状态条件
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(FreePromotion::getStatus, queryDTO.getStatus());
        } else {
            queryWrapper.eq(FreePromotion::getStatus, "ACTIVE");
        }

        // 推广类型
        if (StringUtils.hasText(queryDTO.getPromotionType())) {
            queryWrapper.eq(FreePromotion::getPromotionType, queryDTO.getPromotionType());
        }

        // 价格范围
        if (queryDTO.getMinOriginalPrice() != null) {
            queryWrapper.ge(FreePromotion::getOriginalPrice, queryDTO.getMinOriginalPrice());
        }
        if (queryDTO.getMaxOriginalPrice() != null) {
            queryWrapper.le(FreePromotion::getOriginalPrice, queryDTO.getMaxOriginalPrice());
        }

        // 特殊筛选
        if (Boolean.TRUE.equals(queryDTO.getOnlyNew())) {
            LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
            queryWrapper.ge(FreePromotion::getDiscoveredAt, sixHoursAgo);
        }

        if (Boolean.TRUE.equals(queryDTO.getOnlyEndingSoon())) {
            LocalDateTime sixHoursLater = LocalDateTime.now().plusHours(6);
            queryWrapper.isNotNull(FreePromotion::getEndTime)
                       .le(FreePromotion::getEndTime, sixHoursLater);
        }

        if (Boolean.TRUE.equals(queryDTO.getOnlyHot())) {
            queryWrapper.eq(FreePromotion::getIsHot, true);
        }

        if (Boolean.TRUE.equals(queryDTO.getOnlyFeatured())) {
            queryWrapper.eq(FreePromotion::getIsFeatured, true);
        }

        // 排序
        String sortColumn = queryDTO.getSortColumn();
        if ("desc".equalsIgnoreCase(queryDTO.getSortOrder())) {
            queryWrapper.orderByDesc(FreePromotion::getDiscoveredAt);
        } else {
            queryWrapper.orderByAsc(FreePromotion::getDiscoveredAt);
        }

        return queryWrapper;
    }

    /**
     * 转换为VO列表
     */
    private List<FreePromotionVO> convertToVOList(List<FreePromotion> promotions) {
        List<FreePromotionVO> voList = new ArrayList<>();

        for (FreePromotion promotion : promotions) {
            App app = appMapper.selectById(promotion.getAppId());
            if (app != null) {
                FreePromotionVO vo = convertToVO(promotion, app);
                voList.add(vo);
            }
        }

        return voList;
    }

    /**
     * 转换单个实体为VO
     */
    private FreePromotionVO convertToVO(FreePromotion promotion, App app) {
        FreePromotionVO vo = new FreePromotionVO();
        BeanUtils.copyProperties(promotion, vo);

        // 填充应用信息
        vo.setAppName(app.getName());
        vo.setIconUrl(app.getIconUrl());
        vo.setBundleId(app.getBundleId());
        vo.setDeveloperName(app.getDeveloperName());
        vo.setCategoryName(app.getPrimaryCategoryName());
        vo.setCategoryId(app.getPrimaryCategoryId());
        vo.setRating(app.getRating());
        vo.setRatingCount(app.getRatingCount());
        vo.setFileSize(app.getFileSize());
        vo.setVersion(app.getVersion());
        vo.setDescription(app.getDescription());
        vo.setScreenshots(app.getScreenshots());
        vo.setSupportedDevices(app.getSupportedDevices());
        vo.setLanguages(app.getLanguages());
        vo.setContentRating(app.getContentRating());

        // 计算动态字段
        vo.setFileSizeFormatted(vo.getFileSizeFormatted());
        vo.setRemainingHours(vo.getRemainingHours());
        vo.setIsNew(vo.getIsNew());
        vo.setIsEndingSoon(vo.getIsEndingSoon());
        vo.setHotScore(vo.getHotScore());

        // 添加标签
        List<String> tags = new ArrayList<>();
        if (vo.getIsNew()) {
            tags.add("新发现");
        }
        if (vo.getIsEndingSoon()) {
            tags.add("即将结束");
        }
        if (vo.getIsHot()) {
            tags.add("热门");
        }
        if (vo.getIsFeatured()) {
            tags.add("编辑推荐");
        }
        if ("FREE".equals(vo.getPromotionType())) {
            tags.add("限免");
        } else if ("DISCOUNT".equals(vo.getPromotionType())) {
            tags.add("折扣");
        }
        vo.setTags(tags);

        return vo;
    }
}