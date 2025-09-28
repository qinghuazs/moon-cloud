package com.moon.cloud.appstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 限免应用查询参数
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "限免应用查询参数")
public class FreeAppQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "推广类型: FREE=限免, DISCOUNT=打折, 空=全部")
    private String promotionType;

    @Schema(description = "最低原价")
    private BigDecimal minOriginalPrice;

    @Schema(description = "最高原价")
    private BigDecimal maxOriginalPrice;

    @Schema(description = "最低评分")
    private BigDecimal minRating;

    @Schema(description = "开发者名称")
    private String developerName;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "排序字段: time=时间, price=价格, rating=评分, savings=节省金额, hot=热度")
    private String sortBy = "time";

    @Schema(description = "排序方向: asc=升序, desc=降序")
    private String sortOrder = "desc";

    @Schema(description = "是否只显示新发现(6小时内)")
    private Boolean onlyNew;

    @Schema(description = "是否只显示即将结束(6小时内)")
    private Boolean onlyEndingSoon;

    @Schema(description = "是否只显示热门")
    private Boolean onlyHot;

    @Schema(description = "是否只显示编辑推荐")
    private Boolean onlyFeatured;

    @Schema(description = "状态: ACTIVE=进行中, ENDED=已结束")
    private String status = "ACTIVE";

    @Schema(description = "设备类型: iPhone, iPad, Universal")
    private String deviceType;

    @Schema(description = "是否有内购")
    private Boolean hasInAppPurchase;

    @Schema(description = "语言: ZH=中文, EN=英文")
    private String language;

    /**
     * 获取排序字段的数据库列名
     */
    public String getSortColumn() {
        if (sortBy == null) {
            return "discovered_at";
        }

        switch (sortBy.toLowerCase()) {
            case "price":
                return "original_price";
            case "rating":
                return "rating";
            case "savings":
                return "savings_amount";
            case "hot":
                return "priority_score";
            case "time":
            default:
                return "discovered_at";
        }
    }

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
}