package com.moon.cloud.business.gps.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * GPS数据分表管理服务
 *
 * @author moon-cloud
 * @since 2024-01-01
 */
@Slf4j
@Service
public class GpsPartitionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String GPS_DATA_TABLE_PREFIX = "gps_data_";

    /**
     * 创建指定日期的GPS数据分表
     *
     * @param date 日期
     * @return 是否创建成功
     */
    public boolean createPartitionTable(LocalDate date) {
        String tableName = GPS_DATA_TABLE_PREFIX + date.format(DATE_FORMATTER);
        
        try {
            // 检查表是否已存在
            if (isTableExists(tableName)) {
                log.info("分表已存在: {}", tableName);
                return true;
            }
            
            // 调用存储过程创建分表
            jdbcTemplate.update("CALL CreateGpsDataPartition(?)", date);
            
            log.info("成功创建GPS数据分表: {}", tableName);
            return true;
            
        } catch (Exception e) {
            log.error("创建GPS数据分表失败: {}", tableName, e);
            return false;
        }
    }

    /**
     * 批量创建未来几天的分表
     *
     * @param daysAhead 提前创建的天数
     */
    public void createFuturePartitions(int daysAhead) {
        try {
            jdbcTemplate.update("CALL CreateFuturePartitions(?)", daysAhead);
            log.info("成功创建未来{}天的GPS数据分表", daysAhead);
        } catch (Exception e) {
            log.error("批量创建GPS数据分表失败", e);
        }
    }

    /**
     * 清理旧的分表
     *
     * @param daysToKeep 保留的天数
     */
    public void cleanOldPartitions(int daysToKeep) {
        try {
            jdbcTemplate.update("CALL CleanOldPartitions(?)", daysToKeep);
            log.info("成功清理{}天前的GPS数据分表", daysToKeep);
        } catch (Exception e) {
            log.error("清理旧GPS数据分表失败", e);
        }
    }

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return 是否存在
     */
    private boolean isTableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("检查表是否存在时发生异常: {}", tableName, e);
            return false;
        }
    }

    /**
     * 获取所有GPS数据分表信息
     *
     * @return 分表信息列表
     */
    public List<Map<String, Object>> getPartitionInfo() {
        try {
            String sql = "SELECT table_name, partition_date, create_time, status " +
                        "FROM gps_partition_log " +
                        "ORDER BY partition_date DESC";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("获取GPS数据分表信息失败", e);
            return List.of();
        }
    }

    /**
     * 获取指定日期范围内的分表统计信息
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    public Map<String, Object> getPartitionStats(LocalDate startDate, LocalDate endDate) {
        try {
            // 获取分表数量
            String countSql = "SELECT COUNT(*) as table_count " +
                             "FROM gps_partition_log " +
                             "WHERE partition_date BETWEEN ? AND ? AND status = 'SUCCESS'";
            Map<String, Object> stats = jdbcTemplate.queryForMap(countSql, startDate, endDate);
            
            // 获取总数据量（需要动态查询各个分表）
            long totalRecords = 0;
            String tableListSql = "SELECT table_name FROM gps_partition_log " +
                                 "WHERE partition_date BETWEEN ? AND ? AND status = 'SUCCESS'";
            List<String> tableNames = jdbcTemplate.queryForList(tableListSql, String.class, startDate, endDate);
            
            for (String tableName : tableNames) {
                try {
                    String recordCountSql = "SELECT COUNT(*) FROM " + tableName;
                    Integer count = jdbcTemplate.queryForObject(recordCountSql, Integer.class);
                    totalRecords += (count != null ? count : 0);
                } catch (Exception e) {
                    log.warn("查询分表记录数失败: {}", tableName);
                }
            }
            
            stats.put("total_records", totalRecords);
            stats.put("start_date", startDate);
            stats.put("end_date", endDate);
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取GPS数据分表统计信息失败", e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * 定时任务：每天凌晨2点创建未来7天的分表
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledCreatePartitions() {
        log.info("开始执行定时创建GPS数据分表任务");
        createFuturePartitions(7);
    }

    /**
     * 定时任务：每周日凌晨3点清理30天前的分表
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void scheduledCleanPartitions() {
        log.info("开始执行定时清理GPS数据分表任务");
        cleanOldPartitions(30);
    }

    /**
     * 手动触发创建当天分表（用于应急情况）
     */
    public boolean createTodayPartition() {
        return createPartitionTable(LocalDate.now());
    }

    /**
     * 手动触发创建明天分表（用于提前准备）
     */
    public boolean createTomorrowPartition() {
        return createPartitionTable(LocalDate.now().plusDays(1));
    }
}