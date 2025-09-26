package com.moon.cloud.appstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 应用搜索DTO
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@Schema(description = "应用搜索参数")
public class SearchDTO {

    @Schema(description = "搜索关键词", example = "游戏")
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    @Schema(description = "搜索类型: all=全部, name=应用名称, developer=开发商", example = "all")
    @Pattern(regexp = "^(all|name|developer)$", message = "搜索类型必须是all、name或developer")
    private String searchType = "all";

    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "20")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;

    @Schema(description = "是否只搜索限免应用", example = "true")
    private Boolean onlyFree = false;

    @Schema(description = "分类ID筛选", example = "6014")
    private String categoryId;

    @Schema(description = "最低评分筛选", example = "4.0")
    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 5, message = "评分不能大于5")
    private Double minRating;
}