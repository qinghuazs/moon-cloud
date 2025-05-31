package com.moon.cloud.ai.mcp.jira.controller;

import com.moon.cloud.ai.mcp.jira.dto.JiraIssueDTO;
import com.moon.cloud.ai.mcp.jira.dto.JiraQueryRequest;
import com.moon.cloud.ai.mcp.jira.dto.Result;
import com.moon.cloud.ai.mcp.jira.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Jira MCP 控制器
 * 提供Jira问题查询和匹配的MCP服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp/jira")
@RequiredArgsConstructor
public class JiraMcpController {

    private final JiraIssueService jiraIssueService;

    /**
     * 根据查询条件搜索问题
     *
     * @param request 查询请求
     * @return 问题列表
     */
    @PostMapping("/search")
    public Result<List<JiraIssueDTO>> searchIssues(@RequestBody JiraQueryRequest request) {
        try {
            List<JiraIssueDTO> issues = jiraIssueService.searchIssues(request);
            return Result.success("查询成功", issues);
        } catch (Exception e) {
            log.error("搜索Jira问题失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 根据服务名称查询问题
     *
     * @param serviceName 服务名称
     * @return 问题列表
     */
    @GetMapping("/service/{serviceName}")
    public Result<List<JiraIssueDTO>> getIssuesByService(@PathVariable String serviceName) {
        try {
            if (StringUtils.isBlank(serviceName)) {
                return Result.error("服务名称不能为空");
            }
            List<JiraIssueDTO> issues = jiraIssueService.getIssuesByService(serviceName);
            return Result.success("查询成功", issues);
        } catch (Exception e) {
            log.error("根据服务查询Jira问题失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 智能问题匹配 - 根据用户描述的问题，匹配相似的历史问题
     *
     * @param problemDescription 问题描述
     * @param serviceName 服务名称（可选）
     * @return 匹配的问题列表
     */
    @GetMapping("/match")
    public Result<List<JiraIssueDTO>> matchSimilarProblems(
            @RequestParam String problemDescription,
            @RequestParam(required = false) String serviceName) {
        try {
            if (StringUtils.isBlank(problemDescription)) {
                return Result.error("问题描述不能为空");
            }
            List<JiraIssueDTO> issues = jiraIssueService.matchSimilarProblems(problemDescription, serviceName);
            return Result.success("匹配成功", issues);
        } catch (Exception e) {
            log.error("匹配相似问题失败", e);
            return Result.error("匹配失败：" + e.getMessage());
        }
    }

    /**
     * 根据关键词搜索相似问题
     *
     * @param keyword 关键词
     * @return 问题列表
     */
    @GetMapping("/search/keyword")
    public Result<List<JiraIssueDTO>> searchSimilarIssues(@RequestParam String keyword) {
        try {
            if (StringUtils.isBlank(keyword)) {
                return Result.error("关键词不能为空");
            }
            List<JiraIssueDTO> issues = jiraIssueService.searchSimilarIssues(keyword);
            return Result.success("搜索成功", issues);
        } catch (Exception e) {
            log.error("搜索相似问题失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 根据服务和关键词搜索问题
     *
     * @param serviceName 服务名称
     * @param keyword 关键词
     * @return 问题列表
     */
    @GetMapping("/search/service-keyword")
    public Result<List<JiraIssueDTO>> searchIssuesByServiceAndKeyword(
            @RequestParam String serviceName,
            @RequestParam String keyword) {
        try {
            if (StringUtils.isBlank(serviceName) || StringUtils.isBlank(keyword)) {
                return Result.error("服务名称和关键词不能为空");
            }
            List<JiraIssueDTO> issues = jiraIssueService.searchIssuesByServiceAndKeyword(serviceName, keyword);
            return Result.success("搜索成功", issues);
        } catch (Exception e) {
            log.error("根据服务和关键词搜索问题失败", e);
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 获取已解决的问题列表
     *
     * @return 问题列表
     */
    @GetMapping("/resolved")
    public Result<List<JiraIssueDTO>> getResolvedIssues() {
        try {
            List<JiraIssueDTO> issues = jiraIssueService.getResolvedIssues();
            return Result.success("查询成功", issues);
        } catch (Exception e) {
            log.error("查询已解决问题失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取有补丁的问题列表
     *
     * @return 问题列表
     */
    @GetMapping("/with-patch")
    public Result<List<JiraIssueDTO>> getIssuesWithPatch() {
        try {
            List<JiraIssueDTO> issues = jiraIssueService.getIssuesWithPatch();
            return Result.success("查询成功", issues);
        } catch (Exception e) {
            log.error("查询有补丁的问题失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取问题详情
     *
     * @param id 问题ID
     * @return 问题详情
     */
    @GetMapping("/detail/{id}")
    public Result<JiraIssueDTO> getIssueById(@PathVariable Long id) {
        try {
            if (id == null) {
                return Result.error("问题ID不能为空");
            }
            JiraIssueDTO issue = jiraIssueService.getIssueById(id);
            if (issue == null) {
                return Result.error("问题不存在");
            }
            return Result.success("查询成功", issue);
        } catch (Exception e) {
            log.error("查询问题详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据问题编号获取问题详情
     *
     * @param issueKey 问题编号
     * @return 问题详情
     */
    @GetMapping("/detail/key/{issueKey}")
    public Result<JiraIssueDTO> getIssueByKey(@PathVariable String issueKey) {
        try {
            if (StringUtils.isBlank(issueKey)) {
                return Result.error("问题编号不能为空");
            }
            JiraIssueDTO issue = jiraIssueService.getIssueByKey(issueKey);
            if (issue == null) {
                return Result.error("问题不存在");
            }
            return Result.success("查询成功", issue);
        } catch (Exception e) {
            log.error("查询问题详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * MCP健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Jira MCP服务运行正常", "OK");
    }
}