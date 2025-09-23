package com.mooncloud.shorturl.controller;

import com.mooncloud.shorturl.dto.ApiResponse;
import com.mooncloud.shorturl.dto.CacheWarmupRequest;
import com.mooncloud.shorturl.dto.CacheWarmupResponse;
import com.mooncloud.shorturl.dto.HotDataScore;
import com.mooncloud.shorturl.exception.NotFoundException;
import com.mooncloud.shorturl.service.CacheWarmupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 缓存预热控制器
 *
 * 提供缓存预热相关的API接口：
 * 1. 执行预热任务
 * 2. 智能热点预热
 * 3. 新兴热点预热
 * 4. 查询任务状态
 * 5. 取消任务
 * 6. 管理任务
 *
 * @author mooncloud
 */
@RestController
@RequestMapping("/api/v1/cache/warmup")
@Slf4j
@Validated
public class CacheWarmupController {

    @Autowired
    private CacheWarmupService cacheWarmupService;

    /**
     * 执行缓存预热
     *
     * @param request 预热请求
     * @return 预热响应
     */
    @PostMapping("/execute")
    public ApiResponse<CacheWarmupResponse> executeWarmup(@Valid @RequestBody CacheWarmupRequest request) {
        log.info("收到缓存预热请求: 策略={}, 限制={}, 异步={}",
                request.getStrategy(), request.getLimit(), request.getAsync());

        CacheWarmupResponse response = cacheWarmupService.executeWarmup(request);

        return ApiResponse.success("缓存预热任务已启动", response);
    }

    /**
     * 快速预热热门链接
     */
    @PostMapping("/hot")
    public ApiResponse<CacheWarmupResponse> warmupHotLinks(@RequestParam(defaultValue = "1000") Integer limit,
                                                          @RequestParam(defaultValue = "true") Boolean async) {
        CacheWarmupRequest request = new CacheWarmupRequest();
        request.setStrategy(CacheWarmupRequest.WarmupStrategy.HOT_LINKS);
        request.setLimit(limit);
        request.setAsync(async);

        CacheWarmupResponse response = cacheWarmupService.executeWarmup(request);

        return ApiResponse.success("热门链接预热任务已启动", response);
    }

    /**
     * 快速预热最近创建的链接
     */
    @PostMapping("/recent")
    public ApiResponse<CacheWarmupResponse> warmupRecentLinks(@RequestParam(defaultValue = "1000") Integer limit,
                                                             @RequestParam(defaultValue = "true") Boolean async) {
        CacheWarmupRequest request = new CacheWarmupRequest();
        request.setStrategy(CacheWarmupRequest.WarmupStrategy.RECENT_CREATED);
        request.setLimit(limit);
        request.setAsync(async);

        CacheWarmupResponse response = cacheWarmupService.executeWarmup(request);

        return ApiResponse.success("最近创建链接预热任务已启动", response);
    }

