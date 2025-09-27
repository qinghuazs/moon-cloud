-- 创建APP价格历史记录表
CREATE TABLE IF NOT EXISTS `app_price_history` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `app_id` varchar(100) NOT NULL COMMENT 'App Store应用ID',
  `bundle_id` varchar(255) DEFAULT NULL COMMENT '应用Bundle ID',
  `app_name` varchar(255) DEFAULT NULL COMMENT '应用名称',
  `old_price` decimal(10,2) DEFAULT NULL COMMENT '原价格',
  `new_price` decimal(10,2) DEFAULT NULL COMMENT '新价格',
  `price_change` decimal(10,2) DEFAULT NULL COMMENT '价格变化量',
  `change_percent` decimal(10,2) DEFAULT NULL COMMENT '价格变化百分比',
  `currency` varchar(10) DEFAULT 'CNY' COMMENT '货币类型',
  `price_type` varchar(20) DEFAULT NULL COMMENT '价格类型：NORMAL-正常价格, PROMOTION-促销价格, FREE-限免',
  `change_type` varchar(20) NOT NULL COMMENT '变化类型：INCREASE-涨价, DECREASE-降价, FREE-限免, RESTORE-恢复原价, INITIAL-初始记录',
  `is_free` tinyint(1) DEFAULT '0' COMMENT '是否为限免：0-否, 1-是',
  `old_is_free` tinyint(1) DEFAULT '0' COMMENT '原始是否免费状态',
  `version` varchar(50) DEFAULT NULL COMMENT '版本号',
  `category_id` varchar(50) DEFAULT NULL COMMENT '分类ID',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
  `developer_name` varchar(255) DEFAULT NULL COMMENT '开发者名称',
  `change_time` datetime NOT NULL COMMENT '价格变化时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `source` varchar(20) DEFAULT 'CRAWLER' COMMENT '数据来源：CRAWLER-爬虫, MANUAL-手动, API-接口',
  `is_notified` tinyint(1) DEFAULT '0' COMMENT '是否已通知：0-未通知, 1-已通知',
  `notified_at` datetime DEFAULT NULL COMMENT '通知时间',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_bundle_id` (`bundle_id`),
  KEY `idx_change_time` (`change_time`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_is_free` (`is_free`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_app_change` (`app_id`,`change_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP价格历史记录表';

-- 添加索引优化查询性能
ALTER TABLE `app_price_history` ADD INDEX `idx_price_drop` (`change_type`, `change_time`, `new_price`);
ALTER TABLE `app_price_history` ADD INDEX `idx_developer` (`developer_name`, `change_time`);

-- 创建触发器：自动计算价格变化量和百分比
DELIMITER $$
CREATE TRIGGER `calc_price_change` BEFORE INSERT ON `app_price_history`
FOR EACH ROW
BEGIN
    -- 计算价格变化量
    IF NEW.old_price IS NOT NULL AND NEW.new_price IS NOT NULL THEN
        SET NEW.price_change = NEW.new_price - NEW.old_price;

        -- 计算价格变化百分比
        IF NEW.old_price > 0 THEN
            SET NEW.change_percent = (NEW.price_change / NEW.old_price) * 100;
        END IF;
    END IF;

    -- 自动设置变化类型
    IF NEW.change_type IS NULL THEN
        IF NEW.new_price = 0 AND NEW.old_price > 0 THEN
            SET NEW.change_type = 'FREE';
            SET NEW.is_free = 1;
        ELSEIF NEW.old_price IS NULL THEN
            SET NEW.change_type = 'INITIAL';
        ELSEIF NEW.new_price > NEW.old_price THEN
            SET NEW.change_type = 'INCREASE';
        ELSEIF NEW.new_price < NEW.old_price THEN
            SET NEW.change_type = 'DECREASE';
        END IF;
    END IF;
END$$
DELIMITER ;

-- 创建视图：最新价格视图
CREATE OR REPLACE VIEW `v_app_latest_price` AS
SELECT
    a.app_id,
    a.bundle_id,
    a.app_name,
    a.new_price AS current_price,
    a.currency,
    a.is_free,
    a.change_type AS latest_change_type,
    a.change_time AS latest_change_time,
    a.version,
    a.developer_name,
    (SELECT MIN(new_price) FROM app_price_history WHERE app_id = a.app_id AND new_price > 0) AS historical_low,
    (SELECT MAX(new_price) FROM app_price_history WHERE app_id = a.app_id) AS historical_high
FROM app_price_history a
INNER JOIN (
    SELECT app_id, MAX(change_time) AS max_time
    FROM app_price_history
    GROUP BY app_id
) b ON a.app_id = b.app_id AND a.change_time = b.max_time;

-- 创建存储过程：获取APP价格趋势
DELIMITER $$
CREATE PROCEDURE `sp_get_price_trend`(
    IN p_app_id VARCHAR(100),
    IN p_days INT
)
BEGIN
    SELECT
        DATE(change_time) AS date,
        MIN(new_price) AS min_price,
        MAX(new_price) AS max_price,
        AVG(new_price) AS avg_price,
        COUNT(*) AS change_count
    FROM app_price_history
    WHERE app_id = p_app_id
        AND change_time >= DATE_SUB(NOW(), INTERVAL p_days DAY)
    GROUP BY DATE(change_time)
    ORDER BY date DESC;
END$$
DELIMITER ;