package com.mooncloud.shorturl.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 分区管理服务
 * 负责数据库表分区的创建、维护和清理
 * 
 * @author mooncloud
 */
@Service
@Slf4j
public class PartitionManagerService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Value("${app.partition.enabled:true}")
    private boolean partitionEnabled;
    
    @Value("${app.partition.retention-days:90}")
    private int retentionDays;
    
    @Value("${app.partition.advance-days:7}")
    private int advanceDays;
    
    private static final String URL_MAPPING_TABLE = "url_mapping";
    private static final String URL_ACCESS_LOG_TABLE = "url_access_log";
    
    /**
     * 应用启动后初始化分区
     */
    @PostConstruct
    public void initializePartitions() {
        if (!partitionEnabled) {
            log.info("分区功能已禁用");
            return;
        }
        
        try {
            // 检查数据库是否支持分区
            if (!isDatabaseSupportPartition()) {
                log.warn("当前数据库不支持分区功能");
                return;
            }
            
            // 创建分区表
            createPartitionedTables();
            
            // 创建当前和未来的分区
            createInitialPartitions();
            
            log.info("分区初始化完成");
            
        } catch (Exception e) {
            log.error("分区初始化失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 检查数据库是否支持分区
     */
    private boolean isDatabaseSupportPartition() {
        try {
            String databaseProductName = jdbcTemplate.getDataSource()
                .getConnection().getMetaData().getDatabaseProductName();
            
            // 目前只支持MySQL的分区功能
            return "MySQL".equalsIgnoreCase(databaseProductName);
            
        } catch (Exception e) {
            log.error("检查数据库类型失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 创建分区表
     */
    @Transactional
    public void createPartitionedTables() {
        try {
            // 创建URL映射分区表（按月分区）
            createUrlMappingPartitionTable();
            
            // 创建访问日志分区表（按天分区）
            createAccessLogPartitionTable();
            
        } catch (Exception e) {
            log.error("创建分区表失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 创建URL映射分区表
     */
    private void createUrlMappingPartitionTable() {
        String sql = """
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
                PARTITION p_future VALUES LESS THAN MAXVALUE
            )
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("URL映射分区表创建成功");
        } catch (Exception e) {
            log.error("创建URL映射分区表失败: {}", e.getMessage());
            // 如果分区表创建失败，尝试创建普通表
            createNormalUrlMappingTable();
        }
    }
    
    /**
     * 创建访问日志分区表
     */
    private void createAccessLogPartitionTable() {
        String sql = """
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
                PARTITION p_future VALUES LESS THAN MAXVALUE
            )
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("访问日志分区表创建成功");
        } catch (Exception e) {
            log.error("创建访问日志分区表失败: {}", e.getMessage());
            // 如果分区表创建失败，尝试创建普通表
            createNormalAccessLogTable();
        }
    }
    
    /**
     * 创建普通URL映射表（不分区）
     */
    private void createNormalUrlMappingTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS url_mapping (
                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                short_url VARCHAR(20) NOT NULL UNIQUE,
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
                KEY idx_url_hash (url_hash),
                KEY idx_user_id (user_id),
                KEY idx_status (status),
                KEY idx_created_at (created_at),
                KEY idx_expires_at (expires_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        jdbcTemplate.execute(sql);
        log.info("普通URL映射表创建成功");
    }
    
    /**
     * 创建普通访问日志表（不分区）
     */
    private void createNormalAccessLogTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS url_access_log (
                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
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
                KEY idx_short_url (short_url),
                KEY idx_access_time (access_time),
                KEY idx_ip_address (ip_address),
                KEY idx_device_type (device_type),
                KEY idx_browser (browser)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;
        
        jdbcTemplate.execute(sql);
        log.info("普通访问日志表创建成功");
    }
    
    /**
     * 创建初始分区
     */
    private void createInitialPartitions() {
        LocalDate today = LocalDate.now();
        
        // 为URL映射表创建未来几个月的分区
        for (int i = 0; i <= advanceDays / 30 + 1; i++) {
            LocalDate date = today.plusMonths(i);
            createUrlMappingPartition(date);
        }
        
        // 为访问日志表创建未来几天的分区
        for (int i = 0; i <= advanceDays; i++) {
            LocalDate date = today.plusDays(i);
            createAccessLogPartition(date);
        }
    }
    
    /**
     * 创建URL映射表分区（按月）
     */
    public void createUrlMappingPartition(LocalDate date) {
        try {
            String partitionName = "p" + date.format(DateTimeFormatter.ofPattern("yyyyMM"));
            LocalDate nextMonth = date.plusMonths(1);
            int partitionValue = nextMonth.getYear() * 100 + nextMonth.getMonthValue();
            
            String sql = String.format(
                "ALTER TABLE %s ADD PARTITION (PARTITION %s VALUES LESS THAN (%d))",
                URL_MAPPING_TABLE, partitionName, partitionValue
            );
            
            jdbcTemplate.execute(sql);
            log.info("URL映射表分区创建成功: {}", partitionName);
            
        } catch (Exception e) {
            if (!e.getMessage().contains("Duplicate partition name")) {
                log.error("创建URL映射表分区失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 创建访问日志表分区（按天）
     */
    public void createAccessLogPartition(LocalDate date) {
        try {
            String partitionName = "p" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate nextDay = date.plusDays(1);
            
            String sql = String.format(
                "ALTER TABLE %s ADD PARTITION (PARTITION %s VALUES LESS THAN (TO_DAYS('%s')))",
                URL_ACCESS_LOG_TABLE, partitionName, nextDay.toString()
            );
            
            jdbcTemplate.execute(sql);
            log.info("访问日志表分区创建成功: {}", partitionName);
            
        } catch (Exception e) {
            if (!e.getMessage().contains("Duplicate partition name")) {
                log.error("创建访问日志表分区失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 定时维护分区（每天凌晨2点执行）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void maintainPartitions() {
        if (!partitionEnabled) {
            return;
        }
        
        try {
            log.info("开始分区维护任务");
            
            // 创建未来的分区
            createFuturePartitions();
            
            // 清理过期的分区
            cleanupExpiredPartitions();
            
            log.info("分区维护任务完成");
            
        } catch (Exception e) {
            log.error("分区维护任务失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 创建未来的分区
     */
    private void createFuturePartitions() {
        LocalDate today = LocalDate.now();
        
        // 为URL映射表创建下个月的分区
        createUrlMappingPartition(today.plusMonths(1));
        
        // 为访问日志表创建未来几天的分区
        for (int i = 1; i <= advanceDays; i++) {
            createAccessLogPartition(today.plusDays(i));
        }
    }
    
    /**
     * 清理过期的分区
     */
    private void cleanupExpiredPartitions() {
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        
        // 清理URL映射表的过期分区
        cleanupUrlMappingPartitions(cutoffDate);
        
        // 清理访问日志表的过期分区
        cleanupAccessLogPartitions(cutoffDate);
    }
    
    /**
     * 清理URL映射表的过期分区
     */
    private void cleanupUrlMappingPartitions(LocalDate cutoffDate) {
        try {
            String sql = "SELECT PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                        "AND PARTITION_NAME IS NOT NULL AND PARTITION_NAME != 'p_future'";
            
            List<String> partitions = jdbcTemplate.queryForList(sql, String.class, URL_MAPPING_TABLE);
            
            for (String partition : partitions) {
                if (partition.startsWith("p") && partition.length() == 7) {
                    try {
                        String dateStr = partition.substring(1) + "01";
                        LocalDate partitionDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                        
                        if (partitionDate.isBefore(cutoffDate)) {
                            dropPartition(URL_MAPPING_TABLE, partition);
                        }
                    } catch (Exception e) {
                        log.warn("解析分区日期失败: {}", partition);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("清理URL映射表分区失败: {}", e.getMessage());
        }
    }
    
    /**
     * 清理访问日志表的过期分区
     */
    private void cleanupAccessLogPartitions(LocalDate cutoffDate) {
        try {
            String sql = "SELECT PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                        "AND PARTITION_NAME IS NOT NULL AND PARTITION_NAME != 'p_future'";
            
            List<String> partitions = jdbcTemplate.queryForList(sql, String.class, URL_ACCESS_LOG_TABLE);
            
            for (String partition : partitions) {
                if (partition.startsWith("p") && partition.length() == 9) {
                    try {
                        String dateStr = partition.substring(1);
                        LocalDate partitionDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                        
                        if (partitionDate.isBefore(cutoffDate)) {
                            dropPartition(URL_ACCESS_LOG_TABLE, partition);
                        }
                    } catch (Exception e) {
                        log.warn("解析分区日期失败: {}", partition);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("清理访问日志表分区失败: {}", e.getMessage());
        }
    }
    
    /**
     * 删除分区
     */
    private void dropPartition(String tableName, String partitionName) {
        try {
            String sql = String.format("ALTER TABLE %s DROP PARTITION %s", tableName, partitionName);
            jdbcTemplate.execute(sql);
            log.info("分区删除成功: {}.{}", tableName, partitionName);
            
        } catch (Exception e) {
            log.error("删除分区失败: {}.{}, 错误: {}", tableName, partitionName, e.getMessage());
        }
    }
    
    /**
     * 获取分区信息
     */
    public List<String> getPartitionInfo(String tableName) {
        try {
            String sql = "SELECT PARTITION_NAME, PARTITION_DESCRIPTION, TABLE_ROWS " +
                        "FROM INFORMATION_SCHEMA.PARTITIONS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                        "AND PARTITION_NAME IS NOT NULL " +
                        "ORDER BY PARTITION_ORDINAL_POSITION";
            
            return jdbcTemplate.queryForList(sql, String.class, tableName);
            
        } catch (Exception e) {
            log.error("获取分区信息失败: {}", e.getMessage());
            return List.of();
        }
    }
}