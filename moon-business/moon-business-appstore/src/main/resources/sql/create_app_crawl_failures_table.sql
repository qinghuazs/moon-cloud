-- 创建App爬取失败记录表
CREATE TABLE IF NOT EXISTS `app_crawl_failures` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `url` varchar(500) NOT NULL COMMENT 'App URL',
  `app_id` varchar(100) DEFAULT NULL COMMENT 'App ID',
  `error_message` text COMMENT '错误信息',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0=未解决, 1=已解决',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `resolved_at` datetime DEFAULT NULL COMMENT '解决时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='App爬取失败记录表';

-- 添加索引以优化查询性能
ALTER TABLE `app_crawl_failures` ADD INDEX `idx_url_status` (`url`, `status`);
ALTER TABLE `app_crawl_failures` ADD INDEX `idx_retry_count` (`retry_count`);