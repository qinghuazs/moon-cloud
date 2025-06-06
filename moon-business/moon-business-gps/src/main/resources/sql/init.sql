-- GPS数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS moon DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE moon;

-- 创建GPS数据表
CREATE TABLE IF NOT EXISTS gps_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    vehicle_id VARCHAR(50) NOT NULL COMMENT '车辆ID',
    longitude DECIMAL(10, 7) NOT NULL COMMENT '经度',
    latitude DECIMAL(10, 7) NOT NULL COMMENT '纬度',
    speed DECIMAL(5, 2) DEFAULT 0.00 COMMENT '速度(km/h)',
    direction DECIMAL(5, 2) DEFAULT 0.00 COMMENT '方向角(0-360度)',
    altitude DECIMAL(8, 2) DEFAULT 0.00 COMMENT '海拔高度(米)',
    gps_time DATETIME NOT NULL COMMENT 'GPS时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_gps_time (gps_time),
    INDEX idx_vehicle_gps_time (vehicle_id, gps_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GPS数据表';

-- 创建GPS事件表（用于记录路线偏离、驶入驶出等事件）
CREATE TABLE IF NOT EXISTS gps_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    vehicle_id VARCHAR(50) NOT NULL COMMENT '车辆ID',
    event_type VARCHAR(20) NOT NULL COMMENT '事件类型(ROUTE_DEVIATION:路线偏离, AREA_ENTER:驶入区域, AREA_EXIT:驶出区域)',
    longitude DECIMAL(10, 7) NOT NULL COMMENT '经度',
    latitude DECIMAL(10, 7) NOT NULL COMMENT '纬度',
    event_time DATETIME NOT NULL COMMENT '事件时间',
    description TEXT COMMENT '事件描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_event_type (event_type),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GPS事件表';

-- 创建车辆信息表
CREATE TABLE IF NOT EXISTS vehicle_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    vehicle_id VARCHAR(50) NOT NULL UNIQUE COMMENT '车辆ID',
    vehicle_name VARCHAR(100) COMMENT '车辆名称',
    vehicle_type VARCHAR(50) COMMENT '车辆类型',
    driver_name VARCHAR(50) COMMENT '司机姓名',
    driver_phone VARCHAR(20) COMMENT '司机电话',
    status TINYINT DEFAULT 1 COMMENT '状态(0:停用, 1:启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆信息表';

-- 插入测试车辆数据
INSERT INTO vehicle_info (vehicle_id, vehicle_name, vehicle_type, driver_name, driver_phone) VALUES
('V0001', '京A12345', '货车', '张三', '13800138001'),
('V0002', '京A12346', '客车', '李四', '13800138002'),
('V0003', '京A12347', '货车', '王五', '13800138003'),
('V0004', '京A12348', '客车', '赵六', '13800138004'),
('V0005', '京A12349', '货车', '钱七', '13800138005');

-- 为其他车辆生成数据（V0006到V0100）
INSERT INTO vehicle_info (vehicle_id, vehicle_name, vehicle_type, driver_name, driver_phone)
SELECT 
    CONCAT('V', LPAD(n, 4, '0')) as vehicle_id,
    CONCAT('京A', 12349 + n - 5) as vehicle_name,
    CASE WHEN n % 2 = 0 THEN '货车' ELSE '客车' END as vehicle_type,
    CONCAT('司机', n) as driver_name,
    CONCAT('138001380', LPAD(n % 100, 2, '0')) as driver_phone
FROM (
    SELECT 6 + (a.N + b.N * 10) as n
    FROM 
        (SELECT 0 as N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
        (SELECT 0 as N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
    WHERE 6 + (a.N + b.N * 10) <= 100
) numbers;