package com.moon.cloud.appstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * 简化版爬虫控制器
 * 直接实现爬虫功能，避免复杂的服务依赖
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@RestController
@RequestMapping("/simple-crawler")
@Tag(name = "简化爬虫接口", description = "App Store 爬虫接口简化版")
public class SimpleCrawlerController {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private static final String APP_QUEUE_PREFIX = "appstore:queue:";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36";

    @PostMapping("/crawl/{categoryId}")
    @Operation(summary = "爬取指定分类", description = "根据分类ID爬取应用链接")
    public ResponseEntity<Map<String, Object>> crawlCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable("categoryId") String categoryId) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 示例URL，实际应该从数据库读取
            String url = "https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid";

            // 抓取页面
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(30000)
                    .get();

            // 提取链接
            List<String> appLinks = new ArrayList<>();
            Elements elements = doc.select("a.we-lockup.targeted-link[href]");

            if (elements.isEmpty()) {
                // 尝试其他选择器
                elements = doc.select("a[class*='we-lockup'][class*='targeted-link']");
            }

            for (Element element : elements) {
                String href = element.attr("href");
                if (!href.isEmpty()) {
                    if (href.startsWith("/")) {
                        href = "https://apps.apple.com" + href;
                    }
                    appLinks.add(href);
                }
            }

            // 如果配置了Redis，存储到Redis
            if (stringRedisTemplate != null) {
                String queueName = APP_QUEUE_PREFIX + categoryId;
                for (String link : appLinks) {
                    stringRedisTemplate.opsForList().rightPush(queueName, link);
                }
            }

            result.put("success", true);
            result.put("categoryId", categoryId);
            result.put("crawledCount", appLinks.size());
            result.put("links", appLinks);
            result.put("message", "成功爬取 " + appLinks.size() + " 个应用链接");

        } catch (IOException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "爬取失败");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/test")
    @Operation(summary = "测试接口", description = "测试爬虫服务是否正常")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("message", "爬虫服务运行正常");
        result.put("redisAvailable", stringRedisTemplate != null);
        return ResponseEntity.ok(result);
    }
}