package com.moon.cloud.appstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.SearchDTO;
import com.moon.cloud.appstore.entity.SearchHistory;
import com.moon.cloud.appstore.vo.FreeAppVO;

import java.util.List;

/**
 * 搜索服务接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
public interface SearchService {

    /**
     * 搜索应用
     *
     * @param dto 搜索参数
     * @return 搜索结果列表
     */
    Page<FreeAppVO> searchApps(SearchDTO dto);

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词
     * @return 搜索建议列表
     */
    List<String> getSearchSuggestions(String keyword);

    /**
     * 获取热门搜索词
     *
     * @param limit 返回数量限制
     * @return 热门搜索词列表
     */
    List<String> getHotSearchKeywords(int limit);

    /**
     * 保存搜索历史
     *
     * @param searchHistory 搜索历史
     */
    void saveSearchHistory(SearchHistory searchHistory);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param limit 返回数量限制
     * @return 搜索历史列表
     */
    List<String> getUserSearchHistory(String userId, String deviceId, int limit);

    /**
     * 清除用户搜索历史
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    void clearSearchHistory(String userId, String deviceId);
}