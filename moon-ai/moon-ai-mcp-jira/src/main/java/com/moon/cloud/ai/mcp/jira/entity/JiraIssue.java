package com.moon.cloud.ai.mcp.jira.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Jira问题实体类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jira_issue")
public class JiraIssue {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Jira问题编号
     */
    private String issueKey;

    /**
     * 问题标题
     */
    private String summary;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 问题类型（Bug、Task、Story等）
     */
    private String issueType;

    /**
     * 问题状态（Open、In Progress、Resolved、Closed等）
     */
    private String status;

    /**
     * 优先级（High、Medium、Low等）
     */
    private String priority;

    /**
     * 所属服务/项目
     */
    private String serviceName;

    /**
     * 问题组件
     */
    private String component;

    /**
     * 问题版本
     */
    private String version;

    /**
     * 修复版本
     */
    private String fixVersion;

    /**
     * 报告人
     */
    private String reporter;

    /**
     * 经办人
     */
    private String assignee;

    /**
     * 解决方案描述
     */
    private String resolution;

    /**
     * 解决方案详细说明
     */
    private String resolutionDescription;

    /**
     * 相关补丁信息
     */
    private String patchInfo;

    /**
     * 补丁下载链接
     */
    private String patchUrl;

    /**
     * 问题标签（用逗号分隔）
     */
    private String labels;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 解决时间
     */
    private LocalDateTime resolvedTime;

    /**
     * 是否已删除（0-未删除，1-已删除）
     */
    private Integer deleted;
}