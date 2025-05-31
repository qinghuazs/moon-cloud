package com.moon.cloud.ai.mcp.jira;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Jira MCP 应用启动类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.moon.cloud.ai.mcp.jira.mapper")
public class JiraMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiraMcpApplication.class, args);
    }
}