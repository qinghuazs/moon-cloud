-- Jira MCP 数据库初始化脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS jira_mcp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE jira_mcp;

-- 创建 jira_issue 表
CREATE TABLE IF NOT EXISTS jira_issue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    issue_key VARCHAR(50) NOT NULL COMMENT 'Jira问题编号',
    summary VARCHAR(500) NOT NULL COMMENT '问题标题',
    description TEXT COMMENT '问题描述',
    issue_type VARCHAR(50) COMMENT '问题类型（Bug、Task、Story等）',
    status VARCHAR(50) COMMENT '问题状态（Open、In Progress、Resolved、Closed等）',
    priority VARCHAR(20) COMMENT '优先级（High、Medium、Low等）',
    service_name VARCHAR(100) COMMENT '所属服务/项目',
    component VARCHAR(100) COMMENT '问题组件',
    version VARCHAR(50) COMMENT '问题版本',
    fix_version VARCHAR(50) COMMENT '修复版本',
    reporter VARCHAR(100) COMMENT '报告人',
    assignee VARCHAR(100) COMMENT '经办人',
    resolution VARCHAR(100) COMMENT '解决方案描述',
    resolution_description TEXT COMMENT '解决方案详细说明',
    patch_info TEXT COMMENT '相关补丁信息',
    patch_url VARCHAR(500) COMMENT '补丁下载链接',
    labels TEXT COMMENT '问题标签（用逗号分隔）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    resolved_time DATETIME COMMENT '解决时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否已删除（0-未删除，1-已删除）',
    
    INDEX idx_issue_key (issue_key),
    INDEX idx_service_name (service_name),
    INDEX idx_status (status),
    INDEX idx_issue_type (issue_type),
    INDEX idx_priority (priority),
    INDEX idx_created_time (created_time),
    INDEX idx_resolved_time (resolved_time),
    INDEX idx_deleted (deleted),
    FULLTEXT INDEX ft_summary_description (summary, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jira问题表';

-- 插入示例数据
INSERT INTO jira_issue (
    issue_key, summary, description, issue_type, status, priority, service_name, 
    component, version, fix_version, reporter, assignee, resolution, 
    resolution_description, patch_info, patch_url, labels, resolved_time
) VALUES 
(
    'PROJ-001', 
    '用户登录接口响应超时', 
    '在高并发情况下，用户登录接口响应时间超过5秒，导致用户体验差。经过分析发现是数据库连接池配置不当导致的。', 
    'Bug', 
    'Resolved', 
    'High', 
    'user-service', 
    'authentication', 
    '1.0.0', 
    '1.0.1', 
    'zhang.san', 
    'li.si', 
    'Fixed', 
    '优化数据库连接池配置，增加最大连接数从10调整为50，设置合理的超时时间。同时添加了连接池监控。', 
    '修复补丁包含：1. 更新application.yml中的数据库连接池配置 2. 添加连接池监控代码', 
    'http://patch.example.com/user-service-v1.0.1.patch', 
    '登录,超时,数据库,连接池', 
    '2024-01-15 10:30:00'
),
(
    'PROJ-002', 
    '订单支付失败但扣款成功', 
    '用户在支付订单时，支付接口返回失败，但实际已经扣款成功，导致用户重复支付。', 
    'Bug', 
    'Resolved', 
    'Critical', 
    'payment-service', 
    'payment', 
    '2.1.0', 
    '2.1.1', 
    'wang.wu', 
    'zhao.liu', 
    'Fixed', 
    '修复支付回调处理逻辑，增加幂等性校验，确保支付状态的一致性。添加了支付状态同步机制。', 
    '包含支付回调优化、幂等性校验、状态同步机制的完整补丁', 
    'http://patch.example.com/payment-service-v2.1.1.patch', 
    '支付,扣款,回调,幂等性', 
    '2024-01-20 14:20:00'
),
(
    'PROJ-003', 
    '商品搜索结果不准确', 
    '用户搜索商品时，返回的结果与搜索关键词不匹配，影响用户购买体验。', 
    'Bug', 
    'In Progress', 
    'Medium', 
    'search-service', 
    'elasticsearch', 
    '1.5.0', 
    NULL, 
    'chen.qi', 
    'liu.ba', 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    '搜索,elasticsearch,相关性', 
    NULL
),
(
    'PROJ-004', 
    '系统内存使用率过高', 
    '生产环境中系统内存使用率持续超过80%，可能存在内存泄漏问题。', 
    'Bug', 
    'Resolved', 
    'High', 
    'order-service', 
    'cache', 
    '3.0.0', 
    '3.0.1', 
    'sun.jiu', 
    'zhou.shi', 
    'Fixed', 
    '发现是Redis缓存未设置过期时间导致的内存泄漏，已修复缓存策略并添加监控。', 
    '修复Redis缓存策略，添加过期时间设置和内存监控', 
    'http://patch.example.com/order-service-v3.0.1.patch', 
    '内存,缓存,Redis,监控', 
    '2024-01-25 16:45:00'
),
(
    'PROJ-005', 
    '文件上传功能异常', 
    '用户上传大文件时经常失败，错误信息显示连接超时。', 
    'Bug', 
    'Open', 
    'Medium', 
    'file-service', 
    'upload', 
    '1.2.0', 
    NULL, 
    'wu.yi', 
    'zheng.er', 
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    '文件上传,超时,大文件', 
    NULL
);

-- 创建索引以提高查询性能
CREATE INDEX idx_service_status ON jira_issue(service_name, status);
CREATE INDEX idx_service_type ON jira_issue(service_name, issue_type);
CREATE INDEX idx_resolution ON jira_issue(resolution);
CREATE INDEX idx_patch_info ON jira_issue(patch_info(100));