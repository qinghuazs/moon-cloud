package com.moon.cloud.appstore.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分类信息展示VO
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@Schema(description = "分类信息")
public class CategoryVO {

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "分类名称(中文)")
    private String nameCn;

    @Schema(description = "分类名称(英文)")
    private String nameEn;

    @Schema(description = "分类类型: GAME=游戏, APP=应用")
    private String categoryType;

    @Schema(description = "分类图标URL")
    private String iconUrl;

    @Schema(description = "分类页面URL，用于爬虫获取APP信息")
    private String categoriesUrl;

    @Schema(description = "应用总数")
    private Integer appCount;

    @Schema(description = "当前限免应用数")
    private Integer freeAppCount;

    @Schema(description = "平均评分")
    private BigDecimal avgRating;

    @Schema(description = "子分类列表")
    private List<CategoryVO> children;
}