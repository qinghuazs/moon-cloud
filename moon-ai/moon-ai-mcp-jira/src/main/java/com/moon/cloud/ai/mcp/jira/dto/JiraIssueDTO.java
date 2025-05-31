package com.moon.cloud.ai.mcp.jira.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Jira问题数据传输对象
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
public class JiraIssueDTO {

    /**
     * 主键ID
     */
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
     * 问题类型
     */
    private String issueType;

    /**
     * 问题状态
     */
    private String status;

    /**
     * 优先级
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
     * 问题标签列表
     */
    private List<String> labelList;

    /**
     * 设置标签列表（支持链式调用）
     */
    public JiraIssueDTO setLabelList(List<String> labelList) {
        this.labelList = labelList;
        return this;
    }

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
}