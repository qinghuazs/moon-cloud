package com.moon.cloud.appstore.controller;

import com.moon.cloud.appstore.service.AppQueueConsumerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * App队列管理控制器
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Slf4j
@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
@Tag(name = "App队列管理", description = "App URL队列消费管理接口")
public class AppQueueController {

    private final AppQueueConsumerService appQueueConsumerService;

    @GetMapping("/status")
    @Operation(summary = "获取队列状态", description = "获取所有分类队列、default队列和失败队列的大小")
    public Map<String, Object> getQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalQueueSize", appQueueConsumerService.getAllQueuesSize());
        status.put("defaultQueueSize", appQueueConsumerService.getCategoryQueueSize("default"));
        status.put("failedQueueSize", appQueueConsumerService.getFailedQueueSize());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @GetMapping("/status/{categoryId}")
    @Operation(summary = "获取指定分类队列状态", description = "获取指定分类队列的大小")
    public Map<String, Object> getCategoryQueueStatus(@PathVariable String categoryId) {
        Map<String, Object> status = new HashMap<>();
        status.put("categoryId", categoryId);
        status.put("queueSize", appQueueConsumerService.getCategoryQueueSize(categoryId));
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @PostMapping("/consume")
    @Operation(summary = "手动触发消费", description = "手动触发一次所有分类队列和default队列消费任务")
    public Map<String, Object> triggerConsume() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long totalQueueSize = appQueueConsumerService.getAllQueuesSize();
            if (totalQueueSize > 0) {
                appQueueConsumerService.consumeAllCategoryQueues();
                result.put("success", true);
                result.put("message", "消费任务已触发（包含default队列）");
                result.put("totalQueueSize", totalQueueSize);
            } else {
                result.put("success", false);
                result.put("message", "所有队列为空，无需消费");
                result.put("totalQueueSize", 0);
            }
        } catch (Exception e) {
            log.error("手动触发消费失败", e);
            result.put("success", false);
            result.put("message", "触发失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/consume/{categoryId}")
    @Operation(summary = "手动触发指定分类消费", description = "手动触发指定分类队列的消费任务")
    public Map<String, Object> triggerCategoryConsume(@PathVariable String categoryId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long queueSize = appQueueConsumerService.getCategoryQueueSize(categoryId);
            if (queueSize > 0) {
                appQueueConsumerService.consumeCategoryQueue(categoryId);
                result.put("success", true);
                result.put("message", "分类 " + categoryId + " 的消费任务已触发");
                result.put("queueSize", queueSize);
            } else {
                result.put("success", false);
                result.put("message", "分类 " + categoryId + " 的队列为空");
                result.put("queueSize", 0);
            }
        } catch (Exception e) {
            log.error("手动触发分类消费失败: {}", categoryId, e);
            result.put("success", false);
            result.put("message", "触发失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/consume/default")
    @Operation(summary = "手动触发default队列消费", description = "手动触发default队列的消费任务")
    public Map<String, Object> triggerDefaultConsume() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long queueSize = appQueueConsumerService.getCategoryQueueSize("default");
            if (queueSize > 0) {
                appQueueConsumerService.consumeCategoryQueue("default");
                result.put("success", true);
                result.put("message", "default队列的消费任务已触发");
                result.put("queueSize", queueSize);
            } else {
                result.put("success", false);
                result.put("message", "default队列为空");
                result.put("queueSize", 0);
            }
        } catch (Exception e) {
            log.error("手动触发default队列消费失败", e);
            result.put("success", false);
            result.put("message", "触发失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/retry")
    @Operation(summary = "重试失败任务", description = "将失败队列中的任务重新加入处理队列")
    public Map<String, Object> retryFailedTasks() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long failedQueueSize = appQueueConsumerService.getFailedQueueSize();
            if (failedQueueSize > 0) {
                appQueueConsumerService.retryFailedTasks();
                result.put("success", true);
                result.put("message", "重试任务已触发");
                result.put("failedQueueSize", failedQueueSize);
            } else {
                result.put("success", false);
                result.put("message", "失败队列为空，无需重试");
                result.put("failedQueueSize", 0);
            }
        } catch (Exception e) {
            log.error("触发重试失败", e);
            result.put("success", false);
            result.put("message", "重试失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/process")
    @Operation(summary = "处理单个URL", description = "手动处理单个App URL")
    public Map<String, Object> processUrl(@RequestParam String url) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = appQueueConsumerService.processAppUrl(url);
            result.put("success", success);
            result.put("message", success ? "处理成功" : "处理失败");
            result.put("url", url);
        } catch (Exception e) {
            log.error("处理URL失败: {}", url, e);
            result.put("success", false);
            result.put("message", "处理异常: " + e.getMessage());
            result.put("url", url);
        }
        return result;
    }
}