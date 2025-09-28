package com.moon.cloud.appstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.AppSearchDTO;
import com.moon.cloud.appstore.vo.AppSearchResultVO;

import java.util.List;

/**
 * 应用搜索服务接口
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
public interface AppSearchService {

    /**
     * 搜索应用
     *
     * @param searchDTO 搜索参数
     * @return 搜索结果分页数据
     */
    Page<AppSearchResultVO> searchApps(AppSearchDTO searchDTO);

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词
     * @param limit   返回数量限制
     * @return 搜索建议列表
     */
    List<String> getSearchSuggestions(String keyword, Integer limit);

    /**
     * 获取热门搜索词
     *
     * @param limit 返回数量限制
     * @return 热门搜索词列表
     */
    List<String> getHotSearchKeywords(Integer limit);

    /**
     * 记录搜索历史
     *
     * @param userId  用户ID（可选）
     * @param keyword 搜索关键词
     */
    void recordSearchHistory(String userId, String keyword);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 搜索历史列表
     */
    List<String> getUserSearchHistory(String userId, Integer limit);

    /**
     * 清除用户搜索历史
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearUserSearchHistory(String userId);

    /**
     * 构建搜索索引
     * 重建所有应用的搜索索引
     *
     * @return 索引应用数量
     */
    int buildSearchIndex();

    /**
     * 更新应用搜索索引
     *
     * @param appId 应用ID
     * @return 是否成功
     */
    boolean updateAppSearchIndex(String appId);

    /**
     * 删除应用搜索索引
     *
     * @param appId 应用ID
     * @return 是否成功
     */
    boolean deleteAppSearchIndex(String appId);

    /**
     * 高级搜索
     * 支持更多搜索条件
     *
     * @param searchDTO 搜索参数
     * @return 搜索结果
     */
    Page<AppSearchResultVO> advancedSearch(AppSearchDTO searchDTO);
}