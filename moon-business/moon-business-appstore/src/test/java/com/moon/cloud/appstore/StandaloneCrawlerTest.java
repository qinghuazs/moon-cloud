package com.moon.cloud.appstore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 独立爬虫测试程序 - 不依赖Spring Boot框架
 * 用于测试App Store页面爬取功能
 */
public class StandaloneCrawlerTest {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int FETCH_TIMEOUT = 30000; // 30秒超时

    /**
     * 测试爬取指定分类的应用
     */
    public static void testCrawlCategory(String categoryId, String categoryUrl) {
        System.out.println("========================================");
        System.out.println("开始测试爬取分类: " + categoryId);
        System.out.println("URL: " + categoryUrl);
        System.out.println("========================================");

        try {
            // 1. 获取页面内容
            System.out.println("\n1. 正在获取页面内容...");
            String htmlContent = fetchPageContent(categoryUrl);

            if (htmlContent == null || htmlContent.isEmpty()) {
                System.err.println("❌ 无法获取页面内容");
                return;
            }

            System.out.println("✅ 成功获取页面内容，长度: " + htmlContent.length() + " 字符");

            // 2. 提取应用链接
            System.out.println("\n2. 正在提取应用链接...");
            List<String> appLinks = extractAppLinks(htmlContent);

            System.out.println("✅ 成功提取 " + appLinks.size() + " 个应用链接");

            // 3. 显示前10个链接
            if (!appLinks.isEmpty()) {
                System.out.println("\n3. 前10个应用链接:");
                int count = Math.min(10, appLinks.size());
                for (int i = 0; i < count; i++) {
                    System.out.println("   " + (i + 1) + ". " + appLinks.get(i));
                }

                if (appLinks.size() > 10) {
                    System.out.println("   ... 还有 " + (appLinks.size() - 10) + " 个链接");
                }
            }

            // 4. 分析链接格式
            if (!appLinks.isEmpty()) {
                System.out.println("\n4. 链接格式分析:");
                String firstLink = appLinks.get(0);
                if (firstLink.contains("/app/")) {
                    String appId = extractAppId(firstLink);
                    System.out.println("   示例App ID: " + appId);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取页面内容
     */
    private static String fetchPageContent(String url) {
        try {
            System.out.println("   连接到: " + url);
            System.out.println("   User-Agent: " + USER_AGENT);
            System.out.println("   超时设置: " + (FETCH_TIMEOUT / 1000) + " 秒");

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(FETCH_TIMEOUT)
                    .ignoreContentType(true)
                    .get();

            return doc.html();
        } catch (IOException e) {
            System.err.println("   获取页面失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 提取应用链接
     */
    private static List<String> extractAppLinks(String htmlContent) {
        List<String> appLinks = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(htmlContent);

            // 查找class包含"we-lockup targeted-link"的元素
            Elements elements = doc.select(".we-lockup.targeted-link[href]");
            System.out.println("   找到 " + elements.size() + " 个匹配的元素");

            for (Element element : elements) {
                String href = element.attr("href");
                if (!href.isEmpty()) {
                    // 处理相对链接
                    if (href.startsWith("/")) {
                        href = "https://apps.apple.com" + href;
                    }
                    appLinks.add(href);
                }
            }

            // 如果上面的选择器没找到，尝试其他可能的选择器
            if (appLinks.isEmpty()) {
                System.out.println("   尝试备用选择器...");
                Elements altElements = doc.select("a[class*='we-lockup'][class*='targeted-link']");
                System.out.println("   备用选择器找到 " + altElements.size() + " 个元素");

                for (Element element : altElements) {
                    String href = element.attr("href");
                    if (!href.isEmpty()) {
                        if (href.startsWith("/")) {
                            href = "https://apps.apple.com" + href;
                        }
                        appLinks.add(href);
                    }
                }
            }

            // 如果还是没找到，尝试查找所有包含/app/的链接
            if (appLinks.isEmpty()) {
                System.out.println("   尝试查找所有应用链接...");
                Elements allLinks = doc.select("a[href*='/app/']");
                System.out.println("   找到 " + allLinks.size() + " 个包含'/app/'的链接");

                for (Element element : allLinks) {
                    String href = element.attr("href");
                    if (!href.isEmpty() && href.contains("/app/")) {
                        if (href.startsWith("/")) {
                            href = "https://apps.apple.com" + href;
                        }
                        // 避免重复
                        if (!appLinks.contains(href)) {
                            appLinks.add(href);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("   解析HTML时出错: " + e.getMessage());
        }

        return appLinks;
    }

    /**
     * 从链接中提取App ID
     */
    private static String extractAppId(String link) {
        try {
            // 链接格式: https://apps.apple.com/cn/app/xxx/id123456789
            if (link.contains("/id")) {
                String[] parts = link.split("/id");
                if (parts.length > 1) {
                    String idPart = parts[1];
                    // 提取数字部分
                    return "id" + idPart.replaceAll("[^0-9]", "");
                }
            }
        } catch (Exception e) {
            System.err.println("   提取App ID失败: " + e.getMessage());
        }
        return "unknown";
    }

    /**
     * 主函数 - 测试几个主要分类
     */
    public static void main(String[] args) {
        System.out.println("App Store 爬虫独立测试程序");
        System.out.println("测试时间: " + new java.util.Date());
        System.out.println();

        // 测试分类数据
        String[][] testCategories = {
            {"6004", "体育", "https://apps.apple.com/cn/genre/ios-体育/id6004"},
            {"6014", "游戏", "https://apps.apple.com/cn/genre/ios-游戏/id6014"},
            {"6018", "教育", "https://apps.apple.com/cn/genre/ios-教育/id6018"},
            {"6015", "生活", "https://apps.apple.com/cn/genre/ios-生活/id6015"},
            {"6000", "商务", "https://apps.apple.com/cn/genre/ios-商务/id6000"}
        };

        // 只测试第一个分类，避免请求过多
        System.out.println("将测试以下分类:");
        for (String[] category : testCategories) {
            System.out.println("- " + category[1] + " (ID: " + category[0] + ")");
        }
        System.out.println("\n注意: 为避免频繁请求，只测试第一个分类");

        // 测试第一个分类
        if (testCategories.length > 0) {
            String[] firstCategory = testCategories[0];
            testCrawlCategory(firstCategory[0], firstCategory[2]);
        }

        System.out.println("\n========================================");
        System.out.println("测试完成!");
        System.out.println("========================================");
    }
}