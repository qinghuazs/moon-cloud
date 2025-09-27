package com.moon.cloud.appstore.service;

import com.moon.cloud.appstore.entity.AppPriceHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * APP价格历史服务接口
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
public interface AppPriceHistoryService {

    /**
     * 获取APP的价格历史记录
     *
     * @param appId     App Store ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 价格历史列表
     */
    List<AppPriceHistory> getAppPriceHistory(String appId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取APP的最新价格记录
     *
     * @param appId App Store ID
     * @return 最新价格记录
     */
    AppPriceHistory getLatestPrice(String appId);

    /**
     * 获取限免应用列表
     *
     * @param limit 限制数量
     * @return 限免应用列表
     */
    List<AppPriceHistory> getFreeApps(int limit);

    /**
     * 获取最近降价的应用
     *
     * @param days  天数
     * @param limit 限制数量
     * @return 降价应用列表
     */
    List<AppPriceHistory> getRecentPriceDrops(int days, int limit);

    /**
     * 获取APP的价格统计信息
     *
     * @param appId App Store ID
     * @return 统计信息
     */
    Map<String, Object> getAppPriceStatistics(String appId);

    /**
     * 获取APP的历史最低价
     *
     * @param appId App Store ID
     * @return 历史最低价
     */
    BigDecimal getHistoricalLowestPrice(String appId);

    /**
     * 获取APP的历史最高价
     *
     * @param appId App Store ID
     * @return 历史最高价
     */
    BigDecimal getHistoricalHighestPrice(String appId);

    /**
     * 记录价格变化
     *
     * @param priceHistory 价格历史记录
     * @return 是否成功
     */
    boolean recordPriceChange(AppPriceHistory priceHistory);

    /**
     * 批量记录价格变化
     *
     * @param priceHistoryList 价格历史记录列表
     * @return 成功数量
     */
    int batchRecordPriceChanges(List<AppPriceHistory> priceHistoryList);

    /**
     * 获取指定分类的价格变化
     *
     * @param categoryId 分类ID
     * @param days      天数
     * @param limit     限制数量
     * @return 价格变化列表
     */
    List<AppPriceHistory> getCategoryPriceChanges(String categoryId, int days, int limit);

    /**
     * 获取开发者的应用价格变化
     *
     * @param developerName 开发者名称
     * @param days         天数
     * @param limit        限制数量
     * @return 价格变化列表
     */
    List<AppPriceHistory> getDeveloperPriceChanges(String developerName, int days, int limit);

    /**
     * 标记为已通知
     *
     * @param id 价格历史记录ID
     * @return 是否成功
     */
    boolean markAsNotified(String id);

    /**
     * 获取待通知的价格变化
     *
     * @param changeTypes 变化类型列表
     * @param limit      限制数量
     * @return 待通知列表
     */
    List<AppPriceHistory> getPendingNotifications(List<String> changeTypes, int limit);
}