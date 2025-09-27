package com.moon.cloud.appstore.service;

/**
 * App Store 爬虫服务接口
 * 用于抓取和处理App Store分类页面的应用信息
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
public interface CrawlerService {

    /**
     * 根据分类ID爬取该分类下的应用信息
     * 从数据库读取分类信息，获取分类URL，爬取页面内容，
     * 提取应用链接并存储到Redis队列
     *
     * @param categoryId 分类ID
     * @return 成功爬取的应用链接数量
     */
    int crawlAppsByCategoryId(String categoryId);

    /**
     * 爬取所有激活分类的应用信息
     *
     * @return 成功爬取的总应用链接数量
     */
    int crawlAllCategories();

    /**
     * 从指定URL爬取页面内容
     *
     * @param url 要爬取的页面URL
     * @return 页面HTML内容
     */
    String fetchPageContent(String url);

    /**
     * 从HTML内容中提取应用链接
     * 查找class属性为"we-lockup targeted-link"的元素，提取href属性
     *
     * @param htmlContent HTML内容
     * @return 应用链接列表
     */
    java.util.List<String> extractAppLinks(String htmlContent);

    /**
     * 将应用链接推送到Redis队列
     *
     * @param appLinks 应用链接列表
     * @param queueName 队列名称
     * @return 成功推送的链接数量
     */
    int pushLinksToRedisQueue(java.util.List<String> appLinks, String queueName);

    /**
     * 获取Redis队列中的链接数量
     *
     * @param queueName 队列名称
     * @return 队列中的链接数量
     */
    long getQueueSize(String queueName);

    /**
     * 清空Redis队列
     *
     * @param queueName 队列名称
     * @return 是否清空成功
     */
    boolean clearQueue(String queueName);
}