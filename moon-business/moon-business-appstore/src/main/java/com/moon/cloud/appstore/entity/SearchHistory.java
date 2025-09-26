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
 * 搜索历史表实体类
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("search_history")
public class SearchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID(未登录用户为NULL)
     */
    private String userId;

    /**
     * 设备标识符
     */
    private String deviceId;

    /**
     * 搜索关键词
     */
    private String searchQuery;

    /**
     * 标准化搜索词
     */
    private String searchQueryNormalized;

    /**
     * 搜索类型: NORMAL=普通, VOICE=语音
     */
    private String searchType;

    /**
     * 搜索结果数量
     */
    private Integer resultCount;

    /**
     * 点击的应用ID
     */
    private String clickedAppId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 平台信息
     */
    private String platform;

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * 搜索时间
     */
    private LocalDateTime searchedAt;
}