    /**
     * 用户维度预热
     */
    @PostMapping("/user/{userId}")
    public ApiResponse<CacheWarmupResponse> warmupUserLinks(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "500") Integer limit,
                                                           @RequestParam(defaultValue = "true") Boolean async) {
        CacheWarmupRequest request = new CacheWarmupRequest();
        request.setStrategy(CacheWarmupRequest.WarmupStrategy.USER_BASED);
        request.setUserId(userId);
        request.setLimit(limit);
        request.setAsync(async);

        CacheWarmupResponse response = cacheWarmupService.executeWarmup(request);

        return ApiResponse.success("用户链接预热任务已启动", response);
    }

    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/task/{taskId}")
    public ApiResponse<CacheWarmupResponse> getTaskStatus(@PathVariable String taskId) {
        CacheWarmupResponse response = cacheWarmupService.getTaskStatus(taskId);

        if (response == null) {
            throw new NotFoundException("任务不存在: " + taskId);
        }

        return ApiResponse.success(response);
    }

    /**
     * 获取所有任务状态
     *
     * @return 所有任务状态列表
     */
    @GetMapping("/tasks")
    public ApiResponse<List<CacheWarmupResponse>> getAllTaskStatus() {
        List<CacheWarmupResponse> tasks = cacheWarmupService.getAllTaskStatus();
        return ApiResponse.success(tasks);
    }

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    @PostMapping("/task/{taskId}/cancel")
    public ApiResponse<Boolean> cancelTask(@PathVariable String taskId) {
        boolean cancelled = cacheWarmupService.cancelTask(taskId);

        if (cancelled) {
            log.info("任务已取消: {}", taskId);
            return ApiResponse.success("任务已取消", true);
        } else {
            return ApiResponse.error("任务无法取消或不存在");
        }
    }

    /**
     * 清理已完成的任务
     */
    @PostMapping("/cleanup")
    public ApiResponse<String> cleanupCompletedTasks() {
        cacheWarmupService.cleanupCompletedTasks();
        log.info("已清理完成的任务");
        return ApiResponse.success("已清理完成的任务");
    }

    /**
     * 获取预热策略列表
     */
    @GetMapping("/strategies")
    public ApiResponse<CacheWarmupRequest.WarmupStrategy[]> getWarmupStrategies() {
        return ApiResponse.success(CacheWarmupRequest.WarmupStrategy.values());
    }

    /**
     * 智能热点预热 - 基于热度分析算法的智能预热
     *
     * @param minHotLevel 最小热点级别（可选：SUPER_HOT, HOT, WARM, NORMAL, COLD）
     * @param limit 预热数量限制
     * @return 预热响应
     */
    @PostMapping("/smart")
    public ApiResponse<CacheWarmupResponse> smartHotDataWarmup(
            @RequestParam(defaultValue = "WARM") String minHotLevel,
            @RequestParam(defaultValue = "1000") Integer limit) {

        log.info("智能热点预热请求: minHotLevel={}, limit={}", minHotLevel, limit);

        try {
            HotDataScore.HotLevel hotLevel = HotDataScore.HotLevel.valueOf(minHotLevel.toUpperCase());
            CacheWarmupResponse response = cacheWarmupService.smartHotDataWarmup(hotLevel, limit);
            return ApiResponse.success("智能热点预热任务已完成", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.badRequest("无效的热点级别: " + minHotLevel +
                    "，支持的级别: SUPER_HOT, HOT, WARM, NORMAL, COLD");
        }
    }

    /**
     * 新兴热点自动预热
     *
     * @param limit 预热数量限制
     * @return 预热响应
     */
    @PostMapping("/emerging")
    public ApiResponse<CacheWarmupResponse> emergingHotspotsWarmup(
            @RequestParam(defaultValue = "500") Integer limit) {

        log.info("新兴热点预热请求: limit={}", limit);

        CacheWarmupResponse response = cacheWarmupService.emergingHotspotsWarmup(limit);
        return ApiResponse.success("新兴热点预热任务已完成", response);
    }

    /**
     * 批量智能预热 - 一键执行多种智能预热策略
     *
     * @return 预热响应列表
     */
    @PostMapping("/smart/batch")
    public ApiResponse<List<CacheWarmupResponse>> batchSmartWarmup() {
        log.info("开始批量智能预热");

        List<CacheWarmupResponse> responses = List.of(
                // 1. 预热超级热点和热点数据
                cacheWarmupService.smartHotDataWarmup(HotDataScore.HotLevel.HOT, 500),
                // 2. 预热新兴热点
                cacheWarmupService.emergingHotspotsWarmup(200)
        );

        return ApiResponse.success("批量智能预热已完成", responses);
    }

    /**
     * 获取热点级别信息
     */
    @GetMapping("/hotlevels")
    public ApiResponse<List<java.util.Map<String, Object>>> getHotLevels() {
        List<java.util.Map<String, Object>> levels = List.of(
                java.util.Map.of("level", "SUPER_HOT", "description", "超级热点", "minScore", 90, "maxScore", 100),
                java.util.Map.of("level", "HOT", "description", "热点", "minScore", 70, "maxScore", 89),
                java.util.Map.of("level", "WARM", "description", "温热", "minScore", 50, "maxScore", 69),
                java.util.Map.of("level", "NORMAL", "description", "普通", "minScore", 30, "maxScore", 49),
                java.util.Map.of("level", "COLD", "description", "冷门", "minScore", 0, "maxScore", 29)
        );

        return ApiResponse.success("热点级别信息", levels);
    }
}