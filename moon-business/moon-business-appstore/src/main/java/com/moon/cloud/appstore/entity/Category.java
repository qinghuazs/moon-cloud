package com.moon.cloud.appstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用分类表实体类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("categories")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * App Store分类ID
     */
    private String categoryId;

    /**
     * 父分类ID
     */
    private String parentId;

    /**
     * 中文名称
     */
    private String nameCn;

    /**
     * 英文名称
     */
    private String nameEn;

    /**
     * 分类类型: GAME=游戏, APP=应用
     */
    private String categoryType;

    /**
     * 分类图标URL
     */
    private String iconUrl;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 分类页面URL，用于爬虫获取该分类下的APP信息
     */
    private String categoriesUrl;

    /**
     * 应用总数
     */
    private Integer appCount;

    /**
     * 当前限免应用数
     */
    private Integer freeAppCount;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}