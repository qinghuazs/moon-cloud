package com.moon.cloud.appstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moon.cloud.appstore.dto.CategoryCreateDTO;
import com.moon.cloud.appstore.dto.CategoryUpdateDTO;
import com.moon.cloud.appstore.entity.Category;
import com.moon.cloud.appstore.service.CategoryService;
import com.moon.cloud.appstore.vo.CategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 分类控制器单元测试
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryVO testCategoryVO;
    private Category testCategory;
    private CategoryCreateDTO createDTO;
    private CategoryUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
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

        createDTO = new CategoryCreateDTO();
        createDTO.setCategoryId("6004");
        createDTO.setNameCn("体育");
        createDTO.setNameEn("Sports");
        createDTO.setCategoryType("APP");
        createDTO.setCategoriesUrl("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid");
        createDTO.setSortOrder(1);
        createDTO.setIsActive(true);
        createDTO.setDescription("体育相关应用分类");

        updateDTO = new CategoryUpdateDTO();
        updateDTO.setNameCn("体育运动");
        updateDTO.setNameEn("Sports & Fitness");
        updateDTO.setDescription("体育和健身相关应用");
    }

    @Test
    void testGetAllCategories() throws Exception {
        // given
        List<CategoryVO> categories = Arrays.asList(testCategoryVO);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // when & then
        mockMvc.perform(get("/api/appstore/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categoryId").value("6004"))
                .andExpect(jsonPath("$[0].nameCn").value("体育"))
                .andExpect(jsonPath("$[0].categoriesUrl").value("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid"));

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAppCategories() throws Exception {
        // given
        List<CategoryVO> categories = Arrays.asList(testCategoryVO);
        when(categoryService.getAppCategories()).thenReturn(categories);

        // when & then
        mockMvc.perform(get("/api/appstore/categories/apps"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].categoryId").value("6004"))
                .andExpect(jsonPath("$[0].categoryType").value("APP"));

        verify(categoryService, times(1)).getAppCategories();
    }

    @Test
    void testGetGameCategories() throws Exception {
        // given
        List<CategoryVO> categories = Arrays.asList(testCategoryVO);
        when(categoryService.getGameCategories()).thenReturn(categories);

        // when & then
        mockMvc.perform(get("/api/appstore/categories/games"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(categoryService, times(1)).getGameCategories();
    }

    @Test
    void testGetCategoryById() throws Exception {
        // given
        when(categoryService.getCategoryById("6004")).thenReturn(testCategoryVO);

        // when & then
        mockMvc.perform(get("/api/appstore/categories/6004"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryId").value("6004"))
                .andExpect(jsonPath("$.nameCn").value("体育"));

        verify(categoryService, times(1)).getCategoryById("6004");
    }

    @Test
    void testAddCategory() throws Exception {
        // given
        when(categoryService.addCategory(any(Category.class))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/appstore/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("新增分类成功"));

        verify(categoryService, times(1)).addCategory(any(Category.class));
    }

    @Test
    void testAddCategoryFailed() throws Exception {
        // given
        when(categoryService.addCategory(any(Category.class))).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/appstore/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("新增分类失败"));

        verify(categoryService, times(1)).addCategory(any(Category.class));
    }

    @Test
    void testAddCategoryValidationError() throws Exception {
        // given
        CategoryCreateDTO invalidDTO = new CategoryCreateDTO();
        // 缺少必填字段

        // when & then
        mockMvc.perform(post("/api/appstore/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).addCategory(any(Category.class));
    }

    @Test
    void testUpdateCategory() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("test-id-123")).thenReturn(testCategory);
        when(categoryService.updateCategory(any(Category.class))).thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/appstore/categories/test-id-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("更新分类成功"));

        verify(categoryService, times(1)).getCategoryByPrimaryId("test-id-123");
        verify(categoryService, times(1)).updateCategory(any(Category.class));
    }

    @Test
    void testUpdateCategoryNotFound() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("non-exist-id")).thenReturn(null);

        // when & then
        mockMvc.perform(put("/api/appstore/categories/non-exist-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).getCategoryByPrimaryId("non-exist-id");
        verify(categoryService, never()).updateCategory(any(Category.class));
    }

    @Test
    void testUpdateCategoryFailed() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("test-id-123")).thenReturn(testCategory);
        when(categoryService.updateCategory(any(Category.class))).thenReturn(false);

        // when & then
        mockMvc.perform(put("/api/appstore/categories/test-id-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("更新分类失败"));

        verify(categoryService, times(1)).updateCategory(any(Category.class));
    }

    @Test
    void testDeleteCategory() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("test-id-123")).thenReturn(testCategory);
        when(categoryService.deleteCategory("test-id-123")).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/api/appstore/categories/test-id-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("删除分类成功"));

        verify(categoryService, times(1)).getCategoryByPrimaryId("test-id-123");
        verify(categoryService, times(1)).deleteCategory("test-id-123");
    }

    @Test
    void testDeleteCategoryNotFound() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("non-exist-id")).thenReturn(null);

        // when & then
        mockMvc.perform(delete("/api/appstore/categories/non-exist-id"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).getCategoryByPrimaryId("non-exist-id");
        verify(categoryService, never()).deleteCategory(anyString());
    }

    @Test
    void testDeleteCategoryFailed() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("test-id-123")).thenReturn(testCategory);
        when(categoryService.deleteCategory("test-id-123")).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/api/appstore/categories/test-id-123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("删除分类失败"));

        verify(categoryService, times(1)).deleteCategory("test-id-123");
    }

    @Test
    void testGetCategoryByPrimaryId() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("test-id-123")).thenReturn(testCategory);

        // when & then
        mockMvc.perform(get("/api/appstore/categories/detail/test-id-123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("test-id-123"))
                .andExpect(jsonPath("$.categoryId").value("6004"))
                .andExpect(jsonPath("$.nameCn").value("体育"))
                .andExpect(jsonPath("$.categoriesUrl").value("https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid"));

        verify(categoryService, times(1)).getCategoryByPrimaryId("test-id-123");
    }

    @Test
    void testGetCategoryByPrimaryIdNotFound() throws Exception {
        // given
        when(categoryService.getCategoryByPrimaryId("non-exist-id")).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/appstore/categories/detail/non-exist-id"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).getCategoryByPrimaryId("non-exist-id");
    }

    @Test
    void testUpdateCategoryStatistics() throws Exception {
        // given
        doNothing().when(categoryService).updateCategoryStatistics("6004");

        // when & then
        mockMvc.perform(post("/api/appstore/categories/6004/statistics"))
                .andExpect(status().isOk());

        verify(categoryService, times(1)).updateCategoryStatistics("6004");
    }
}