package com.moon.cloud.appstore.controller;

import com.moon.cloud.appstore.dto.CategoryCreateDTO;
import com.moon.cloud.appstore.dto.CategoryUpdateDTO;
import com.moon.cloud.appstore.entity.Category;
import com.moon.cloud.appstore.service.CategoryService;
import com.moon.cloud.appstore.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类控制器
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appstore/categories")
@Tag(name = "分类管理", description = "应用分类相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类（树形结构）")
    @GetMapping
    public List<CategoryVO> getAllCategories() {
        log.info("获取所有分类");
        return categoryService.getAllCategories();
    }

    @Operation(summary = "获取应用分类列表")
    @GetMapping("/apps")
    public List<CategoryVO> getAppCategories() {
        log.info("获取应用分类列表");
        return categoryService.getAppCategories();
    }

    @Operation(summary = "获取游戏分类列表")
    @GetMapping("/games")
    public List<CategoryVO> getGameCategories() {
        log.info("获取游戏分类列表");
        return categoryService.getGameCategories();
    }

    @Operation(summary = "根据ID获取分类详情")
    @GetMapping("/{categoryId}")
    public CategoryVO getCategoryById(
            @Parameter(description = "分类ID", required = true)
            @PathVariable String categoryId) {
        log.info("获取分类详情, categoryId: {}", categoryId);
        return categoryService.getCategoryById(categoryId);
    }

    @Operation(summary = "更新分类统计信息")
    @PostMapping("/{categoryId}/statistics")
    public ResponseEntity<Void> updateCategoryStatistics(
            @Parameter(description = "分类ID", required = true)
            @PathVariable String categoryId) {
        log.info("更新分类统计信息, categoryId: {}", categoryId);
        categoryService.updateCategoryStatistics(categoryId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public ResponseEntity<String> addCategory(@Valid @RequestBody CategoryCreateDTO createDTO) {
        log.info("新增分类: {}", createDTO);

        Category category = new Category()
                .setCategoryId(createDTO.getCategoryId())
                .setParentId(createDTO.getParentId())
                .setNameCn(createDTO.getNameCn())
                .setNameEn(createDTO.getNameEn())
                .setCategoryType(createDTO.getCategoryType())
                .setIconUrl(createDTO.getIconUrl())
                .setCategoriesUrl(createDTO.getCategoriesUrl())
                .setSortOrder(createDTO.getSortOrder())
                .setIsActive(createDTO.getIsActive())
                .setDescription(createDTO.getDescription())
                .setAppCount(0)
                .setFreeAppCount(0)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());

        boolean success = categoryService.addCategory(category);
        if (success) {
            return ResponseEntity.ok("新增分类成功");
        } else {
            return ResponseEntity.badRequest().body("新增分类失败");
        }
    }

    @Operation(summary = "更新分类信息")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(
            @Parameter(description = "分类主键ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody CategoryUpdateDTO updateDTO) {
        log.info("更新分类信息, id: {}, updateDTO: {}", id, updateDTO);

        Category existingCategory = categoryService.getCategoryByPrimaryId(id);
        if (existingCategory == null) {
            return ResponseEntity.notFound().build();
        }

        Category category = existingCategory
                .setCategoryId(updateDTO.getCategoryId() != null ? updateDTO.getCategoryId() : existingCategory.getCategoryId())
                .setParentId(updateDTO.getParentId())
                .setNameCn(updateDTO.getNameCn())
                .setNameEn(updateDTO.getNameEn())
                .setCategoryType(updateDTO.getCategoryType() != null ? updateDTO.getCategoryType() : existingCategory.getCategoryType())
                .setIconUrl(updateDTO.getIconUrl())
                .setCategoriesUrl(updateDTO.getCategoriesUrl())
                .setSortOrder(updateDTO.getSortOrder() != null ? updateDTO.getSortOrder() : existingCategory.getSortOrder())
                .setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingCategory.getIsActive())
                .setDescription(updateDTO.getDescription())
                .setUpdatedAt(LocalDateTime.now());

        boolean success = categoryService.updateCategory(category);
        if (success) {
            return ResponseEntity.ok("更新分类成功");
        } else {
            return ResponseEntity.badRequest().body("更新分类失败");
        }
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(
            @Parameter(description = "分类主键ID", required = true)
            @PathVariable String id) {
        log.info("删除分类, id: {}", id);

        Category existingCategory = categoryService.getCategoryByPrimaryId(id);
        if (existingCategory == null) {
            return ResponseEntity.notFound().build();
        }

        boolean success = categoryService.deleteCategory(id);
        if (success) {
            return ResponseEntity.ok("删除分类成功");
        } else {
            return ResponseEntity.badRequest().body("删除分类失败");
        }
    }

    @Operation(summary = "根据主键ID获取分类详情")
    @GetMapping("/detail/{id}")
    public ResponseEntity<Category> getCategoryByPrimaryId(
            @Parameter(description = "分类主键ID", required = true)
            @PathVariable String id) {
        log.info("根据主键ID获取分类详情, id: {}", id);

        Category category = categoryService.getCategoryByPrimaryId(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(category);
    }
}