package com.moon.cloud.appstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.SearchDTO;
import com.moon.cloud.appstore.entity.SearchHistory;
import com.moon.cloud.appstore.service.SearchService;
import com.moon.cloud.appstore.vo.FreeAppVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appstore/search")
@Tag(name = "搜索管理", description = "应用搜索相关接口")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "搜索应用")
    @GetMapping
    public Page<FreeAppVO> searchApps(@Valid SearchDTO dto, HttpServletRequest request) {
        log.info("搜索应用, 关键词: {}", dto.getKeyword());

        // 保存搜索历史
        SearchHistory history = new SearchHistory();
        history.setSearchQuery(dto.getKeyword());
        history.setSearchType("NORMAL");
        history.setIpAddress(request.getRemoteAddr());
        history.setUserAgent(request.getHeader("User-Agent"));
        history.setPlatform(request.getHeader("Platform"));

        // 从Header获取用户ID或设备ID
        String userId = request.getHeader("User-Id");
        String deviceId = request.getHeader("Device-Id");
        history.setUserId(userId);
        history.setDeviceId(deviceId);

        searchService.saveSearchHistory(history);

        return searchService.searchApps(dto);
    }

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggestions")
    public List<String> getSearchSuggestions(
            @Parameter(description = "关键词", required = true)
            @RequestParam String keyword) {
        log.info("获取搜索建议, 关键词: {}", keyword);
        return searchService.getSearchSuggestions(keyword);
    }

    @Operation(summary = "获取热门搜索词")
    @GetMapping("/hot")
    public List<String> getHotSearchKeywords(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门搜索词, 数量: {}", limit);
        return searchService.getHotSearchKeywords(limit);
    }

    @Operation(summary = "获取用户搜索历史")
    @GetMapping("/history")
    public List<String> getUserSearchHistory(
            @Parameter(description = "用户ID")
            @RequestHeader(value = "User-Id", required = false) String userId,
            @Parameter(description = "设备ID")
            @RequestHeader(value = "Device-Id", required = false) String deviceId,
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取用户搜索历史, userId: {}, deviceId: {}", userId, deviceId);
        return searchService.getUserSearchHistory(userId, deviceId, limit);
    }

    @Operation(summary = "清除用户搜索历史")
    @DeleteMapping("/history")
    public void clearSearchHistory(
            @Parameter(description = "用户ID")
            @RequestHeader(value = "User-Id", required = false) String userId,
            @Parameter(description = "设备ID")
            @RequestHeader(value = "Device-Id", required = false) String deviceId) {
        log.info("清除用户搜索历史, userId: {}, deviceId: {}", userId, deviceId);
        searchService.clearSearchHistory(userId, deviceId);
    }
}