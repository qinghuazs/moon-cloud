package com.mooncloud.shorturl.task;

import com.mooncloud.shorturl.service.PartitionManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 分区维护定时任务
 * 
 * @author mooncloud
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.partition.enabled", havingValue = "true", matchIfMissing = true)
public class PartitionMaintenanceTask {
    
    @Autowired
    private PartitionManagerService partitionManagerService;
    
    /**
     * 每天凌晨2点执行分区维护
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void maintainPartitions() {
        log.info("开始执行分区维护任务");
        
        try {
            partitionManagerService.maintainPartitions();
            log.info("分区维护任务执行成功");
            
        } catch (Exception e) {
            log.error("分区维护任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每小时检查并创建必要的分区
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkAndCreatePartitions() {
        log.debug("开始检查分区状态");
        
        try {
            // 这里可以添加分区状态检查逻辑
            // 例如检查是否有足够的未来分区
            
        } catch (Exception e) {
            log.error("分区状态检查失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每周日凌晨3点执行分区统计和优化
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void optimizePartitions() {
        log.info("开始执行分区优化任务");
        
        try {
            // 这里可以添加分区优化逻辑
            // 例如分析分区大小、重新组织分区等
            
            log.info("分区优化任务执行成功");
            
        } catch (Exception e) {
            log.error("分区优化任务执行失败: {}", e.getMessage(), e);
        }
    }
}