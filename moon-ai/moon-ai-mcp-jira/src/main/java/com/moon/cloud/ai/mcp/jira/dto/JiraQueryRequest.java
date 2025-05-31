package com.moon.cloud.ai.mcp.jira.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Jira问题查询请求
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
public class JiraQueryRequest {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 搜索关键词
     */
    private String keyword;

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
     * 是否只查询已解决的问题
     */
    private Boolean onlyResolved;

    /**
     * 是否只查询有补丁的问题
     */
    private Boolean onlyWithPatch;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}