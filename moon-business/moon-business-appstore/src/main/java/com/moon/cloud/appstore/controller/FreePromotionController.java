package com.moon.cloud.appstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppQueryDTO;
import com.moon.cloud.appstore.service.FreePromotionService;
import com.moon.cloud.appstore.vo.FreeAppStatisticsVO;
import com.moon.cloud.appstore.vo.FreePromotionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 限免推广控制器
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@RestController
@RequestMapping("/api/appstore/free")
@RequiredArgsConstructor
@Tag(name = "限免应用管理", description = "限免应用相关接口")
public class FreePromotionController {

    private final FreePromotionService freePromotionService;

    @GetMapping("/today")
    @Operation(summary = "获取今日限免列表", description = "获取今日所有限免应用，支持分页、筛选和排序")
    public Map<String, Object> getTodayFreeApps(@Validated FreeAppQueryDTO queryDTO) {
        log.info("获取今日限免列表，参数: {}", queryDTO);

        // 验证分页参数
        queryDTO.validatePagination();

        // 获取限免列表
        Page<FreePromotionVO> page = freePromotionService.getTodayFreeApps(queryDTO);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", page.getTotal());
        result.put("page", page.getCurrent());
        result.put("size", page.getSize());
        result.put("pages", page.getPages());
        result.put("data", page.getRecords());

        // 添加统计信息
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", page.getTotal());
        summary.put("currentPage", page.getCurrent());
        summary.put("hasNext", page.hasNext());
        summary.put("hasPrevious", page.hasPrevious());
        result.put("summary", summary);

        return result;
    }

    @GetMapping("/ending-soon")
    @Operation(summary = "获取即将结束的限免", description = "获取指定小时内即将结束的限免应用")
    public Map<String, Object> getEndingSoonApps(
            @Parameter(description = "小时数，默认6小时") @RequestParam(defaultValue = "6") int hours,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") int limit) {

        log.info("获取{}小时内即将结束的限免应用", hours);

        // 限制最大时间范围
        if (hours > 24) {
            hours = 24;
        }
        if (limit > 100) {
            limit = 100;
        }

        List<FreePromotionVO> apps = freePromotionService.getEndingSoonApps(hours);

        // 限制返回数量
        if (apps.size() > limit) {
            apps = apps.subList(0, limit);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("hours", hours);
        result.put("count", apps.size());
        result.put("data", apps);
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门限免应用", description = "基于下载量、评分等因素获取热门限免应用")
    public Map<String, Object> getHotFreeApps(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "推广类型: FREE=限免, DISCOUNT=打折") @RequestParam(required = false) String type) {

        log.info("获取热门限免应用，限制: {}", limit);

        // 限制最大返回数量
        if (limit > 50) {
            limit = 50;
        }

        List<FreePromotionVO> apps = freePromotionService.getHotFreeApps(limit);

        // 根据类型过滤
        if (type != null && !type.isEmpty()) {
            apps = apps.stream()
                    .filter(app -> type.equals(app.getPromotionType()))
                    .toList();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", apps.size());
        result.put("limit", limit);
        result.put("data", apps);

        return result;
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取限免统计数据", description = "获取限免应用的各种统计信息")
    public Map<String, Object> getFreeAppStatistics(
            @Parameter(description = "统计周期: today, week, month") @RequestParam(defaultValue = "today") String period) {

        log.info("获取限免统计数据，周期: {}", period);

        FreeAppStatisticsVO statistics = freePromotionService.getFreeAppStatistics();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("period", period);
        result.put("data", statistics);

        return result;
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类获取限免应用", description = "获取指定分类的限免应用")
    public Map<String, Object> getFreeAppsByCategory(
            @PathVariable String categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        log.info("获取分类 {} 的限免应用，页码: {}, 每页: {}", categoryId, page, size);

        // 验证分页参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        Page<FreePromotionVO> pageResult = freePromotionService.getFreeAppsByCategory(categoryId, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("categoryId", categoryId);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("data", pageResult.getRecords());

        return result;
    }

    @GetMapping("/app/{appId}/history")
    @Operation(summary = "获取应用限免历史", description = "获取指定应用的历史限免记录")
    public Map<String, Object> getPromotionHistory(@PathVariable String appId) {
        log.info("获取应用 {} 的限免历史", appId);

        List<FreePromotionVO> history = freePromotionService.getPromotionHistory(appId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("appId", appId);
        result.put("count", history.size());
        result.put("data", history);

        return result;
    }

    @PostMapping("/{promotionId}/view")
    @Operation(summary = "标记为已查看", description = "增加限免应用的查看次数")
    public Map<String, Object> markAsViewed(@PathVariable String promotionId) {
        log.info("标记限免 {} 为已查看", promotionId);

        boolean success = freePromotionService.markAsViewed(promotionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("promotionId", promotionId);
        result.put("message", success ? "标记成功" : "标记失败");

        return result;
    }

    @PostMapping("/{promotionId}/click")
    @Operation(summary = "记录点击", description = "增加限免应用的点击次数")
    public Map<String, Object> recordClick(@PathVariable String promotionId) {
        log.info("记录限免 {} 的点击", promotionId);

        boolean success = freePromotionService.increaseClickCount(promotionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("promotionId", promotionId);
        result.put("message", success ? "记录成功" : "记录失败");

        return result;
    }

    @PostMapping("/update-status")
    @Operation(summary = "更新限免状态", description = "手动触发限免状态更新（管理员功能）")
    public Map<String, Object> updatePromotionStatus() {
        log.info("手动触发限免状态更新");

        freePromotionService.updatePromotionStatus();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "状态更新任务已触发");
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    @PostMapping("/detect-new")
    @Operation(summary = "检测新限免", description = "手动触发新限免检测（管理员功能）")
    public Map<String, Object> detectNewPromotions() {
        log.info("手动触发新限免检测");

        int count = freePromotionService.detectNewPromotions();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("newCount", count);
        result.put("message", String.format("发现 %d 个新限免应用", count));
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }
}