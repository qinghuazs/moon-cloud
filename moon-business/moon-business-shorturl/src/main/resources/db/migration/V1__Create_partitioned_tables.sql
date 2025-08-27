-- 创建分区表的初始化脚本
-- 注意：此脚本仅适用于MySQL数据库

-- 创建URL映射分区表（按月分区）
CREATE TABLE IF NOT EXISTS url_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,
    short_url VARCHAR(20) NOT NULL,
    original_url TEXT NOT NULL,
    url_hash VARCHAR(64) NOT NULL,
    user_id VARCHAR(50),
    click_count BIGINT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    title VARCHAR(200),
    description TEXT,
    is_custom BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id, created_at),
    UNIQUE KEY uk_short_url (short_url),
    KEY idx_url_hash (url_hash),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_created_at (created_at),
    KEY idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    PARTITION p202403 VALUES LESS THAN (202404),
    PARTITION p202404 VALUES LESS THAN (202405),
    PARTITION p202405 VALUES LESS THAN (202406),
    PARTITION p202406 VALUES LESS THAN (202407),
    PARTITION p202407 VALUES LESS THAN (202408),
    PARTITION p202408 VALUES LESS THAN (202409),
    PARTITION p202409 VALUES LESS THAN (202410),
    PARTITION p202410 VALUES LESS THAN (202411),
    PARTITION p202411 VALUES LESS THAN (202412),
    PARTITION p202412 VALUES LESS THAN (202501),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 创建访问日志分区表（按天分区）
