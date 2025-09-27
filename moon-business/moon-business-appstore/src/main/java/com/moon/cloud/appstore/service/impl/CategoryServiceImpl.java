package com.moon.cloud.appstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.moon.cloud.appstore.entity.App;
import com.moon.cloud.appstore.entity.Category;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.mapper.AppMapper;
import com.moon.cloud.appstore.mapper.CategoryMapper;
import com.moon.cloud.appstore.mapper.FreePromotionMapper;
import com.moon.cloud.appstore.service.CategoryService;
import com.moon.cloud.appstore.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final AppMapper appMapper;
    private final FreePromotionMapper freePromotionMapper;

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryVO> getAllCategories() {
        // 查询所有分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsActive, true)
               .orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);

        // 构建树形结构
        return buildCategoryTree(categories);
    }

    @Override
    @Cacheable(value = "categories", key = "'app'")
    public List<CategoryVO> getAppCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCategoryType, "APP")
               .eq(Category::getIsActive, true)
               .orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);

        return categories.stream()
            .map(this::convertToCategoryVO)
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "categories", key = "'game'")
    public List<CategoryVO> getGameCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCategoryType, "GAME")
               .eq(Category::getIsActive, true)
               .orderByAsc(Category::getSortOrder);
        List<Category> categories = categoryMapper.selectList(wrapper);

        // 获取游戏主分类和子分类
        List<Category> parentCategory = categories.stream()
            .filter(c -> c.getParentId() == null)
            .collect(Collectors.toList());

        if (!parentCategory.isEmpty()) {
            CategoryVO gameCategory = convertToCategoryVO(parentCategory.get(0));

            // 添加子分类
            List<CategoryVO> children = categories.stream()
                .filter(c -> c.getParentId() != null)
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
            gameCategory.setChildren(children);

            List<CategoryVO> result = new ArrayList<>();
            result.add(gameCategory);
            return result;
        }

        return categories.stream()
            .map(this::convertToCategoryVO)
            .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryById(String categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return null;
        }

        CategoryVO vo = convertToCategoryVO(category);

        // 如果是父分类，加载子分类
        if (category.getParentId() == null) {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getParentId, categoryId)
                   .eq(Category::getIsActive, true)
                   .orderByAsc(Category::getSortOrder);
            List<Category> children = categoryMapper.selectList(wrapper);

            List<CategoryVO> childrenVOs = children.stream()
                .map(this::convertToCategoryVO)
                .collect(Collectors.toList());
            vo.setChildren(childrenVOs);
        }

        return vo;
    }

    @Override
    @Transactional
    public void updateCategoryStatistics(String categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return;
        }

        // 统计该分类下的应用总数
        LambdaQueryWrapper<App> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(App::getPrimaryCategoryId, category.getCategoryId())
                  .eq(App::getStatus, 1);
        Long appCount = appMapper.selectCount(appWrapper);

        // 统计该分类下的限免应用数
        LambdaQueryWrapper<App> freeAppWrapper = new LambdaQueryWrapper<>();
        freeAppWrapper.eq(App::getPrimaryCategoryId, category.getCategoryId())
                      .eq(App::getCurrentPrice, 0)
                      .eq(App::getStatus, 1);
        Long freeAppCount = appMapper.selectCount(freeAppWrapper);

        // 计算平均评分
        List<App> apps = appMapper.selectList(appWrapper);
        BigDecimal avgRating = null;
        if (!apps.isEmpty()) {
            double totalRating = apps.stream()
                .filter(app -> app.getRating() != null)
                .mapToDouble(app -> app.getRating().doubleValue())
                .average()
                .orElse(0.0);
            avgRating = BigDecimal.valueOf(totalRating).setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        // 更新分类统计信息
        LambdaUpdateWrapper<Category> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Category::getId, categoryId)
                     .set(Category::getAppCount, appCount.intValue())
                     .set(Category::getFreeAppCount, freeAppCount.intValue())
                     .set(Category::getAvgRating, avgRating);
        categoryMapper.update(null, updateWrapper);

        log.info("更新分类统计信息成功: categoryId={}, appCount={}, freeAppCount={}, avgRating={}",
                categoryId, appCount, freeAppCount, avgRating);
    }

    @Override
    @Transactional
    public boolean addCategory(Category category) {
        try {
            return categoryMapper.insert(category) > 0;
        } catch (Exception e) {
            log.error("新增分类失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateCategory(Category category) {
        try {
            return categoryMapper.updateById(category) > 0;
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteCategory(String id) {
        try {
            return categoryMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return false;
        }
    }

    @Override
    public Category getCategoryByPrimaryId(String id) {
        return categoryMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean batchInsertCategories(List<Category> categories) {
        try {
            for (Category category : categories) {
                categoryMapper.insert(category);
            }
            return true;
        } catch (Exception e) {
            log.error("批量插入分类失败", e);
            return false;
        }
    }

    @Override
    public List<Category> getAllActiveCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsActive, true)
               .orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    private List<CategoryVO> buildCategoryTree(List<Category> categories) {
        // 分组：父分类和子分类
        Map<String, List<Category>> childrenMap = categories.stream()
            .filter(c -> c.getParentId() != null)
            .collect(Collectors.groupingBy(Category::getParentId));

        // 构建树形结构
        List<CategoryVO> tree = new ArrayList<>();
        for (Category category : categories) {
            if (category.getParentId() == null) {
                CategoryVO vo = convertToCategoryVO(category);

                // 添加子分类
                List<Category> children = childrenMap.get(category.getCategoryId());
                if (children != null && !children.isEmpty()) {
                    List<CategoryVO> childrenVOs = children.stream()
                        .map(this::convertToCategoryVO)
                        .collect(Collectors.toList());
                    vo.setChildren(childrenVOs);
                }

                tree.add(vo);
            }
        }

        return tree;
    }

    private CategoryVO convertToCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setCategoryId(category.getCategoryId());
        vo.setNameCn(category.getNameCn());
        vo.setNameEn(category.getNameEn());
        vo.setCategoryType(category.getCategoryType());
        vo.setIconUrl(category.getIconUrl());
        vo.setCategoriesUrl(category.getCategoriesUrl());
        vo.setAppCount(category.getAppCount());
        vo.setFreeAppCount(category.getFreeAppCount());
        vo.setAvgRating(category.getAvgRating());
        return vo;
    }
}