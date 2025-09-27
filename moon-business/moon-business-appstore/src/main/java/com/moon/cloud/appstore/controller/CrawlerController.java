package com.moon.cloud.appstore.controller;

import com.moon.cloud.appstore.service.CrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * App Store 爬虫控制器
 * 提供爬虫相关的REST API接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@RestController
@RequestMapping("/crawler")
@RequiredArgsConstructor
@Tag(name = "爬虫接口", description = "App Store 爬虫相关接口")
public class CrawlerController {

    private final CrawlerService crawlerService;

    @PostMapping("/crawl/category/{categoryId}")
    @Operation(summary = "爬取指定分类的应用", description = "根据分类ID爬取该分类下的所有应用链接并存入Redis队列")
    public ResponseEntity<Map<String, Object>> crawlByCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable("categoryId") String categoryId) {

        log.info("接收到爬取请求，分类ID: {}", categoryId);

        try {
            int count = crawlerService.crawlAppsByCategoryId(categoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categoryId", categoryId);
            response.put("crawledCount", count);
            response.put("message", String.format("成功爬取 %d 个应用链接", count));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("爬取分类 {} 失败", categoryId, e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("categoryId", categoryId);
            response.put("error", e.getMessage());
            response.put("message", "爬取失败");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/crawl/all")
    @Operation(summary = "爬取所有分类的应用", description = "爬取所有激活分类下的应用链接并存入Redis队列")
    public ResponseEntity<Map<String, Object>> crawlAllCategories() {

        log.info("接收到爬取所有分类的请求");

        try {
            int totalCount = crawlerService.crawlAllCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalCrawled", totalCount);
            response.put("message", String.format("成功爬取所有分类，共 %d 个应用链接", totalCount));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("爬取所有分类失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "爬取失败");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/queue/{queueName}/size")
    @Operation(summary = "获取队列大小", description = "获取Redis队列中的链接数量")
    public ResponseEntity<Map<String, Object>> getQueueSize(
            @Parameter(description = "队列名称", required = true)
            @PathVariable String queueName) {

        long size = crawlerService.getQueueSize(queueName);

        Map<String, Object> response = new HashMap<>();
        response.put("queueName", queueName);
        response.put("size", size);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/queue/{queueName}")
    @Operation(summary = "清空队列", description = "清空指定的Redis队列")
    public ResponseEntity<Map<String, Object>> clearQueue(
            @Parameter(description = "队列名称", required = true)
            @PathVariable String queueName) {

        boolean success = crawlerService.clearQueue(queueName);

        Map<String, Object> response = new HashMap<>();
        response.put("queueName", queueName);
        response.put("success", success);
        response.put("message", success ? "队列清空成功" : "队列清空失败");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test/extract")
    @Operation(summary = "测试链接提取", description = "测试从HTML内容中提取应用链接的功能")
    public ResponseEntity<Map<String, Object>> testExtractLinks(
            @Parameter(description = "HTML内容", required = true)
            @RequestBody String htmlContent) {

        try {
            List<String> links = crawlerService.extractAppLinks(htmlContent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("extractedCount", links.size());
            response.put("links", links);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("提取链接失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/test/fetch")
    @Operation(summary = "测试页面抓取", description = "测试从指定URL抓取页面内容的功能")
    public ResponseEntity<Map<String, Object>> testFetchPage(
            @Parameter(description = "页面URL", required = true)
            @RequestParam String url) {

        try {
            String content = crawlerService.fetchPageContent(url);

            Map<String, Object> response = new HashMap<>();
            response.put("success", content != null);
            response.put("url", url);
            response.put("contentLength", content != null ? content.length() : 0);

            if (content != null && content.length() > 1000) {
                response.put("contentPreview", content.substring(0, 1000) + "...");
            } else {
                response.put("contentPreview", content);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("抓取页面失败", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("url", url);
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}