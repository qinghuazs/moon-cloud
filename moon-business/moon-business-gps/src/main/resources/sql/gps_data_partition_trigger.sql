-- GPS数据分表触发器程序
-- 用于自动创建按天分表，以YYYYMMDD为后缀

DELIMITER //

-- 创建分表的存储过程
CREATE PROCEDURE IF NOT EXISTS CreateGpsDataPartition(IN partition_date DATE)
BEGIN
    DECLARE table_name VARCHAR(50);
    DECLARE table_exists INT DEFAULT 0;
    
    -- 生成表名，格式：gps_data_YYYYMMDD
    SET table_name = CONCAT('gps_data_', DATE_FORMAT(partition_date, '%Y%m%d'));
    
    -- 检查表是否已存在
    SELECT COUNT(*) INTO table_exists 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() 
    AND table_name = table_name;
    
    -- 如果表不存在，则创建
    IF table_exists = 0 THEN
        SET @sql = CONCAT('
            CREATE TABLE ', table_name, ' (
                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT ''主键ID'',
                vehicle_id VARCHAR(50) NOT NULL COMMENT ''车辆ID'',
                longitude DECIMAL(10, 7) NOT NULL COMMENT ''经度'',
                latitude DECIMAL(10, 7) NOT NULL COMMENT ''纬度'',
                speed DECIMAL(5, 2) DEFAULT 0.00 COMMENT ''速度(km/h)'',
                direction DECIMAL(5, 2) DEFAULT 0.00 COMMENT ''方向角(0-360度)'',
                altitude DECIMAL(8, 2) DEFAULT 0.00 COMMENT ''海拔高度(米)'',
                gps_time DATETIME NOT NULL COMMENT ''GPS时间'',
                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
                INDEX idx_vehicle_id (vehicle_id),
                INDEX idx_gps_time (gps_time),
                INDEX idx_vehicle_gps_time (vehicle_id, gps_time)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT=''GPS数据表_', DATE_FORMAT(partition_date, '%Y%m%d'), ''''
        ');
        
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 记录日志
        INSERT INTO gps_partition_log (table_name, partition_date, create_time, status) 
        VALUES (table_name, partition_date, NOW(), 'SUCCESS');
    END IF;
END//

-- 创建分表日志表（用于记录分表创建情况）
CREATE TABLE IF NOT EXISTS gps_partition_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    table_name VARCHAR(50) NOT NULL COMMENT '表名',
    partition_date DATE NOT NULL COMMENT '分区日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态',
    INDEX idx_partition_date (partition_date),
    INDEX idx_table_name (table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GPS分表日志'//

-- 创建触发器：在插入gps_data时自动创建对应日期的分表
CREATE TRIGGER IF NOT EXISTS gps_data_partition_trigger
BEFORE INSERT ON gps_data
FOR EACH ROW
BEGIN
    DECLARE partition_date DATE;
    
    -- 获取GPS时间的日期部分
    SET partition_date = DATE(NEW.gps_time);
    
    -- 调用存储过程创建分表
    CALL CreateGpsDataPartition(partition_date);
END//

-- 创建定时任务存储过程：提前创建未来几天的分表
CREATE PROCEDURE IF NOT EXISTS CreateFuturePartitions(IN days_ahead INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE target_date DATE;
    
    WHILE i <= days_ahead DO
        SET target_date = DATE_ADD(CURDATE(), INTERVAL i DAY);
        CALL CreateGpsDataPartition(target_date);
        SET i = i + 1;
    END WHILE;
END//

-- 创建清理旧分表的存储过程
CREATE PROCEDURE IF NOT EXISTS CleanOldPartitions(IN days_to_keep INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE table_name VARCHAR(50);
    DECLARE partition_date DATE;
    DECLARE cutoff_date DATE;
    
    DECLARE partition_cursor CURSOR FOR 
        SELECT table_name, partition_date 
        FROM gps_partition_log 
        WHERE partition_date < DATE_SUB(CURDATE(), INTERVAL days_to_keep DAY)
        AND status = 'SUCCESS';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    SET cutoff_date = DATE_SUB(CURDATE(), INTERVAL days_to_keep DAY);
    
    OPEN partition_cursor;
    
    read_loop: LOOP
        FETCH partition_cursor INTO table_name, partition_date;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 删除分表
        SET @sql = CONCAT('DROP TABLE IF EXISTS ', table_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 更新日志状态
        UPDATE gps_partition_log 
        SET status = 'DROPPED' 
        WHERE table_name = table_name AND partition_date = partition_date;
        
    END LOOP;
    
    CLOSE partition_cursor;
END//

DELIMITER ;

-- 初始化：创建当天和未来7天的分表
CALL CreateFuturePartitions(7);

-- 使用示例：
-- 1. 手动创建指定日期的分表：
-- CALL CreateGpsDataPartition('2024-01-15');

-- 2. 创建未来30天的分表：
-- CALL CreateFuturePartitions(30);

-- 3. 清理30天前的分表：
-- CALL CleanOldPartitions(30);

-- 4. 查看分表创建日志：
-- SELECT * FROM gps_partition_log ORDER BY partition_date DESC;