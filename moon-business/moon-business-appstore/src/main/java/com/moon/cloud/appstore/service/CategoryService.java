package com.moon.cloud.appstore.service;

import com.moon.cloud.appstore.entity.Category;
import com.moon.cloud.appstore.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
public interface CategoryService {

    /**
     * 获取所有分类列表（树形结构）
     *
     * @return 分类列表
     */
    List<CategoryVO> getAllCategories();

    /**
     * 获取应用分类列表
     *
     * @return 应用分类列表
     */
    List<CategoryVO> getAppCategories();

    /**
     * 获取游戏分类列表
     *
     * @return 游戏分类列表
     */
    List<CategoryVO> getGameCategories();

    /**
     * 根据分类ID获取分类信息
     *
     * @param categoryId 分类ID
     * @return 分类信息
     */
    CategoryVO getCategoryById(String categoryId);

    /**
     * 更新分类统计信息
     *
     * @param categoryId 分类ID
     */
    void updateCategoryStatistics(String categoryId);

    /**
     * 新增分类
     *
     * @param category 分类信息
     * @return 新增结果
     */
    boolean addCategory(Category category);

    /**
     * 更新分类信息
     *
     * @param category 分类信息
     * @return 更新结果
     */
    boolean updateCategory(Category category);

    /**
     * 删除分类
     *
     * @param id 分类主键ID
     * @return 删除结果
     */
    boolean deleteCategory(String id);

    /**
     * 根据主键ID获取分类信息
     *
     * @param id 分类主键ID
     * @return 分类信息
     */
    Category getCategoryByPrimaryId(String id);

    /**
     * 批量初始化分类数据
     *
     * @param categories 分类列表
     * @return 初始化结果
     */
    boolean batchInsertCategories(List<Category> categories);

    /**
     * 获取所有激活状态的分类
     *
     * @return 激活的分类列表
     */
    List<Category> getAllActiveCategories();
}