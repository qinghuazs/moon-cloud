package com.moon.cloud.ai.mcp.jira.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.moon.cloud.ai.mcp.jira.service.JiraIssueService;

@Configuration

public class JiraMcpServerConfig {
    
    // @Bean
    // public McpServer mcpServer(JiraIssueService jiraIssueService) {
    //     return McpServer.builder()
    //         .info(ServerInfo.builder()
    //             .name("jira-mcp-server")
    //             .version("1.0.0")
    //             .description("Jira问题查询和匹配的MCP服务")
    //             .build())
    //         .tool("search_jira_issues", this::searchJiraIssues)
    //         .tool("match_similar_issues", this::matchSimilarIssues)
    //         .tool("get_issue_detail", this::getIssueDetail)
    //         .build();
    // }
    
    // // 实现具体的工具方法
    // private ToolResult searchJiraIssues(Map<String, Object> arguments) {
    //     // 调用现有的 JiraIssueService 方法
    //     // return new ToolResult("", false);
    // }
}
