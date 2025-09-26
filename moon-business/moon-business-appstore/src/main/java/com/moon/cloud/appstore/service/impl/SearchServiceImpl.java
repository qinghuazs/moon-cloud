package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.SearchDTO;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.entity.SearchHistory;
import com.moon.cloud.appstore.entity.SearchIndex;
import com.moon.cloud.appstore.mapper.*;
import com.moon.cloud.appstore.service.SearchService;
import com.moon.cloud.appstore.vo.FreeAppVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchIndexMapper searchIndexMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final AppMapper appMapper;
    private final FreePromotionMapper freePromotionMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String HOT_SEARCH_KEY = "appstore:hot:search";
    private static final String SEARCH_SUGGESTION_KEY = "appstore:search:suggestion:";
    private static final String USER_SEARCH_HISTORY_KEY = "appstore:search:history:";

    @Override
    public Page<FreeAppVO> searchApps(SearchDTO dto) {
        Page<App> page = new Page<>(dto.getPage(), dto.getPageSize());

        // 使用全文搜索
        List<SearchIndex> searchResults = searchIndexMapper.fullTextSearch(dto.getKeyword());
        if (searchResults.isEmpty()) {
            return new Page<>(dto.getPage(), dto.getPageSize(), 0);
        }

        // 获取应用ID列表
        List<String> appIds = searchResults.stream()
            .map(SearchIndex::getAppId)
            .collect(Collectors.toList());

        // 查询应用信息
        LambdaQueryWrapper<App> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(App::getId, appIds);

        // 分类筛选
        if (StringUtils.isNotBlank(dto.getCategoryId())) {
            wrapper.eq(App::getPrimaryCategoryId, dto.getCategoryId());
        }

        // 评分筛选
        if (dto.getMinRating() != null) {
            wrapper.ge(App::getRating, dto.getMinRating());
        }

        // 是否只显示限免应用
        if (dto.getOnlyFree()) {
            wrapper.eq(App::getCurrentPrice, 0);
        }

        Page<App> appPage = appMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<FreeAppVO> voPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        List<FreeAppVO> voList = appPage.getRecords().stream()
            .map(this::convertToFreeAppVO)
            .collect(Collectors.toList());
        voPage.setRecords(voList);

        // 更新热搜
        updateHotSearch(dto.getKeyword());

        return voPage;
    }

    @Override
    public List<String> getSearchSuggestions(String keyword) {
        // 先从缓存获取
        String cacheKey = SEARCH_SUGGESTION_KEY + keyword;
        Set<String> cached = redisTemplate.opsForSet().members(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return new ArrayList<>(cached);
        }

        // 查询数据库
        LambdaQueryWrapper<SearchIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SearchIndex::getAppName, keyword)
               .or()
               .like(SearchIndex::getAppNamePinyin, keyword)
               .or()
               .like(SearchIndex::getDeveloperName, keyword)
               .orderByDesc(SearchIndex::getSearchWeight)
               .last("LIMIT 10");

        List<SearchIndex> indexes = searchIndexMapper.selectList(wrapper);
        List<String> suggestions = indexes.stream()
            .map(SearchIndex::getAppName)
            .distinct()
            .collect(Collectors.toList());

        // 缓存结果
        if (!suggestions.isEmpty()) {
            redisTemplate.opsForSet().add(cacheKey, suggestions.toArray(new String[0]));
            redisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
        }

        return suggestions;
    }

    @Override
    public List<String> getHotSearchKeywords(int limit) {
        Set<ZSetOperations.TypedTuple<String>> hotSearches =
            redisTemplate.opsForZSet().reverseRangeWithScores(HOT_SEARCH_KEY, 0, limit - 1);

        if (hotSearches == null || hotSearches.isEmpty()) {
            // 从数据库获取
            return getHotSearchFromDB(limit);
        }

        return hotSearches.stream()
            .map(ZSetOperations.TypedTuple::getValue)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveSearchHistory(SearchHistory searchHistory) {
        searchHistory.setSearchedAt(LocalDateTime.now());
        searchHistoryMapper.insert(searchHistory);

        // 更新用户搜索历史缓存
        String userKey = getUserSearchKey(searchHistory.getUserId(), searchHistory.getDeviceId());
        redisTemplate.opsForList().leftPush(userKey, searchHistory.getSearchQuery());
        redisTemplate.opsForList().trim(userKey, 0, 19); // 只保留最近20条
        redisTemplate.expire(userKey, 30, TimeUnit.DAYS);
    }

    @Override
    public List<String> getUserSearchHistory(String userId, String deviceId, int limit) {
        String userKey = getUserSearchKey(userId, deviceId);
        List<String> history = redisTemplate.opsForList().range(userKey, 0, limit - 1);

        if (history == null || history.isEmpty()) {
            // 从数据库获取
            LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.isNotBlank(userId)) {
                wrapper.eq(SearchHistory::getUserId, userId);
            } else if (StringUtils.isNotBlank(deviceId)) {
                wrapper.eq(SearchHistory::getDeviceId, deviceId);
            } else {
                return new ArrayList<>();
            }

            wrapper.orderByDesc(SearchHistory::getSearchedAt)
                   .last("LIMIT " + limit);

            List<SearchHistory> histories = searchHistoryMapper.selectList(wrapper);
            history = histories.stream()
                .map(SearchHistory::getSearchQuery)
                .distinct()
                .collect(Collectors.toList());
        }

        return history;
    }

    @Override
    @Transactional
    public void clearSearchHistory(String userId, String deviceId) {
        // 清除缓存
        String userKey = getUserSearchKey(userId, deviceId);
        redisTemplate.delete(userKey);

        // 清除数据库记录
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(userId)) {
            wrapper.eq(SearchHistory::getUserId, userId);
        } else if (StringUtils.isNotBlank(deviceId)) {
            wrapper.eq(SearchHistory::getDeviceId, deviceId);
        }
        searchHistoryMapper.delete(wrapper);
    }

    private void updateHotSearch(String keyword) {
        redisTemplate.opsForZSet().incrementScore(HOT_SEARCH_KEY, keyword, 1.0);
        // 只保留前100个热搜词
        Long size = redisTemplate.opsForZSet().size(HOT_SEARCH_KEY);
        if (size != null && size > 100) {
            redisTemplate.opsForZSet().removeRange(HOT_SEARCH_KEY, 0, size - 101);
        }
    }

    private List<String> getHotSearchFromDB(int limit) {
        // 查询最近7天的搜索记录
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SearchHistory::getSearchedAt, sevenDaysAgo)
               .groupBy(SearchHistory::getSearchQuery);

        List<SearchHistory> histories = searchHistoryMapper.selectList(wrapper);

        // 统计搜索次数
        Map<String, Long> searchCounts = histories.stream()
            .collect(Collectors.groupingBy(
                SearchHistory::getSearchQuery,
                Collectors.counting()
            ));

        // 排序并返回前N个
        return searchCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private String getUserSearchKey(String userId, String deviceId) {
        if (StringUtils.isNotBlank(userId)) {
            return USER_SEARCH_HISTORY_KEY + "user:" + userId;
        } else if (StringUtils.isNotBlank(deviceId)) {
            return USER_SEARCH_HISTORY_KEY + "device:" + deviceId;
        }
        return USER_SEARCH_HISTORY_KEY + "anonymous";
    }

    private FreeAppVO convertToFreeAppVO(App app) {
        FreeAppVO vo = new FreeAppVO();
        vo.setAppId(app.getId());
        vo.setAppstoreId(app.getAppId());
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
        vo.setOriginalPrice(app.getOriginalPrice());
        vo.setCurrentPrice(app.getCurrentPrice());

        // 格式化文件大小
        if (app.getFileSize() != null) {
            vo.setFileSizeFormatted(formatFileSize(app.getFileSize()));
        }

        // 查询限免信息
        LambdaQueryWrapper<FreePromotion> promotionWrapper = new LambdaQueryWrapper<>();
        promotionWrapper.eq(FreePromotion::getAppId, app.getId())
                       .eq(FreePromotion::getStatus, "ACTIVE")
                       .last("LIMIT 1");
        FreePromotion promotion = freePromotionMapper.selectOne(promotionWrapper);

        if (promotion != null) {
            vo.setSavingsAmount(promotion.getSavingsAmount());
            vo.setFreeStartTime(promotion.getStartTime());
            vo.setFreeEndTime(promotion.getEndTime());

            if (promotion.getEndTime() != null) {
                long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), promotion.getEndTime());
                vo.setRemainingHours((int) Math.max(0, hours));
                vo.setIsEndingSoon(hours <= 6);
            }

            vo.setIsFeatured(promotion.getIsFeatured());
            vo.setIsHot(promotion.getIsHot());

            long hoursSinceDiscovered = ChronoUnit.HOURS.between(promotion.getDiscoveredAt(), LocalDateTime.now());
            vo.setIsNewFound(hoursSinceDiscovered <= 6);
        }

        // 生成状态标签
        List<String> tags = new ArrayList<>();
        if (vo.getIsNewFound() != null && vo.getIsNewFound()) tags.add("新发现");
        if (vo.getIsHot() != null && vo.getIsHot()) tags.add("热门");
        if (vo.getIsEndingSoon() != null && vo.getIsEndingSoon()) tags.add("即将结束");
        if (vo.getIsFeatured() != null && vo.getIsFeatured()) tags.add("编辑推荐");
        vo.setStatusTags(tags);

        return vo;
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