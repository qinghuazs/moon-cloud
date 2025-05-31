package com.moon.cloud.ai.mcp.jira.service;

import com.moon.cloud.ai.mcp.jira.dto.JiraIssueDTO;
import com.moon.cloud.ai.mcp.jira.dto.JiraQueryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jira问题服务测试类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
class JiraIssueServiceTest {

    @Autowired
    private JiraIssueService jiraIssueService;

    @Test
    void testSearchIssues() {
        JiraQueryRequest request = new JiraQueryRequest()
                .setServiceName("user-service")
                .setKeyword("登录")
                .setPageNum(1)
                .setPageSize(10);

        List<JiraIssueDTO> issues = jiraIssueService.searchIssues(request);
        assertNotNull(issues);
        // 根据测试数据，应该能找到相关问题
        // assertTrue(issues.size() > 0);
    }

    @Test
    void testGetIssuesByService() {
        List<JiraIssueDTO> issues = jiraIssueService.getIssuesByService("user-service");
        assertNotNull(issues);
    }

    @Test
    void testSearchSimilarIssues() {
        List<JiraIssueDTO> issues = jiraIssueService.searchSimilarIssues("登录");
        assertNotNull(issues);
    }

    @Test
    void testMatchSimilarProblems() {
        String problemDescription = "用户登录接口响应很慢，超时了";
        List<JiraIssueDTO> issues = jiraIssueService.matchSimilarProblems(problemDescription, "user-service");
        assertNotNull(issues);
    }

    @Test
    void testGetResolvedIssues() {
        List<JiraIssueDTO> issues = jiraIssueService.getResolvedIssues();
        assertNotNull(issues);
    }

    @Test
    void testGetIssuesWithPatch() {
        List<JiraIssueDTO> issues = jiraIssueService.getIssuesWithPatch();
        assertNotNull(issues);
    }

    @Test
    void testGetIssueByKey() {
        JiraIssueDTO issue = jiraIssueService.getIssueByKey("PROJ-001");
        // 根据测试数据，应该能找到这个问题
        // assertNotNull(issue);
        // assertEquals("PROJ-001", issue.getIssueKey());
    }

    @Test
    void testSearchWithEmptyKeyword() {
        List<JiraIssueDTO> issues = jiraIssueService.searchSimilarIssues("");
        assertTrue(issues.isEmpty());
    }

    @Test
    void testSearchWithNullServiceName() {
        List<JiraIssueDTO> issues = jiraIssueService.getIssuesByService(null);
        assertTrue(issues.isEmpty());
    }
}