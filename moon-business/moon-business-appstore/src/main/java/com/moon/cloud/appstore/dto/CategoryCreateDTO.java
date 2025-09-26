package com.moon.cloud.appstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 分类创建请求DTO
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@Schema(description = "分类创建请求")
public class CategoryCreateDTO {

    @Schema(description = "App Store分类ID", example = "6004", required = true)
    @NotBlank(message = "分类ID不能为空")
    private String categoryId;

    @Schema(description = "父分类ID", example = "GAME")
    private String parentId;

    @Schema(description = "中文名称", example = "体育", required = true)
    @NotBlank(message = "中文名称不能为空")
    private String nameCn;

    @Schema(description = "英文名称", example = "Sports", required = true)
    @NotBlank(message = "英文名称不能为空")
    private String nameEn;

    @Schema(description = "分类类型: GAME=游戏, APP=应用", example = "APP", required = true)
    @NotBlank(message = "分类类型不能为空")
    private String categoryType;

    @Schema(description = "分类图标URL", example = "https://example.com/icon.png")
    private String iconUrl;

    @Schema(description = "分类页面URL，用于爬虫获取APP信息", example = "https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid")
    private String categoriesUrl;

    @Schema(description = "排序权重", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否启用", example = "true")
    private Boolean isActive = true;

    @Schema(description = "分类描述", example = "体育相关应用")
    private String description;
}