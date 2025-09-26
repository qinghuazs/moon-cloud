package com.moon.cloud.appstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 限免应用列表查询DTO
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@Schema(description = "限免应用列表查询参数")
public class FreeAppListDTO {

    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "20")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;

    @Schema(description = "分类ID", example = "6014")
    private String categoryId;

    @Schema(description = "排序方式: discovery=发现时间, savings=节省金额, rating=评分", example = "discovery")
    @Pattern(regexp = "^(discovery|savings|rating)$", message = "排序方式必须是discovery、savings或rating")
    private String sortBy = "discovery";

    @Schema(description = "筛选条件: featured=编辑推荐, hot=热门, ending=即将结束", example = "featured")
    @Pattern(regexp = "^(featured|hot|ending)?$", message = "筛选条件必须是featured、hot或ending")
    private String filter;

    @Schema(description = "最低评分", example = "4.0")
    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 5, message = "评分不能大于5")
    private Double minRating;

    @Schema(description = "最低原价", example = "0")
    @Min(value = 0, message = "价格不能为负数")
    private Double minOriginalPrice;

    @Schema(description = "最高原价", example = "999")
    @Min(value = 0, message = "价格不能为负数")
    private Double maxOriginalPrice;
}