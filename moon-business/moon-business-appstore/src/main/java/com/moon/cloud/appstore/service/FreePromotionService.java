package com.moon.cloud.appstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppQueryDTO;
import com.moon.cloud.appstore.vo.FreeAppStatisticsVO;
import com.moon.cloud.appstore.vo.FreePromotionVO;

import java.util.List;

/**
 * 限免推广服务接口
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
public interface FreePromotionService {

    /**
     * 获取今日限免应用列表
     *
     * @param queryDTO 查询参数
     * @return 限免应用分页列表
     */
    Page<FreePromotionVO> getTodayFreeApps(FreeAppQueryDTO queryDTO);

    /**
     * 获取即将结束的限免应用
     *
     * @param hours 小时数（如：6表示6小时内结束）
     * @return 即将结束的限免应用列表
     */
    List<FreePromotionVO> getEndingSoonApps(int hours);

    /**
     * 获取热门限免应用
     *
     * @param limit 返回数量限制
     * @return 热门限免应用列表
     */
    List<FreePromotionVO> getHotFreeApps(int limit);

    /**
     * 获取限免统计信息
     *
     * @return 限免统计数据
     */
    FreeAppStatisticsVO getFreeAppStatistics();

    /**
     * 更新限免状态
     * 检查所有活跃的限免记录，更新已结束的限免状态
     */
    void updatePromotionStatus();

    /**
     * 检测并记录新的限免应用
     * 扫描所有应用，发现新的限免并记录
     *
     * @return 新发现的限免应用数量
     */
    int detectNewPromotions();

    /**
     * 根据分类获取限免应用
     *
     * @param categoryId 分类ID
     * @param page      页码
     * @param size      每页大小
     * @return 限免应用分页列表
     */
    Page<FreePromotionVO> getFreeAppsByCategory(String categoryId, int page, int size);

    /**
     * 获取指定应用的限免历史
     *
     * @param appId App Store ID
     * @return 限免历史记录
     */
    List<FreePromotionVO> getPromotionHistory(String appId);

    /**
     * 标记限免为已查看
     *
     * @param promotionId 限免记录ID
     * @return 是否成功
     */
    boolean markAsViewed(String promotionId);

    /**
     * 增加限免点击次数
     *
     * @param promotionId 限免记录ID
     * @return 是否成功
     */
    boolean increaseClickCount(String promotionId);
}