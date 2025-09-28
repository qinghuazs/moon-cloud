package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.AppSearchDTO;
import com.moon.cloud.appstore.entity.*;
import com.moon.cloud.appstore.mapper.*;
import com.moon.cloud.appstore.service.AppSearchService;
import com.moon.cloud.appstore.vo.AppSearchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 应用搜索服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppSearchServiceImpl implements AppSearchService {

    private final AppMapper appMapper;
    private final SearchIndexMapper searchIndexMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final FreePromotionMapper freePromotionMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String HOT_SEARCH_KEY = "appstore:search:hot";
    private static final String USER_SEARCH_HISTORY_KEY = "appstore:search:history:";
    private static final String SEARCH_SUGGESTIONS_KEY = "appstore:search:suggestions:";

    @Override
    public Page<AppSearchResultVO> searchApps(AppSearchDTO searchDTO) {
        log.info("搜索应用: {}", searchDTO);

        // 验证分页参数
        searchDTO.validatePagination();

        // 创建分页对象
        Page<App> page = new Page<>(searchDTO.getPage(), searchDTO.getSize());

        // 构建查询条件
        LambdaQueryWrapper<App> queryWrapper = buildSearchQuery(searchDTO);

        // 执行查询
        Page<App> resultPage = appMapper.selectPage(page, queryWrapper);

        // 转换为搜索结果VO
        Page<AppSearchResultVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AppSearchResultVO> voList = convertToSearchResultVO(resultPage.getRecords(), searchDTO.getKeyword(), searchDTO.getHighlight());
        voPage.setRecords(voList);

        // 记录搜索关键词到热搜
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            recordHotSearch(searchDTO.getKeyword());
        }

        return voPage;
    }

    @Override
    @Cacheable(value = "search:suggestions", key = "#keyword", unless = "#result.isEmpty()")
    public List<String> getSearchSuggestions(String keyword, Integer limit) {
        log.info("获取搜索建议: keyword={}, limit={}", keyword, limit);

        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 先从缓存获取
        String cacheKey = SEARCH_SUGGESTIONS_KEY + keyword;
        Set<String> cachedSuggestions = redisTemplate.opsForZSet().reverseRange(cacheKey, 0, limit - 1);
        if (cachedSuggestions != null && !cachedSuggestions.isEmpty()) {
            return new ArrayList<>(cachedSuggestions);
        }

        // 从数据库生成建议
        List<String> suggestions = generateSearchSuggestions(keyword, limit);

        // 缓存建议
        if (!suggestions.isEmpty()) {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            for (int i = 0; i < suggestions.size(); i++) {
                zSetOps.add(cacheKey, suggestions.get(i), suggestions.size() - i);
            }
            redisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
        }

        return suggestions;
    }

    @Override
    public List<String> getHotSearchKeywords(Integer limit) {
        log.info("获取热门搜索词: limit={}", limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Set<String> hotKeywords = redisTemplate.opsForZSet().reverseRange(HOT_SEARCH_KEY, 0, limit - 1);
        return hotKeywords != null ? new ArrayList<>(hotKeywords) : new ArrayList<>();
    }

    @Override
    @Transactional
    public void recordSearchHistory(String userId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        try {
            // 记录到数据库
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword);
            history.setSearchTime(LocalDateTime.now());
            history.setCreatedAt(LocalDateTime.now());
            searchHistoryMapper.insert(history);

            // 记录到Redis（用户历史）
            if (StringUtils.hasText(userId)) {
                String userHistoryKey = USER_SEARCH_HISTORY_KEY + userId;
                redisTemplate.opsForList().leftPush(userHistoryKey, keyword);
                redisTemplate.opsForList().trim(userHistoryKey, 0, 49); // 保留最近50条
                redisTemplate.expire(userHistoryKey, 30, TimeUnit.DAYS);
            }

            log.info("记录搜索历史: userId={}, keyword={}", userId, keyword);

        } catch (Exception e) {
            log.error("记录搜索历史失败", e);
        }
    }

    @Override
    public List<String> getUserSearchHistory(String userId, Integer limit) {
        log.info("获取用户搜索历史: userId={}, limit={}", userId, limit);

        if (!StringUtils.hasText(userId)) {
            return new ArrayList<>();
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 从Redis获取
        String userHistoryKey = USER_SEARCH_HISTORY_KEY + userId;
        List<String> history = redisTemplate.opsForList().range(userHistoryKey, 0, limit - 1);

        if (history == null || history.isEmpty()) {
            // 从数据库获取
            LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchHistory::getUserId, userId)
                       .orderByDesc(SearchHistory::getSearchTime)
                       .last("LIMIT " + limit);

            List<SearchHistory> dbHistory = searchHistoryMapper.selectList(queryWrapper);
            history = dbHistory.stream()
                    .map(SearchHistory::getKeyword)
                    .collect(Collectors.toList());
        }

        return history;
    }

    @Override
    public boolean clearUserSearchHistory(String userId) {
        log.info("清除用户搜索历史: userId={}", userId);

        if (!StringUtils.hasText(userId)) {
            return false;
        }

        try {
            // 清除Redis缓存
            String userHistoryKey = USER_SEARCH_HISTORY_KEY + userId;
            redisTemplate.delete(userHistoryKey);

            // 清除数据库记录
            LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchHistory::getUserId, userId);
            searchHistoryMapper.delete(queryWrapper);

            return true;

        } catch (Exception e) {
            log.error("清除搜索历史失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public int buildSearchIndex() {
        log.info("开始构建搜索索引");

        try {
            // 清除旧索引
            searchIndexMapper.delete(null);

            // 获取所有应用
            List<App> apps = appMapper.selectList(null);
            int count = 0;

            for (App app : apps) {
                SearchIndex index = new SearchIndex();
                index.setAppId(app.getId());
                index.setAppstoreId(app.getAppId());
                index.setName(app.getName());
                index.setDeveloperName(app.getDeveloperName());
                index.setDescription(app.getDescription());
                index.setCategoryName(app.getPrimaryCategoryName());
                index.setKeywords(buildKeywords(app));
                index.setRating(app.getRating());
                index.setDownloads(app.getRatingCount()); // 用评分人数近似下载量
                index.setUpdatedAt(LocalDateTime.now());

                searchIndexMapper.insert(index);
                count++;
            }

            log.info("搜索索引构建完成，索引应用数: {}", count);
            return count;

        } catch (Exception e) {
            log.error("构建搜索索引失败", e);
            throw new RuntimeException("构建搜索索引失败", e);
        }
    }

    @Override
    public boolean updateAppSearchIndex(String appId) {
        log.info("更新应用搜索索引: {}", appId);

        try {
            App app = appMapper.selectById(appId);
            if (app == null) {
                return false;
            }

            // 查找现有索引
            LambdaQueryWrapper<SearchIndex> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchIndex::getAppId, appId);
            SearchIndex existingIndex = searchIndexMapper.selectOne(queryWrapper);

            if (existingIndex != null) {
                // 更新索引
                existingIndex.setName(app.getName());
                existingIndex.setDeveloperName(app.getDeveloperName());
                existingIndex.setDescription(app.getDescription());
                existingIndex.setCategoryName(app.getPrimaryCategoryName());
                existingIndex.setKeywords(buildKeywords(app));
                existingIndex.setRating(app.getRating());
                existingIndex.setDownloads(app.getRatingCount());
                existingIndex.setUpdatedAt(LocalDateTime.now());

                searchIndexMapper.updateById(existingIndex);
            } else {
                // 创建新索引
                SearchIndex index = new SearchIndex();
                index.setAppId(app.getId());
                index.setAppstoreId(app.getAppId());
                index.setName(app.getName());
                index.setDeveloperName(app.getDeveloperName());
                index.setDescription(app.getDescription());
                index.setCategoryName(app.getPrimaryCategoryName());
                index.setKeywords(buildKeywords(app));
                index.setRating(app.getRating());
                index.setDownloads(app.getRatingCount());
                index.setUpdatedAt(LocalDateTime.now());

                searchIndexMapper.insert(index);
            }

            return true;

        } catch (Exception e) {
            log.error("更新搜索索引失败: {}", appId, e);
            return false;
        }
    }

    @Override
    public boolean deleteAppSearchIndex(String appId) {
        log.info("删除应用搜索索引: {}", appId);

        try {
            LambdaQueryWrapper<SearchIndex> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SearchIndex::getAppId, appId);
            searchIndexMapper.delete(queryWrapper);
            return true;

        } catch (Exception e) {
            log.error("删除搜索索引失败: {}", appId, e);
            return false;
        }
    }

    @Override
    public Page<AppSearchResultVO> advancedSearch(AppSearchDTO searchDTO) {
        log.info("高级搜索: {}", searchDTO);

        // 使用更复杂的搜索逻辑
        return searchApps(searchDTO);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 构建搜索查询条件
     */
    private LambdaQueryWrapper<App> buildSearchQuery(AppSearchDTO searchDTO) {
        LambdaQueryWrapper<App> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            String keyword = searchDTO.getKeyword().trim();

            if ("all".equals(searchDTO.getSearchScope())) {
                queryWrapper.and(wrapper -> wrapper
                        .like(App::getName, keyword)
                        .or().like(App::getDeveloperName, keyword)
                        .or().like(App::getDescription, keyword)
                        .or().like(App::getBundleId, keyword));
            } else if ("name".equals(searchDTO.getSearchScope())) {
                queryWrapper.like(App::getName, keyword);
            } else if ("developer".equals(searchDTO.getSearchScope())) {
                queryWrapper.like(App::getDeveloperName, keyword);
            } else if ("description".equals(searchDTO.getSearchScope())) {
                queryWrapper.like(App::getDescription, keyword);
            }
        }

        // 分类筛选
        if (searchDTO.getCategoryIds() != null && !searchDTO.getCategoryIds().isEmpty()) {
            queryWrapper.in(App::getPrimaryCategoryId, searchDTO.getCategoryIds());
        }

        // 开发者筛选
        if (StringUtils.hasText(searchDTO.getDeveloperName())) {
            queryWrapper.eq(App::getDeveloperName, searchDTO.getDeveloperName());
        }

        // 价格筛选
        if (searchDTO.getMinPrice() != null) {
            queryWrapper.ge(App::getCurrentPrice, searchDTO.getMinPrice());
        }
        if (searchDTO.getMaxPrice() != null) {
            queryWrapper.le(App::getCurrentPrice, searchDTO.getMaxPrice());
        }
        if (Boolean.TRUE.equals(searchDTO.getIsFree())) {
            queryWrapper.eq(App::getIsFree, true);
        }

        // 评分筛选
        if (searchDTO.getMinRating() != null) {
            queryWrapper.ge(App::getRating, searchDTO.getMinRating());
        }

        // 排序
        String sortBy = searchDTO.getSortBy();
        boolean isDesc = "desc".equalsIgnoreCase(searchDTO.getSortOrder());

        if ("rating".equals(sortBy)) {
            queryWrapper.orderBy(true, !isDesc, App::getRating);
        } else if ("downloads".equals(sortBy)) {
            queryWrapper.orderBy(true, !isDesc, App::getRatingCount);
        } else if ("price".equals(sortBy)) {
            queryWrapper.orderBy(true, !isDesc, App::getCurrentPrice);
        } else if ("updated".equals(sortBy)) {
            queryWrapper.orderBy(true, !isDesc, App::getUpdatedDate);
        } else {
            // 默认按相关度排序（这里简化为按评分和评分人数）
            queryWrapper.orderByDesc(App::getRating)
                       .orderByDesc(App::getRatingCount);
        }

        return queryWrapper;
    }

    /**
     * 转换搜索结果
     */
    private List<AppSearchResultVO> convertToSearchResultVO(List<App> apps, String keyword, Boolean highlight) {
        return apps.stream().map(app -> {
            AppSearchResultVO vo = new AppSearchResultVO();
            vo.setId(app.getId());
            vo.setAppId(app.getAppId());
            vo.setBundleId(app.getBundleId());

            // 处理高亮
            vo.setNameOriginal(app.getName());
            vo.setDeveloperNameOriginal(app.getDeveloperName());
            vo.setDescriptionOriginal(truncateDescription(app.getDescription()));

            if (Boolean.TRUE.equals(highlight) && StringUtils.hasText(keyword)) {
                vo.setName(AppSearchResultVO.highlightText(app.getName(), keyword));
                vo.setDeveloperName(AppSearchResultVO.highlightText(app.getDeveloperName(), keyword));
                vo.setDescription(AppSearchResultVO.highlightText(truncateDescription(app.getDescription()), keyword));
            } else {
                vo.setName(app.getName());
                vo.setDeveloperName(app.getDeveloperName());
                vo.setDescription(truncateDescription(app.getDescription()));
            }

            vo.setIconUrl(app.getIconUrl());
            vo.setCategoryName(app.getPrimaryCategoryName());
            vo.setCurrentPrice(app.getCurrentPrice());
            vo.setOriginalPrice(app.getOriginalPrice());
            vo.setIsFree(app.getIsFree());
            vo.setRating(app.getRating());
            vo.setRatingCount(app.getRatingCount());
            vo.setFileSize(app.getFileSize());
            vo.setVersion(app.getVersion());
            vo.setUpdatedDate(app.getUpdatedDate());
            vo.setSupportedDevices(app.getSupportedDevices());

            // 检查限免信息
            checkPromotion(vo, app.getAppId());

            // 设置截图（只取前3张）
            if (app.getScreenshots() != null && !app.getScreenshots().isEmpty()) {
                vo.setScreenshots(app.getScreenshots().stream()
                        .limit(3)
                        .collect(Collectors.toList()));
            }

            // 设置标签
            vo.setTags(generateTags(app, vo));

            // 设置匹配字段
            if (StringUtils.hasText(keyword)) {
                vo.setMatchedFields(findMatchedFields(app, keyword));
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 检查限免信息
     */
    private void checkPromotion(AppSearchResultVO vo, String appStoreId) {
        LambdaQueryWrapper<FreePromotion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FreePromotion::getAppstoreAppId, appStoreId)
                   .eq(FreePromotion::getStatus, "ACTIVE")
                   .orderByDesc(FreePromotion::getStartTime)
                   .last("LIMIT 1");

        FreePromotion promotion = freePromotionMapper.selectOne(queryWrapper);
        if (promotion != null) {
            vo.setHasPromotion(true);
            vo.setPromotionType(promotion.getPromotionType());
            vo.setSavingsAmount(promotion.getSavingsAmount());
        } else {
            vo.setHasPromotion(false);
        }
    }

    /**
     * 生成搜索建议
     */
    private List<String> generateSearchSuggestions(String keyword, int limit) {
        List<String> suggestions = new ArrayList<>();

        // 搜索应用名称
        LambdaQueryWrapper<App> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.like(App::getName, keyword)
                .select(App::getName)
                .last("LIMIT " + limit);
        List<App> nameMatches = appMapper.selectList(nameQuery);
        suggestions.addAll(nameMatches.stream()
                .map(App::getName)
                .distinct()
                .collect(Collectors.toList()));

        // 搜索开发者名称
        if (suggestions.size() < limit) {
            LambdaQueryWrapper<App> developerQuery = new LambdaQueryWrapper<>();
            developerQuery.like(App::getDeveloperName, keyword)
                    .select(App::getDeveloperName)
                    .last("LIMIT " + (limit - suggestions.size()));
            List<App> developerMatches = appMapper.selectList(developerQuery);
            suggestions.addAll(developerMatches.stream()
                    .map(App::getDeveloperName)
                    .distinct()
                    .filter(name -> !suggestions.contains(name))
                    .collect(Collectors.toList()));
        }

        return suggestions.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 记录热搜关键词
     */
    private void recordHotSearch(String keyword) {
        try {
            redisTemplate.opsForZSet().incrementScore(HOT_SEARCH_KEY, keyword, 1);
            redisTemplate.expire(HOT_SEARCH_KEY, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("记录热搜关键词失败", e);
        }
    }

    /**
     * 构建关键词索引
     */
    private String buildKeywords(App app) {
        List<String> keywords = new ArrayList<>();

        if (app.getName() != null) {
            keywords.add(app.getName());
        }
        if (app.getDeveloperName() != null) {
            keywords.add(app.getDeveloperName());
        }
        if (app.getPrimaryCategoryName() != null) {
            keywords.add(app.getPrimaryCategoryName());
        }
        if (app.getBundleId() != null) {
            keywords.add(app.getBundleId());
        }

        return String.join(",", keywords);
    }

    /**
     * 截取描述
     */
    private String truncateDescription(String description) {
        if (description == null) {
            return "";
        }
        if (description.length() > 200) {
            return description.substring(0, 200) + "...";
        }
        return description;
    }

    /**
     * 生成标签
     */
    private List<String> generateTags(App app, AppSearchResultVO vo) {
        List<String> tags = new ArrayList<>();

        if (Boolean.TRUE.equals(vo.getHasPromotion())) {
            if ("FREE".equals(vo.getPromotionType())) {
                tags.add("限免");
            } else if ("DISCOUNT".equals(vo.getPromotionType())) {
                tags.add("折扣");
            }
        }

        if (app.getRating() != null && app.getRating().compareTo(new BigDecimal("4.5")) >= 0) {
            tags.add("高分");
        }

        if (app.getRatingCount() != null && app.getRatingCount() > 10000) {
            tags.add("热门");
        }

        if (app.getUpdatedDate() != null &&
            app.getUpdatedDate().isAfter(LocalDateTime.now().minusDays(7))) {
            tags.add("最近更新");
        }

        return tags;
    }

    /**
     * 查找匹配的字段
     */
    private List<String> findMatchedFields(App app, String keyword) {
        List<String> matchedFields = new ArrayList<>();

        if (app.getName() != null && app.getName().toLowerCase().contains(keyword.toLowerCase())) {
            matchedFields.add("应用名称");
        }
        if (app.getDeveloperName() != null && app.getDeveloperName().toLowerCase().contains(keyword.toLowerCase())) {
            matchedFields.add("开发者");
        }
        if (app.getDescription() != null && app.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
            matchedFields.add("应用描述");
        }

        return matchedFields;
    }
}