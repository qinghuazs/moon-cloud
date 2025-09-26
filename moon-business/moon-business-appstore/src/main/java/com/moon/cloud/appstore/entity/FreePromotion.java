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
 * 限免推广记录表实体类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("free_promotions")
public class FreePromotion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 应用ID(关联apps表)
     */
    private String appId;

    /**
     * App Store应用ID
     */
    private String appstoreAppId;

    /**
     * 推广类型: FREE=限免, DISCOUNT=打折
     */
    private String promotionType;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 推广价格
     */
    private BigDecimal promotionPrice;

    /**
     * 折扣率(%)
     */
    private BigDecimal discountRate;

    /**
     * 节省金额
     */
    private BigDecimal savingsAmount;

    /**
     * 限免开始时间
     */
    private LocalDateTime startTime;

    /**
     * 限免结束时间(预估)
     */
    private LocalDateTime endTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 限免持续时长(小时)
     */
    private Integer durationHours;

    /**
     * 发现时间
     */
    private LocalDateTime discoveredAt;

    /**
     * 发现来源: AUTO=自动, USER=用户提交, EDITOR=编辑添加
     */
    private String discoverySource;

    /**
     * 确认时间
     */
    private LocalDateTime confirmedAt;

    /**
     * 确认人员
     */
    private String confirmedBy;

    /**
     * 状态: ACTIVE=进行中, ENDED=已结束, INVALID=无效
     */
    private String status;

    /**
     * 是否编辑推荐
     */
    private Boolean isFeatured;

    /**
     * 是否热门
     */
    private Boolean isHot;

    /**
     * 优先级得分(用于排序)
     */
    private Integer priorityScore;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 点击次数
     */
    private Integer clickCount;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}