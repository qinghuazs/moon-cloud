package com.moon.cloud.appstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 价格历史表实体类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("price_history")
public class PriceHistory implements Serializable {

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
     * 价格
     */
    private BigDecimal price;

    /**
     * 货币类型
     */
    private String currency;

    /**
     * 价格变化类型: INCREASE=上涨, DECREASE=下降, FREE=限免, NORMAL=正常
     */
    private String priceChangeType;

    /**
     * 变化金额
     */
    private BigDecimal changeAmount;

    /**
     * 变化百分比
     */
    private BigDecimal changePercentage;

    /**
     * 记录日期
     */
    private LocalDate recordDate;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 关联的限免记录ID
     */
    private String promotionId;

    /**
     * 上一次价格
     */
    private BigDecimal previousPrice;

    /**
     * 数据来源: AUTO=自动采集, MANUAL=手动录入
     */
    private String dataSource;

    /**
     * 采集批次ID
     */
    private String crawlSessionId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}