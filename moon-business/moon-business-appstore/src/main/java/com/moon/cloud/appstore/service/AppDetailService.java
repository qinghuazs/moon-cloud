package com.moon.cloud.appstore.service;

import com.moon.cloud.appstore.vo.AppDetailVO;
import com.moon.cloud.appstore.vo.AppPriceChartVO;
import com.moon.cloud.appstore.vo.AppSimilarVO;

import java.util.List;

/**
 * 应用详情服务接口
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
public interface AppDetailService {

    /**
     * 获取应用详细信息
     *
     * @param appId App Store ID 或 内部ID
     * @return 应用详情
     */
    AppDetailVO getAppDetail(String appId);

    /**
     * 获取应用价格历史图表数据
     *
     * @param appId App Store ID
     * @param days  天数（默认90天）
     * @return 价格历史图表数据
     */
    AppPriceChartVO getAppPriceChart(String appId, Integer days);

    /**
     * 获取相似应用推荐
     *
     * @param appId App Store ID
     * @param limit 返回数量限制
     * @return 相似应用列表
     */
    List<AppSimilarVO> getSimilarApps(String appId, Integer limit);

    /**
     * 获取同开发商的其他应用
     *
     * @param appId App Store ID
     * @param limit 返回数量限制
     * @return 应用列表
     */
    List<AppSimilarVO> getDeveloperApps(String appId, Integer limit);

    /**
     * 获取同分类的热门应用
     *
     * @param appId App Store ID
     * @param limit 返回数量限制
     * @return 应用列表
     */
    List<AppSimilarVO> getCategoryTopApps(String appId, Integer limit);

    /**
     * 增加应用查看次数
     *
     * @param appId App Store ID
     * @return 是否成功
     */
    boolean increaseViewCount(String appId);

    /**
     * 记录应用下载
     *
     * @param appId App Store ID
     * @return 是否成功
     */
    boolean recordDownload(String appId);

    /**
     * 获取应用的App Store链接
     *
     * @param appId App Store ID
     * @return App Store URL
     */
    String getAppStoreUrl(String appId);
}