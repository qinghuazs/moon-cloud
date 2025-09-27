package com.moon.cloud.appstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * App爬取失败记录实体类
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app_crawl_failures")
public class AppCrawlFailure implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * App URL
     */
    private String url;

    /**
     * App ID
     */
    private String appId;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 状态: 0=未解决, 1=已解决
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 解决时间
     */
    private LocalDateTime resolvedAt;

    /**
     * 备注
     */
    private String remark;
}