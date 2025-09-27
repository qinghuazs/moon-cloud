package com.moon.cloud.appstore.controller;

import com.moon.cloud.appstore.entity.AppPriceHistory;
import com.moon.cloud.appstore.service.AppPriceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * APP价格历史控制器
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@RestController
@RequestMapping("/price-history")
@RequiredArgsConstructor
@Tag(name = "APP价格历史", description = "APP价格历史查询接口")
public class AppPriceHistoryController {

    private final AppPriceHistoryService appPriceHistoryService;

    @GetMapping("/app/{appId}")
    @Operation(summary = "获取APP价格历史", description = "获取指定APP的价格变化历史记录")
    public List<AppPriceHistory> getAppPriceHistory(
            @PathVariable String appId,
            @Parameter(description = "开始时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusMonths(3);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return appPriceHistoryService.getAppPriceHistory(appId, startTime, endTime);
    }

    @GetMapping("/app/{appId}/latest")
    @Operation(summary = "获取APP最新价格", description = "获取APP的最新价格记录")
    public AppPriceHistory getLatestPrice(@PathVariable String appId) {
        return appPriceHistoryService.getLatestPrice(appId);
    }

    @GetMapping("/app/{appId}/statistics")
    @Operation(summary = "获取APP价格统计", description = "获取APP的价格统计信息")
    public Map<String, Object> getAppPriceStatistics(@PathVariable String appId) {
        return appPriceHistoryService.getAppPriceStatistics(appId);
    }

    @GetMapping("/app/{appId}/lowest")
    @Operation(summary = "获取历史最低价", description = "获取APP的历史最低价格")
    public Map<String, Object> getLowestPrice(@PathVariable String appId) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal lowestPrice = appPriceHistoryService.getHistoricalLowestPrice(appId);
        result.put("appId", appId);
        result.put("lowestPrice", lowestPrice);
        return result;
    }

    @GetMapping("/app/{appId}/highest")
    @Operation(summary = "获取历史最高价", description = "获取APP的历史最高价格")
    public Map<String, Object> getHighestPrice(@PathVariable String appId) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal highestPrice = appPriceHistoryService.getHistoricalHighestPrice(appId);
        result.put("appId", appId);
        result.put("highestPrice", highestPrice);
        return result;
    }

    @GetMapping("/free")
    @Operation(summary = "获取限免应用", description = "获取最近的限免应用列表")
    public List<AppPriceHistory> getFreeApps(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {
        return appPriceHistoryService.getFreeApps(limit);
    }

    @GetMapping("/price-drops")
    @Operation(summary = "获取降价应用", description = "获取最近降价的应用列表")
    public List<AppPriceHistory> getPriceDrops(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {
        return appPriceHistoryService.getRecentPriceDrops(days, limit);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取分类价格变化", description = "获取指定分类的价格变化")
    public List<AppPriceHistory> getCategoryPriceChanges(
            @PathVariable String categoryId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {
        return appPriceHistoryService.getCategoryPriceChanges(categoryId, days, limit);
    }

    @GetMapping("/developer/{developerName}")
    @Operation(summary = "获取开发者价格变化", description = "获取指定开发者的应用价格变化")
    public List<AppPriceHistory> getDeveloperPriceChanges(
            @PathVariable String developerName,
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit) {
        return appPriceHistoryService.getDeveloperPriceChanges(developerName, days, limit);
    }

    @GetMapping("/notifications/pending")
    @Operation(summary = "获取待通知列表", description = "获取待通知的价格变化")
    public List<AppPriceHistory> getPendingNotifications(
            @Parameter(description = "变化类型") @RequestParam(defaultValue = "FREE,DECREASE") String changeTypes,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "50") int limit) {
        List<String> typeList = Arrays.asList(changeTypes.split(","));
        return appPriceHistoryService.getPendingNotifications(typeList, limit);
    }

    @PostMapping("/notification/{id}/mark")
    @Operation(summary = "标记为已通知", description = "将价格变化记录标记为已通知")
    public Map<String, Object> markAsNotified(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = appPriceHistoryService.markAsNotified(id);
        result.put("success", success);
        result.put("id", id);
        result.put("message", success ? "标记成功" : "标记失败");
        return result;
    }

    @GetMapping("/summary")
    @Operation(summary = "获取价格变化汇总", description = "获取最近的价格变化汇总信息")
    public Map<String, Object> getPriceSummary(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") int days) {

        Map<String, Object> summary = new HashMap<>();

        // 获取限免应用数
        List<AppPriceHistory> freeApps = appPriceHistoryService.getFreeApps(100);
        summary.put("freeAppCount", freeApps.size());

        // 获取降价应用数
        List<AppPriceHistory> priceDrops = appPriceHistoryService.getRecentPriceDrops(days, 100);
        summary.put("priceDropCount", priceDrops.size());

        // 统计各种变化类型
        Map<String, Integer> changeTypeCount = new HashMap<>();
        for (AppPriceHistory history : priceDrops) {
            String changeType = history.getChangeType();
            changeTypeCount.put(changeType, changeTypeCount.getOrDefault(changeType, 0) + 1);
        }
        summary.put("changeTypeCount", changeTypeCount);

        // 添加时间范围
        summary.put("days", days);
        summary.put("timestamp", LocalDateTime.now());

        return summary;
    }
}