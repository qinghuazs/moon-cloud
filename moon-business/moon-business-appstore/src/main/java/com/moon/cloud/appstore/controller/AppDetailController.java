package com.moon.cloud.appstore.controller;

import com.moon.cloud.appstore.service.AppDetailService;
import com.moon.cloud.appstore.vo.AppDetailVO;
import com.moon.cloud.appstore.vo.AppPriceChartVO;
import com.moon.cloud.appstore.vo.AppSimilarVO;
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
 * 应用详情控制器
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@RestController
@RequestMapping("/api/appstore/app")
@RequiredArgsConstructor
@Tag(name = "应用详情管理", description = "应用详情相关接口")
public class AppDetailController {

    private final AppDetailService appDetailService;

    @GetMapping("/{appId}")
    @Operation(summary = "获取应用详情", description = "根据应用ID获取完整的应用详细信息")
    public Map<String, Object> getAppDetail(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId) {

        log.info("获取应用详情: {}", appId);

        AppDetailVO appDetail = appDetailService.getAppDetail(appId);

        Map<String, Object> result = new HashMap<>();
        if (appDetail != null) {
            result.put("success", true);
            result.put("data", appDetail);

            // 记录查看次数
            appDetailService.increaseViewCount(appId);
        } else {
            result.put("success", false);
            result.put("message", "应用不存在");
            result.put("appId", appId);
        }

        return result;
    }

    @GetMapping("/{appId}/price-chart")
    @Operation(summary = "获取价格历史图表数据", description = "获取应用的价格变化历史数据，用于绘制价格走势图")
    public Map<String, Object> getAppPriceChart(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId,
            @Parameter(description = "天数，默认90天") @RequestParam(defaultValue = "90") Integer days) {

        log.info("获取价格图表数据: appId={}, days={}", appId, days);

        // 限制最大查询天数
        if (days > 365) {
            days = 365;
        }

        AppPriceChartVO chartData = appDetailService.getAppPriceChart(appId, days);

        Map<String, Object> result = new HashMap<>();
        if (chartData != null) {
            result.put("success", true);
            result.put("data", chartData);
        } else {
            result.put("success", false);
            result.put("message", "无价格历史数据");
            result.put("appId", appId);
        }

        return result;
    }

    @GetMapping("/{appId}/similar")
    @Operation(summary = "获取相似应用", description = "获取与当前应用相似的其他应用推荐")
    public Map<String, Object> getSimilarApps(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取相似应用: appId={}, limit={}", appId, limit);

        // 限制最大返回数量
        if (limit > 50) {
            limit = 50;
        }

        List<AppSimilarVO> similarApps = appDetailService.getSimilarApps(appId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appId", appId);
        result.put("count", similarApps.size());
        result.put("data", similarApps);

        return result;
    }

    @GetMapping("/{appId}/developer-apps")
    @Operation(summary = "获取同开发商应用", description = "获取同一开发商的其他应用")
    public Map<String, Object> getDeveloperApps(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取同开发商应用: appId={}, limit={}", appId, limit);

        // 限制最大返回数量
        if (limit > 50) {
            limit = 50;
        }

        List<AppSimilarVO> developerApps = appDetailService.getDeveloperApps(appId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appId", appId);
        result.put("count", developerApps.size());
        result.put("data", developerApps);

        return result;
    }

    @GetMapping("/{appId}/category-top")
    @Operation(summary = "获取同分类热门应用", description = "获取同一分类下的热门应用")
    public Map<String, Object> getCategoryTopApps(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取同分类热门应用: appId={}, limit={}", appId, limit);

        // 限制最大返回数量
        if (limit > 50) {
            limit = 50;
        }

        List<AppSimilarVO> topApps = appDetailService.getCategoryTopApps(appId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appId", appId);
        result.put("count", topApps.size());
        result.put("data", topApps);

        return result;
    }

    @GetMapping("/{appId}/related")
    @Operation(summary = "获取所有相关应用", description = "一次性获取相似应用、同开发商应用和同分类热门应用")
    public Map<String, Object> getAllRelatedApps(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId,
            @Parameter(description = "每类返回数量，默认5") @RequestParam(defaultValue = "5") Integer limit) {

        log.info("获取所有相关应用: appId={}, limit={}", appId, limit);

        // 限制最大返回数量
        if (limit > 20) {
            limit = 20;
        }

        Map<String, Object> relatedApps = new HashMap<>();
        relatedApps.put("similar", appDetailService.getSimilarApps(appId, limit));
        relatedApps.put("developer", appDetailService.getDeveloperApps(appId, limit));
        relatedApps.put("categoryTop", appDetailService.getCategoryTopApps(appId, limit));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appId", appId);
        result.put("data", relatedApps);

        return result;
    }

    @PostMapping("/{appId}/download")
    @Operation(summary = "记录下载", description = "记录用户点击下载按钮的行为")
    public Map<String, Object> recordDownload(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId) {

        log.info("记录应用下载: {}", appId);

        boolean success = appDetailService.recordDownload(appId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("appId", appId);

        // 返回App Store链接
        if (success) {
            String appStoreUrl = appDetailService.getAppStoreUrl(appId);
            result.put("appStoreUrl", appStoreUrl);
            result.put("message", "下载记录成功");
        } else {
            result.put("message", "记录失败");
        }

        return result;
    }

    @GetMapping("/{appId}/store-url")
    @Operation(summary = "获取App Store链接", description = "获取应用的App Store下载链接")
    public Map<String, Object> getAppStoreUrl(
            @Parameter(description = "应用ID或App Store ID") @PathVariable String appId) {

        log.info("获取App Store链接: {}", appId);

        String url = appDetailService.getAppStoreUrl(appId);

        Map<String, Object> result = new HashMap<>();
        if (url != null) {
            result.put("success", true);
            result.put("url", url);
            result.put("appId", appId);
        } else {
            result.put("success", false);
            result.put("message", "应用不存在");
            result.put("appId", appId);
        }

        return result;
    }
}