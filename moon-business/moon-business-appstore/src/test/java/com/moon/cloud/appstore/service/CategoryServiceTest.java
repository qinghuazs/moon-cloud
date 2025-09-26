package com.moon.cloud.appstore.service;

import com.moon.cloud.appstore.entity.Category;
import com.moon.cloud.appstore.mapper.CategoryMapper;
import com.moon.cloud.appstore.service.impl.CategoryServiceImpl;
import com.moon.cloud.appstore.vo.CategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 分类服务单元测试
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryVO testCategoryVO;

    @BeforeEach
    void setUp() {
        testCategory = new Category()
                .setId("test-id-123")
                .setCategoryId("6004")
                .setParentId(null)
                .setNameCn("体育")
                .setNameEn("Sports")
                .setCategoryType("APP")
                .setIconUrl("https://example.com/icon.png")
                .setCategoriesUrl("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid")
                .setSortOrder(1)
                .setIsActive(true)
                .setDescription("体育相关应用分类")
                .setAppCount(100)
                .setFreeAppCount(5)
                .setAvgRating(new BigDecimal("4.5"))
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        testCategoryVO = new CategoryVO();
        testCategoryVO.setCategoryId("6004");
        testCategoryVO.setNameCn("体育");
        testCategoryVO.setNameEn("Sports");
        testCategoryVO.setCategoryType("APP");
        testCategoryVO.setIconUrl("https://example.com/icon.png");
        testCategoryVO.setCategoriesUrl("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid");
        testCategoryVO.setAppCount(100);
        testCategoryVO.setFreeAppCount(5);
        testCategoryVO.setAvgRating(new BigDecimal("4.5"));
    }

    @Test
    void testAddCategory() {
        // given
        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        // when
        boolean result = categoryService.addCategory(testCategory);

        // then
        assertThat(result).isTrue();
        verify(categoryMapper, times(1)).insert(testCategory);
    }

    @Test
    void testAddCategoryFailed() {
        // given
        when(categoryMapper.insert(any(Category.class))).thenReturn(0);

        // when
        boolean result = categoryService.addCategory(testCategory);

        // then
        assertThat(result).isFalse();
        verify(categoryMapper, times(1)).insert(testCategory);
    }

    @Test
    void testAddCategoryException() {
        // given
        when(categoryMapper.insert(any(Category.class))).thenThrow(new RuntimeException("Database error"));

        // when
        boolean result = categoryService.addCategory(testCategory);

        // then
        assertThat(result).isFalse();
        verify(categoryMapper, times(1)).insert(testCategory);
    }

    @Test
    void testUpdateCategory() {
        // given
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        // when
        boolean result = categoryService.updateCategory(testCategory);

        // then
        assertThat(result).isTrue();
        verify(categoryMapper, times(1)).updateById(testCategory);
    }

    @Test
    void testUpdateCategoryFailed() {
        // given
        when(categoryMapper.updateById(any(Category.class))).thenReturn(0);

        // when
        boolean result = categoryService.updateCategory(testCategory);

        // then
        assertThat(result).isFalse();
        verify(categoryMapper, times(1)).updateById(testCategory);
    }

    @Test
    void testDeleteCategory() {
        // given
        when(categoryMapper.deleteById(anyString())).thenReturn(1);

        // when
        boolean result = categoryService.deleteCategory("test-id-123");

        // then
        assertThat(result).isTrue();
        verify(categoryMapper, times(1)).deleteById("test-id-123");
    }

    @Test
    void testDeleteCategoryFailed() {
        // given
        when(categoryMapper.deleteById(anyString())).thenReturn(0);

        // when
        boolean result = categoryService.deleteCategory("test-id-123");

        // then
        assertThat(result).isFalse();
        verify(categoryMapper, times(1)).deleteById("test-id-123");
    }

    @Test
    void testGetCategoryByPrimaryId() {
        // given
        when(categoryMapper.selectById("test-id-123")).thenReturn(testCategory);

        // when
        Category result = categoryService.getCategoryByPrimaryId("test-id-123");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-id-123");
        assertThat(result.getCategoryId()).isEqualTo("6004");
        assertThat(result.getNameCn()).isEqualTo("体育");
        assertThat(result.getCategoriesUrl()).isEqualTo("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid");
        verify(categoryMapper, times(1)).selectById("test-id-123");
    }

    @Test
    void testGetCategoryByPrimaryIdNotFound() {
        // given
        when(categoryMapper.selectById("non-exist-id")).thenReturn(null);

        // when
        Category result = categoryService.getCategoryByPrimaryId("non-exist-id");

        // then
        assertThat(result).isNull();
        verify(categoryMapper, times(1)).selectById("non-exist-id");
    }

    @Test
    void testBatchInsertCategories() {
        // given
        Category category1 = new Category().setCategoryId("6001").setNameCn("天气");
        Category category2 = new Category().setCategoryId("6002").setNameCn("工具");
        List<Category> categories = Arrays.asList(category1, category2);

        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        // when
        boolean result = categoryService.batchInsertCategories(categories);

        // then
        assertThat(result).isTrue();
        verify(categoryMapper, times(2)).insert(any(Category.class));
    }

    @Test
    void testBatchInsertCategoriesFailed() {
        // given
        Category category1 = new Category().setCategoryId("6001").setNameCn("天气");
        List<Category> categories = Arrays.asList(category1);

        when(categoryMapper.insert(any(Category.class))).thenThrow(new RuntimeException("Database error"));

        // when
        boolean result = categoryService.batchInsertCategories(categories);

        // then
        assertThat(result).isFalse();
        verify(categoryMapper, times(1)).insert(any(Category.class));
    }

    @Test
    void testGetAppCategories() {
        // given
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryMapper.selectList(any())).thenReturn(categories);

        // when
        List<CategoryVO> result = categoryService.getAppCategories();

        // then
        assertThat(result).hasSize(1);
        CategoryVO categoryVO = result.get(0);
        assertThat(categoryVO.getCategoryId()).isEqualTo("6004");
        assertThat(categoryVO.getNameCn()).isEqualTo("体育");
        assertThat(categoryVO.getCategoriesUrl()).isEqualTo("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid");
        assertThat(categoryVO.getCategoryType()).isEqualTo("APP");
        verify(categoryMapper, times(1)).selectList(any());
    }

    @Test
    void testGetCategoryById() {
        // given
        when(categoryMapper.selectById("6004")).thenReturn(testCategory);
        when(categoryMapper.selectList(any())).thenReturn(Arrays.asList());

        // when
        CategoryVO result = categoryService.getCategoryById("6004");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isEqualTo("6004");
        assertThat(result.getNameCn()).isEqualTo("体育");
        assertThat(result.getCategoriesUrl()).isEqualTo("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid");
        verify(categoryMapper, times(1)).selectById("6004");
    }

    @Test
    void testGetCategoryByIdNotFound() {
        // given
        when(categoryMapper.selectById("non-exist")).thenReturn(null);

        // when
        CategoryVO result = categoryService.getCategoryById("non-exist");

        // then
        assertThat(result).isNull();
        verify(categoryMapper, times(1)).selectById("non-exist");
    }
}