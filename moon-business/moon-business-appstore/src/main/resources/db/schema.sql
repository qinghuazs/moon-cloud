-- ============================================================
-- Moon Business AppStore 数据库DDL
--
-- 描述: APP Store限免应用推荐服务数据库结构定义
-- 作者: Moon Cloud
-- 日期: 2024-09-26
-- 版本: 1.0.0
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `moon_appstore`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `moon_appstore`;

-- ============================================================
-- 表结构定义
-- ============================================================

-- ----------------------------
-- 1. 应用信息表 (apps)
-- 存储App Store应用的基础信息
-- ----------------------------
DROP TABLE IF EXISTS `apps`;
CREATE TABLE `apps` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `app_id` varchar(20) NOT NULL COMMENT 'App Store应用ID',
  `bundle_id` varchar(255) DEFAULT NULL COMMENT '应用Bundle ID',
  `name` varchar(255) NOT NULL COMMENT '应用名称',
  `subtitle` varchar(255) DEFAULT NULL COMMENT '应用副标题',
  `description` text COMMENT '应用描述',
  `developer_name` varchar(255) NOT NULL COMMENT '开发商名称',
  `developer_id` varchar(20) DEFAULT NULL COMMENT '开发商ID',
  `developer_url` varchar(500) DEFAULT NULL COMMENT '开发商网站',
  `primary_category_id` varchar(20) NOT NULL COMMENT '主分类ID',
  `primary_category_name` varchar(100) NOT NULL COMMENT '主分类名称',
  `categories` json DEFAULT NULL COMMENT '所有分类信息(JSON)',
  `version` varchar(50) DEFAULT NULL COMMENT '当前版本号',
  `release_date` datetime DEFAULT NULL COMMENT '首次发布时间',
  `updated_date` datetime DEFAULT NULL COMMENT '最后更新时间',
  `release_notes` text COMMENT '版本更新说明',
  `file_size` bigint DEFAULT NULL COMMENT '应用大小(字节)',
  `minimum_os_version` varchar(20) DEFAULT NULL COMMENT '最低系统要求',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '应用图标URL',
  `screenshots` json DEFAULT NULL COMMENT '截图URLs(JSON数组)',
  `ipad_screenshots` json DEFAULT NULL COMMENT 'iPad截图URLs(JSON数组)',
  `preview_video_url` varchar(500) DEFAULT NULL COMMENT '预览视频URL',
  `rating` decimal(3,2) DEFAULT NULL COMMENT '平均评分',
  `rating_count` int DEFAULT NULL COMMENT '评分总数',
  `current_version_rating` decimal(3,2) DEFAULT NULL COMMENT '当前版本评分',
  `current_version_rating_count` int DEFAULT NULL COMMENT '当前版本评分数',
  `current_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价(用于计算优惠)',
  `currency` varchar(10) DEFAULT 'CNY' COMMENT '货币类型',
  `is_free` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否免费应用',
  `content_rating` varchar(20) DEFAULT NULL COMMENT '内容分级',
  `languages` json DEFAULT NULL COMMENT '支持语言(JSON数组)',
  `supported_devices` json DEFAULT NULL COMMENT '支持设备(JSON数组)',
  `features` json DEFAULT NULL COMMENT '应用特性(JSON数组)',
  `has_in_app_purchase` tinyint(1) DEFAULT '0' COMMENT '是否有内购',
  `has_ads` tinyint(1) DEFAULT NULL COMMENT '是否含广告',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态: 1=正常, 0=下架',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_crawled_at` timestamp DEFAULT NULL COMMENT '最后爬取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_category_price` (`primary_category_id`, `current_price`),
  KEY `idx_developer` (`developer_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_updated` (`updated_at`),
  KEY `idx_status_category` (`status`, `primary_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用信息表';

-- ----------------------------
-- 2. 限免推广记录表 (free_promotions)
-- 记录应用的限免活动信息
-- ----------------------------
DROP TABLE IF EXISTS `free_promotions`;
CREATE TABLE `free_promotions` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `app_id` varchar(32) NOT NULL COMMENT '应用ID(关联apps表)',
  `appstore_app_id` varchar(20) NOT NULL COMMENT 'App Store应用ID',
  `promotion_type` varchar(20) NOT NULL DEFAULT 'FREE' COMMENT '推广类型: FREE=限免, DISCOUNT=打折',
  `original_price` decimal(10,2) NOT NULL COMMENT '原价',
  `promotion_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '推广价格',
  `discount_rate` decimal(5,2) DEFAULT NULL COMMENT '折扣率(%)',
  `savings_amount` decimal(10,2) NOT NULL COMMENT '节省金额',
  `start_time` datetime NOT NULL COMMENT '限免开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '限免结束时间(预估)',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `duration_hours` int DEFAULT NULL COMMENT '限免持续时长(小时)',
  `discovered_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '发现时间',
  `discovery_source` varchar(50) DEFAULT NULL COMMENT '发现来源: AUTO=自动, USER=用户提交, EDITOR=编辑添加',
  `confirmed_at` timestamp DEFAULT NULL COMMENT '确认时间',
  `confirmed_by` varchar(100) DEFAULT NULL COMMENT '确认人员',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE=进行中, ENDED=已结束, INVALID=无效',
  `is_featured` tinyint(1) DEFAULT '0' COMMENT '是否编辑推荐',
  `is_hot` tinyint(1) DEFAULT '0' COMMENT '是否热门',
  `priority_score` int DEFAULT '0' COMMENT '优先级得分(用于排序)',
  `view_count` int DEFAULT '0' COMMENT '查看次数',
  `click_count` int DEFAULT '0' COMMENT '点击次数',
  `share_count` int DEFAULT '0' COMMENT '分享次数',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_promotion` (`appstore_app_id`, `start_time`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status_start` (`status`, `start_time` DESC),
  KEY `idx_featured_hot` (`is_featured`, `is_hot`, `priority_score` DESC),
  KEY `idx_discovery` (`discovered_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='限免推广记录表';

-- ----------------------------
-- 3. 应用分类表 (categories)
-- App Store官方分类信息
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `category_id` varchar(20) NOT NULL COMMENT 'App Store分类ID',
  `parent_id` varchar(20) DEFAULT NULL COMMENT '父分类ID',
  `name_cn` varchar(100) NOT NULL COMMENT '中文名称',
  `name_en` varchar(100) NOT NULL COMMENT '英文名称',
  `category_type` varchar(20) NOT NULL COMMENT '分类类型: GAME=游戏, APP=应用',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '分类图标URL',
  `sort_order` int DEFAULT '0' COMMENT '排序权重',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `description` text COMMENT '分类描述',
  `app_count` int DEFAULT '0' COMMENT '应用总数',
  `free_app_count` int DEFAULT '0' COMMENT '当前限免应用数',
  `avg_rating` decimal(3,2) DEFAULT NULL COMMENT '平均评分',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_id` (`category_id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_type_order` (`category_type`, `sort_order`),
  KEY `idx_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用分类表';

-- ----------------------------
-- 4. 价格历史表 (price_history)
-- 跟踪应用的价格变化历史
-- ----------------------------
DROP TABLE IF EXISTS `price_history`;
CREATE TABLE `price_history` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `app_id` varchar(32) NOT NULL COMMENT '应用ID(关联apps表)',
  `appstore_app_id` varchar(20) NOT NULL COMMENT 'App Store应用ID',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `currency` varchar(10) DEFAULT 'CNY' COMMENT '货币类型',
  `price_change_type` varchar(20) DEFAULT NULL COMMENT '价格变化类型: INCREASE=上涨, DECREASE=下降, FREE=限免, NORMAL=正常',
  `change_amount` decimal(10,2) DEFAULT NULL COMMENT '变化金额',
  `change_percentage` decimal(5,2) DEFAULT NULL COMMENT '变化百分比',
  `record_date` date NOT NULL COMMENT '记录日期',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `promotion_id` varchar(32) DEFAULT NULL COMMENT '关联的限免记录ID',
  `previous_price` decimal(10,2) DEFAULT NULL COMMENT '上一次价格',
  `data_source` varchar(50) DEFAULT 'AUTO' COMMENT '数据来源: AUTO=自动采集, MANUAL=手动录入',
  `crawl_session_id` varchar(32) DEFAULT NULL COMMENT '采集批次ID',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_date_time` (`appstore_app_id`, `record_date`, `record_time`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_date` (`record_date` DESC),
  KEY `idx_app_date` (`app_id`, `record_date` DESC),
  KEY `idx_promotion` (`promotion_id`),
  KEY `idx_change_type` (`price_change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格历史表';

-- ----------------------------
-- 5. 搜索索引表 (search_index)
-- 支持全文搜索的索引表
-- ----------------------------
DROP TABLE IF EXISTS `search_index`;
CREATE TABLE `search_index` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `app_id` varchar(32) NOT NULL COMMENT '应用ID(关联apps表)',
  `appstore_app_id` varchar(20) NOT NULL COMMENT 'App Store应用ID',
  `app_name` varchar(255) NOT NULL COMMENT '应用名称',
  `app_name_pinyin` varchar(500) DEFAULT NULL COMMENT '应用名称拼音',
  `developer_name` varchar(255) NOT NULL COMMENT '开发商名称',
  `developer_name_pinyin` varchar(500) DEFAULT NULL COMMENT '开发商名称拼音',
  `keywords` text COMMENT '关键词(从描述中提取)',
  `category_names` varchar(500) COMMENT '分类名称(多个用逗号分隔)',
  `description_snippet` text COMMENT '描述摘要',
  `search_weight` int DEFAULT '1' COMMENT '搜索权重',
  `popularity_score` decimal(5,2) DEFAULT '0.00' COMMENT '热度得分',
  `quality_score` decimal(5,2) DEFAULT '0.00' COMMENT '质量得分',
  `search_count` int DEFAULT '0' COMMENT '搜索次数',
  `click_rate` decimal(5,4) DEFAULT '0.0000' COMMENT '点击率',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_appstore_id` (`appstore_app_id`),
  KEY `idx_search_weight` (`search_weight` DESC),
  KEY `idx_popularity` (`popularity_score` DESC),
  FULLTEXT KEY `ft_app_name` (`app_name`, `app_name_pinyin`),
  FULLTEXT KEY `ft_developer` (`developer_name`, `developer_name_pinyin`),
  FULLTEXT KEY `ft_keywords` (`keywords`),
  FULLTEXT KEY `ft_all` (`app_name`, `developer_name`, `keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索索引表';

-- ----------------------------
-- 6. 搜索历史表 (search_history)
-- 记录用户搜索历史
-- ----------------------------
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '用户ID(未登录用户为NULL)',
  `device_id` varchar(64) DEFAULT NULL COMMENT '设备标识符',
  `search_query` varchar(255) NOT NULL COMMENT '搜索关键词',
  `search_query_normalized` varchar(255) DEFAULT NULL COMMENT '标准化搜索词',
  `search_type` varchar(20) DEFAULT 'NORMAL' COMMENT '搜索类型: NORMAL=普通, VOICE=语音',
  `result_count` int DEFAULT '0' COMMENT '搜索结果数量',
  `clicked_app_id` varchar(32) DEFAULT NULL COMMENT '点击的应用ID',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `platform` varchar(50) DEFAULT NULL COMMENT '平台信息',
  `app_version` varchar(20) DEFAULT NULL COMMENT '应用版本',
  `searched_at` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_search_query` (`search_query`),
  KEY `idx_searched_at` (`searched_at` DESC),
  KEY `idx_result_count` (`result_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- ============================================================
-- 索引优化说明
-- ============================================================

-- apps表索引说明:
-- uk_app_id: 唯一索引，保证App Store ID唯一性
-- idx_category_price: 组合索引，优化按分类和价格查询
-- idx_developer: 开发商查询索引
-- idx_rating: 评分排序索引
-- idx_updated: 更新时间排序索引
-- idx_status_category: 组合索引，优化状态和分类联合查询

-- free_promotions表索引说明:
-- uk_app_promotion: 唯一索引，防止同一应用同时间重复推广记录
-- idx_app_id: 应用ID查询索引
-- idx_start_time: 开始时间排序索引
-- idx_status_start: 组合索引，优化状态和时间联合查询
-- idx_featured_hot: 组合索引，优化推荐和热门排序
-- idx_discovery: 发现时间排序索引

-- categories表索引说明:
-- uk_category_id: 唯一索引，保证分类ID唯一性
-- idx_parent: 父分类查询索引
-- idx_type_order: 组合索引，优化类型和排序联合查询
-- idx_active: 启用状态查询索引

-- price_history表索引说明:
-- uk_app_date_time: 唯一索引，防止同一时间重复价格记录
-- idx_app_id: 应用ID查询索引
-- idx_date: 日期排序索引
-- idx_app_date: 组合索引，优化应用价格历史查询
-- idx_promotion: 推广关联查询索引
-- idx_change_type: 价格变化类型查询索引

-- search_index表索引说明:
-- uk_app_id: 唯一索引，每个应用一条搜索索引记录
-- idx_appstore_id: App Store ID查询索引
-- idx_search_weight: 搜索权重排序索引
-- idx_popularity: 热度排序索引
-- ft_app_name: 应用名称全文索引
-- ft_developer: 开发商全文索引
-- ft_keywords: 关键词全文索引
-- ft_all: 综合全文索引

-- search_history表索引说明:
-- idx_user_id: 用户ID查询索引
-- idx_device_id: 设备ID查询索引
-- idx_search_query: 搜索词查询索引
-- idx_searched_at: 搜索时间排序索引
-- idx_result_count: 结果数量排序索引

-- ============================================================
-- 性能优化建议
-- ============================================================

-- 1. 分区表优化（适用于大数据量场景）
-- 价格历史表可按月分区:
-- ALTER TABLE price_history PARTITION BY RANGE (TO_DAYS(record_date)) (
--   PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
--   PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
--   PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 2. 查询优化
-- 使用EXPLAIN分析慢查询
-- 适时添加覆盖索引
-- 避免SELECT *，只查询需要的字段

-- 3. 维护建议
-- 定期分析表: ANALYZE TABLE table_name;
-- 定期优化表: OPTIMIZE TABLE table_name;
-- 监控慢查询日志

-- ============================================================
-- 版本历史
-- ============================================================
-- v1.0.0 2024-09-26 初始版本，创建基础表结构