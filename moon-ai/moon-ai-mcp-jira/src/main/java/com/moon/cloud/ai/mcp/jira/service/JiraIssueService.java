package com.moon.cloud.ai.mcp.jira.service;

import com.moon.cloud.ai.mcp.jira.dto.JiraIssueDTO;
import com.moon.cloud.ai.mcp.jira.dto.JiraQueryRequest;
import com.moon.cloud.ai.mcp.jira.entity.JiraIssue;

import java.util.List;

/**
 * Jira问题服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface JiraIssueService {

    /**
     * 根据查询条件搜索问题
     *
     * @param request 查询请求
     * @return 问题列表
     */
    List<JiraIssueDTO> searchIssues(JiraQueryRequest request);

    /**
     * 根据服务名称查询问题
     *
     * @param serviceName 服务名称
     * @return 问题列表
     */
    List<JiraIssueDTO> getIssuesByService(String serviceName);

    /**
     * 根据关键词搜索相似问题
     *
     * @param keyword 关键词
     * @return 问题列表
     */
    List<JiraIssueDTO> searchSimilarIssues(String keyword);

    /**
     * 根据服务和关键词搜索问题
     *
     * @param serviceName 服务名称
     * @param keyword 关键词
     * @return 问题列表
     */
    List<JiraIssueDTO> searchIssuesByServiceAndKeyword(String serviceName, String keyword);

    /**
     * 获取已解决的问题列表
     *
     * @return 问题列表
     */
    List<JiraIssueDTO> getResolvedIssues();

    /**
     * 获取有补丁的问题列表
     *
     * @return 问题列表
     */
    List<JiraIssueDTO> getIssuesWithPatch();

    /**
     * 根据ID获取问题详情
     *
     * @param id 问题ID
     * @return 问题详情
     */
    JiraIssueDTO getIssueById(Long id);

    /**
     * 根据问题编号获取问题详情
     *
     * @param issueKey 问题编号
     * @return 问题详情
     */
    JiraIssueDTO getIssueByKey(String issueKey);

    /**
     * 保存或更新问题
     *
     * @param jiraIssue 问题实体
     * @return 是否成功
     */
    boolean saveOrUpdate(JiraIssue jiraIssue);

    /**
     * 删除问题
     *
     * @param id 问题ID
     * @return 是否成功
     */
    boolean deleteIssue(Long id);

    /**
     * 智能问题匹配 - 根据用户描述的问题，匹配相似的历史问题
     *
     * @param problemDescription 问题描述
     * @param serviceName 服务名称（可选）
     * @return 匹配的问题列表
     */
    List<JiraIssueDTO> matchSimilarProblems(String problemDescription, String serviceName);
}