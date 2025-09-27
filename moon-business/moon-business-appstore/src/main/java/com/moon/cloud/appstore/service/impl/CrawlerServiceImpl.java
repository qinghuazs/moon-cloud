package com.moon.cloud.appstore.service.impl;

import com.moon.cloud.appstore.entity.Category;
import com.moon.cloud.appstore.mapper.CategoryMapper;
import com.moon.cloud.appstore.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * App Store 爬虫服务实现类
 * 负责爬取App Store分类页面，提取应用链接并存储到Redis队列
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerServiceImpl implements CrawlerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final CategoryMapper categoryMapper;

    private static final String APP_QUEUE_PREFIX = "appstore:queue:";
    private static final String DEFAULT_QUEUE_NAME = "app_links";
    private static final int FETCH_TIMEOUT = 30000; // 30秒超时
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public int crawlAppsByCategoryId(String categoryId) {
        log.info("开始爬取分类ID: {} 的应用信息", categoryId);

        // 1. 从数据库获取分类信息（根据category_id字段查询）
        Category category = categoryMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryId, categoryId)
        );
        if (category == null) {
            log.error("分类ID: {} 不存在", categoryId);
            return 0;
        }

        String categoriesUrl = category.getCategoriesUrl();
        if (!StringUtils.hasText(categoriesUrl)) {
            log.error("分类ID: {} 的URL为空", categoryId);
            return 0;
        }

        log.info("分类: {} - {}, URL: {}", category.getNameCn(), category.getNameEn(), categoriesUrl);

        // 2. 爬取页面内容
        String htmlContent = fetchPageContent(categoriesUrl);
        if (!StringUtils.hasText(htmlContent)) {
            log.error("无法获取分类 {} 的页面内容", categoryId);
            return 0;
        }

        // 3. 提取应用链接
        List<String> appLinks = extractAppLinks(htmlContent);
        log.info("从分类 {} 提取到 {} 个应用链接", categoryId, appLinks.size());

        // 4. 存储到Redis队列
        String queueName = APP_QUEUE_PREFIX + categoryId;
        int pushedCount = pushLinksToRedisQueue(appLinks, queueName);
        log.info("成功将 {} 个链接推送到Redis队列: {}", pushedCount, queueName);

        return pushedCount;
    }

    @Override
    public int crawlAllCategories() {
        log.info("开始爬取所有激活分类的应用信息");

        // 从数据库获取所有激活的分类
        List<Category> categories = categoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                .eq(Category::getIsActive, true)
                .isNotNull(Category::getCategoriesUrl)
        );

        if (categories == null || categories.isEmpty()) {
            log.warn("没有找到激活的分类");
            return 0;
        }

        log.info("找到 {} 个激活的分类", categories.size());
        int totalLinks = 0;

        for (Category category : categories) {
            try {
                int count = crawlAppsByCategoryId(category.getCategoryId());
                totalLinks += count;

                // 添加延时避免请求过快
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("爬取被中断", e);
                break;
            } catch (Exception e) {
                log.error("爬取分类 {} 时出错", category.getCategoryId(), e);
            }
        }

        log.info("所有分类爬取完成，共获取 {} 个应用链接", totalLinks);
        return totalLinks;
    }

    @Override
    public String fetchPageContent(String url) {
        try {
            log.debug("正在获取页面内容: {}", url);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(FETCH_TIMEOUT)
                    .ignoreContentType(true)
                    .get();

            return doc.html();
        } catch (IOException e) {
            log.error("获取页面内容失败: {}", url, e);
            return null;
        }
    }

    @Override
    public List<String> extractAppLinks(String htmlContent) {
        List<String> appLinks = new ArrayList<>();

        if (!StringUtils.hasText(htmlContent)) {
            return appLinks;
        }

        try {
            Document doc = Jsoup.parse(htmlContent);

            // 查找class包含"we-lockup targeted-link"的元素
            // 注意：class可能有多个值，使用CSS选择器
            Elements elements = doc.select(".we-lockup.targeted-link[href]");

            for (Element element : elements) {
                String href = element.attr("href");
                if (StringUtils.hasText(href)) {
                    // 处理相对链接，转换为绝对链接
                    if (href.startsWith("/")) {
                        href = "https://apps.apple.com" + href;
                    }
                    appLinks.add(href);
                    log.debug("提取到应用链接: {}", href);
                }
            }

            // 如果上面的选择器没找到，尝试其他可能的选择器
            if (appLinks.isEmpty()) {
                // 尝试查找所有包含这两个class的a标签
                Elements altElements = doc.select("a[class*='we-lockup'][class*='targeted-link']");
                for (Element element : altElements) {
                    String href = element.attr("href");
                    if (StringUtils.hasText(href)) {
                        if (href.startsWith("/")) {
                            href = "https://apps.apple.com" + href;
                        }
                        appLinks.add(href);
                        log.debug("提取到应用链接(备用选择器): {}", href);
                    }
                }
            }

        } catch (Exception e) {
            log.error("解析HTML内容时出错", e);
        }

        return appLinks;
    }

    @Override
    public int pushLinksToRedisQueue(List<String> appLinks, String queueName) {
        if (appLinks == null || appLinks.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        String actualQueueName = StringUtils.hasText(queueName) ? queueName : APP_QUEUE_PREFIX + DEFAULT_QUEUE_NAME;

        try {
            for (String link : appLinks) {
                // 使用Redis的List数据结构，RPUSH添加到队列尾部
                Long result = redisTemplate.opsForList().rightPush(actualQueueName, link);
                if (result != null && result > 0) {
                    successCount++;
                }
            }

            // 设置队列过期时间为7天
            redisTemplate.expire(actualQueueName, 7, TimeUnit.DAYS);

            log.info("成功推送 {} 个链接到Redis队列: {}", successCount, actualQueueName);
        } catch (Exception e) {
            log.error("推送链接到Redis队列失败: {}", actualQueueName, e);
        }

        return successCount;
    }

    @Override
    public long getQueueSize(String queueName) {
        try {
            String actualQueueName = StringUtils.hasText(queueName) ? queueName : APP_QUEUE_PREFIX + DEFAULT_QUEUE_NAME;
            Long size = redisTemplate.opsForList().size(actualQueueName);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("获取队列大小失败: {}", queueName, e);
            return 0;
        }
    }

    @Override
    public boolean clearQueue(String queueName) {
        try {
            String actualQueueName = StringUtils.hasText(queueName) ? queueName : APP_QUEUE_PREFIX + DEFAULT_QUEUE_NAME;
            Boolean result = redisTemplate.delete(actualQueueName);
            log.info("清空队列 {} 结果: {}", actualQueueName, result);
            return result != null && result;
        } catch (Exception e) {
            log.error("清空队列失败: {}", queueName, e);
            return false;
        }
    }
}