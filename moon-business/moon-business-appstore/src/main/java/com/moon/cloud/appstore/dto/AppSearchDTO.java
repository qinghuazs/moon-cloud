package com.moon.cloud.appstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 应用搜索参数DTO
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "应用搜索参数")
public class AppSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "分类ID列表")
    private List<String> categoryIds;

    @Schema(description = "开发者名称")
    private String developerName;

    @Schema(description = "最低价格")
    private BigDecimal minPrice;

    @Schema(description = "最高价格")
    private BigDecimal maxPrice;

    @Schema(description = "是否免费")
    private Boolean isFree;

    @Schema(description = "是否有限免")
    private Boolean hasPromotion;

    @Schema(description = "最低评分")
    private BigDecimal minRating;

    @Schema(description = "支持的设备: iPhone, iPad, Universal")
    private String deviceType;

    @Schema(description = "语言: ZH=中文, EN=英文")
    private String language;

    @Schema(description = "是否有内购")
    private Boolean hasInAppPurchase;

    @Schema(description = "排序字段: relevance=相关度, rating=评分, downloads=下载量, price=价格, updated=更新时间")
    private String sortBy = "relevance";

    @Schema(description = "排序方向: asc=升序, desc=降序")
    private String sortOrder = "desc";

    @Schema(description = "搜索范围: all=全部, name=应用名, developer=开发者, description=描述")
    private String searchScope = "all";

    @Schema(description = "是否高亮关键词")
    private Boolean highlight = true;

    /**
     * 验证分页参数
     */
    public void validatePagination() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100; // 限制最大每页数量
        }
    }

    /**
     * 是否有筛选条件
     */
    public boolean hasFilters() {
        return categoryIds != null && !categoryIds.isEmpty()
                || developerName != null
                || minPrice != null
                || maxPrice != null
                || isFree != null
                || hasPromotion != null
                || minRating != null
                || deviceType != null
                || language != null
                || hasInAppPurchase != null;
    }
}