CREATE TABLE IF NOT EXISTS url_access_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    short_url VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referer TEXT,
    access_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    country VARCHAR(50),
    region VARCHAR(50),
    city VARCHAR(50),
    device_type VARCHAR(20),
    browser VARCHAR(50),
    operating_system VARCHAR(50),
    PRIMARY KEY (id, access_time),
    KEY idx_short_url (short_url),
    KEY idx_access_time (access_time),
    KEY idx_ip_address (ip_address),
    KEY idx_device_type (device_type),
    KEY idx_browser (browser)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(access_time)) (
    PARTITION p20240101 VALUES LESS THAN (TO_DAYS('2024-01-02')),
    PARTITION p20240102 VALUES LESS THAN (TO_DAYS('2024-01-03')),
    PARTITION p20240103 VALUES LESS THAN (TO_DAYS('2024-01-04')),
    PARTITION p20240104 VALUES LESS THAN (TO_DAYS('2024-01-05')),
    PARTITION p20240105 VALUES LESS THAN (TO_DAYS('2024-01-06')),
    PARTITION p20240106 VALUES LESS THAN (TO_DAYS('2024-01-07')),
    PARTITION p20240107 VALUES LESS THAN (TO_DAYS('2024-01-08')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 创建分区维护存储过程
DELIMITER //

-- 创建URL映射表分区的存储过程
CREATE PROCEDURE IF NOT EXISTS CreateUrlMappingPartition(IN partition_date DATE)
BEGIN
    DECLARE partition_name VARCHAR(20);
    DECLARE partition_value INT;
    DECLARE next_month DATE;
    DECLARE sql_stmt TEXT;
    
    SET next_month = DATE_ADD(partition_date, INTERVAL 1 MONTH);
    SET partition_name = CONCAT('p', DATE_FORMAT(partition_date, '%Y%m'));
    SET partition_value = YEAR(next_month) * 100 + MONTH(next_month);
    
    SET sql_stmt = CONCAT(
        'ALTER TABLE url_mapping ADD PARTITION (',
        'PARTITION ', partition_name, ' VALUES LESS THAN (', partition_value, '))'
    );
    
    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //

-- 创建访问日志表分区的存储过程
CREATE PROCEDURE IF NOT EXISTS CreateAccessLogPartition(IN partition_date DATE)
BEGIN
    DECLARE partition_name VARCHAR(20);
    DECLARE next_day DATE;
    DECLARE sql_stmt TEXT;
    
    SET next_day = DATE_ADD(partition_date, INTERVAL 1 DAY);
    SET partition_name = CONCAT('p', DATE_FORMAT(partition_date, '%Y%m%d'));
    
    SET sql_stmt = CONCAT(
        'ALTER TABLE url_access_log ADD PARTITION (',
        'PARTITION ', partition_name, ' VALUES LESS THAN (TO_DAYS(''', next_day, ''')))'
    );
    
    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //

-- 删除过期分区的存储过程
CREATE PROCEDURE IF NOT EXISTS DropExpiredPartitions(IN table_name VARCHAR(64), IN retention_days INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE partition_name VARCHAR(64);
    DECLARE partition_date DATE;
    DECLARE cutoff_date DATE;
    DECLARE sql_stmt TEXT;
    
    DECLARE partition_cursor CURSOR FOR
        SELECT PARTITION_NAME
        FROM INFORMATION_SCHEMA.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = table_name
        AND PARTITION_NAME IS NOT NULL
        AND PARTITION_NAME != 'p_future';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    SET cutoff_date = DATE_SUB(CURDATE(), INTERVAL retention_days DAY);
    
    OPEN partition_cursor;
    
    read_loop: LOOP
        FETCH partition_cursor INTO partition_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 解析分区日期
        IF table_name = 'url_mapping' AND LENGTH(partition_name) = 7 THEN
            SET partition_date = STR_TO_DATE(CONCAT(SUBSTRING(partition_name, 2), '01'), '%Y%m%d');
        ELSEIF table_name = 'url_access_log' AND LENGTH(partition_name) = 9 THEN
            SET partition_date = STR_TO_DATE(SUBSTRING(partition_name, 2), '%Y%m%d');
        ELSE
            ITERATE read_loop;
        END IF;
        
        -- 如果分区日期早于截止日期，则删除分区
        IF partition_date < cutoff_date THEN
            SET sql_stmt = CONCAT('ALTER TABLE ', table_name, ' DROP PARTITION ', partition_name);
            SET @sql = sql_stmt;
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;
        
    END LOOP;
    
    CLOSE partition_cursor;
END //

DELIMITER ;

-- 创建分区维护事件（每天凌晨2点执行）
-- 注意：需要确保MySQL的事件调度器已启用 (SET GLOBAL event_scheduler = ON;)

-- 删除已存在的事件
DROP EVENT IF EXISTS partition_maintenance;

-- 创建分区维护事件
CREATE EVENT IF NOT EXISTS partition_maintenance
ON SCHEDULE EVERY 1 DAY
STARTS TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '02:00:00')
DO
BEGIN
    -- 为URL映射表创建下个月的分区
    CALL CreateUrlMappingPartition(DATE_ADD(CURDATE(), INTERVAL 1 MONTH));
    
    -- 为访问日志表创建未来7天的分区
    CALL CreateAccessLogPartition(DATE_ADD(CURDATE(), INTERVAL 1 DAY));
    CALL CreateAccessLogPartition(DATE_ADD(CURDATE(), INTERVAL 2 DAY));
    CALL CreateAccessLogPartition(DATE_ADD(CURDATE(), INTERVAL 3 DAY));
    CALL CreateAccessLogPartition(DATE_ADD(CURDATE(), INTERVAL 4 DAY));
    CALL CreateAccessLogPartition(DATE_ADD(CURDATE(), INTERVAL 5 DAY));
    CALL CreateAccessLogPartition(DATE_ADD(CURDATE(), INTERVAL 6 DAY));
    CALL CreateAccessLogPartition(DATE_ADD(CURDATE(), INTERVAL 7 DAY));
    
    -- 清理90天前的分区
    CALL DropExpiredPartitions('url_mapping', 90);
    CALL DropExpiredPartitions('url_access_log', 90);
END;

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;