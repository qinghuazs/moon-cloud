package com.moon.cloud.appstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.AppSearchDTO;
import com.moon.cloud.appstore.service.AppSearchService;
import com.moon.cloud.appstore.vo.AppSearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用搜索控制器
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@RestController
@RequestMapping("/app/search")
@RequiredArgsConstructor
@Tag(name = "应用搜索", description = "应用搜索相关接口")
public class AppSearchController {

    private final AppSearchService appSearchService;

    @GetMapping
    @Operation(summary = "搜索应用", description = "根据关键词和筛选条件搜索应用")
    public Map<String, Object> searchApps(@ModelAttribute AppSearchDTO searchDTO) {
        log.info("搜索应用: keyword={}, page={}, size={}",
                searchDTO.getKeyword(), searchDTO.getPage(), searchDTO.getSize());

        // 验证分页参数
        searchDTO.validatePagination();

        // 执行搜索
        Page<AppSearchResultVO> searchResults = appSearchService.searchApps(searchDTO);

        // 记录搜索历史（如果有关键词）
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty()) {
            // TODO: 从请求中获取用户ID
            String userId = getCurrentUserId();
            if (userId != null) {
                appSearchService.recordSearchHistory(userId, searchDTO.getKeyword());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", searchResults.getRecords());
        result.put("total", searchResults.getTotal());
        result.put("page", searchResults.getCurrent());
        result.put("size", searchResults.getSize());
        result.put("pages", searchResults.getPages());
        result.put("hasFilters", searchDTO.hasFilters());

        return result;
    }

    @GetMapping("/advanced")
    @Operation(summary = "高级搜索", description = "支持更多搜索条件的高级搜索")
    public Map<String, Object> advancedSearch(@ModelAttribute AppSearchDTO searchDTO) {
        log.info("高级搜索: {}", searchDTO);

        // 验证分页参数
        searchDTO.validatePagination();

        // 执行高级搜索
        Page<AppSearchResultVO> searchResults = appSearchService.advancedSearch(searchDTO);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", searchResults.getRecords());
        result.put("total", searchResults.getTotal());
        result.put("page", searchResults.getCurrent());
        result.put("size", searchResults.getSize());
        result.put("pages", searchResults.getPages());

        return result;
    }

    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议", description = "根据输入的关键词获取搜索建议")
    public Map<String, Object> getSearchSuggestions(
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取搜索建议: keyword={}, limit={}", keyword, limit);

        if (keyword == null || keyword.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "关键词不能为空");
            return result;
        }

        // 限制返回数量
        if (limit > 20) {
            limit = 20;
        }

        List<String> suggestions = appSearchService.getSearchSuggestions(keyword, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("keyword", keyword);
        result.put("suggestions", suggestions);
        result.put("count", suggestions.size());

        return result;
    }

    @GetMapping("/hot-keywords")
    @Operation(summary = "获取热门搜索词", description = "获取当前热门搜索关键词")
    public Map<String, Object> getHotKeywords(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取热门搜索词: limit={}", limit);

        // 限制返回数量
        if (limit > 30) {
            limit = 30;
        }

        List<String> hotKeywords = appSearchService.getHotSearchKeywords(limit);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", hotKeywords);
        result.put("count", hotKeywords.size());

        return result;
    }

    @GetMapping("/history")
    @Operation(summary = "获取搜索历史", description = "获取当前用户的搜索历史")
    public Map<String, Object> getSearchHistory(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {

        String userId = getCurrentUserId();

        if (userId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        log.info("获取搜索历史: userId={}, limit={}", userId, limit);

        // 限制返回数量
        if (limit > 50) {
            limit = 50;
        }

        List<String> history = appSearchService.getUserSearchHistory(userId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", history);
        result.put("count", history.size());
        result.put("userId", userId);

        return result;
    }

    @DeleteMapping("/history")
    @Operation(summary = "清除搜索历史", description = "清除当前用户的所有搜索历史")
    public Map<String, Object> clearSearchHistory() {

        String userId = getCurrentUserId();

        if (userId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        log.info("清除搜索历史: userId={}", userId);

        boolean success = appSearchService.clearUserSearchHistory(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("userId", userId);
        if (success) {
            result.put("message", "搜索历史已清除");
        } else {
            result.put("message", "清除失败");
        }

        return result;
    }

    @PostMapping("/history")
    @Operation(summary = "记录搜索", description = "记录一次搜索行为")
    public Map<String, Object> recordSearch(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "关键词不能为空");
            return result;
        }

        String userId = getCurrentUserId();

        log.info("记录搜索: userId={}, keyword={}", userId, keyword);

        appSearchService.recordSearchHistory(userId, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("keyword", keyword);
        result.put("userId", userId);

        return result;
    }

    @PostMapping("/index/build")
    @Operation(summary = "构建搜索索引", description = "重新构建所有应用的搜索索引（管理员功能）")
    public Map<String, Object> buildSearchIndex() {
        log.info("开始构建搜索索引");

        long startTime = System.currentTimeMillis();
        int indexedCount = appSearchService.buildSearchIndex();
        long endTime = System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("indexedCount", indexedCount);
        result.put("timeElapsed", (endTime - startTime) + "ms");
        result.put("message", String.format("成功索引 %d 个应用", indexedCount));

        return result;
    }

    @PostMapping("/index/update/{appId}")
    @Operation(summary = "更新应用索引", description = "更新指定应用的搜索索引")
    public Map<String, Object> updateAppIndex(
            @Parameter(description = "应用ID") @PathVariable String appId) {

        log.info("更新应用索引: appId={}", appId);

        boolean success = appSearchService.updateAppSearchIndex(appId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("appId", appId);
        if (success) {
            result.put("message", "索引更新成功");
        } else {
            result.put("message", "索引更新失败");
        }

        return result;
    }

    @DeleteMapping("/index/{appId}")
    @Operation(summary = "删除应用索引", description = "删除指定应用的搜索索引")
    public Map<String, Object> deleteAppIndex(
            @Parameter(description = "应用ID") @PathVariable String appId) {

        log.info("删除应用索引: appId={}", appId);

        boolean success = appSearchService.deleteAppSearchIndex(appId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("appId", appId);
        if (success) {
            result.put("message", "索引删除成功");
        } else {
            result.put("message", "索引删除失败");
        }

        return result;
    }

    @GetMapping("/quick")
    @Operation(summary = "快速搜索", description = "仅返回应用名称和图标的快速搜索")
    public Map<String, Object> quickSearch(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量，默认5") @RequestParam(defaultValue = "5") Integer limit) {

        log.info("快速搜索: keyword={}, limit={}", keyword, limit);

        if (keyword == null || keyword.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "关键词不能为空");
            return result;
        }

        // 构建搜索参数
        AppSearchDTO searchDTO = new AppSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setPage(1);
        searchDTO.setSize(limit > 20 ? 20 : limit);
        searchDTO.setHighlight(false); // 快速搜索不需要高亮

        Page<AppSearchResultVO> searchResults = appSearchService.searchApps(searchDTO);

        // 简化返回结果
        List<Map<String, Object>> quickResults = searchResults.getRecords().stream()
                .map(app -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", app.getId());
                    item.put("appId", app.getAppId());
                    item.put("name", app.getNameOriginal());
                    item.put("icon", app.getIconUrl());
                    item.put("developer", app.getDeveloperNameOriginal());
                    item.put("category", app.getCategoryName());
                    item.put("rating", app.getRating());
                    item.put("isFree", app.getIsFree());
                    return item;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("keyword", keyword);
        result.put("data", quickResults);
        result.put("count", quickResults.size());

        return result;
    }

    /**
     * 获取当前用户ID
     * TODO: 实际项目中应从Spring Security或JWT中获取
     */
    private String getCurrentUserId() {
        // 模拟获取用户ID，实际应从认证上下文中获取
        // return SecurityContextHolder.getContext().getAuthentication().getName();
        return null; // 暂时返回null，表示匿名用户
    }
}