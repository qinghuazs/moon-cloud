package com.moon.cloud.drift.bottle.task;

import com.moon.cloud.drift.bottle.service.DriftBottleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 漂流瓶清理定时任务
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Component
public class DriftBottleCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(DriftBottleCleanupTask.class);

    @Autowired
    private DriftBottleService driftBottleService;

    /**
     * 清理过期漂流瓶
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredBottles() {
        logger.info("开始执行漂流瓶清理任务");
        
        try {
            driftBottleService.cleanupExpiredBottles();
            logger.info("漂流瓶清理任务执行完成");
        } catch (Exception e) {
            logger.error("漂流瓶清理任务执行失败", e);
        }
    }

    /**
     * 系统状态检查
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void systemHealthCheck() {
        logger.debug("执行系统健康检查");
        
        try {
            // 这里可以添加系统健康检查逻辑
            // 比如检查数据库连接、内存使用情况等
            
            // 记录系统运行状态
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            logger.debug("系统内存使用情况 - 总内存: {}MB, 已用内存: {}MB, 空闲内存: {}MB", 
                        totalMemory / 1024 / 1024, 
                        usedMemory / 1024 / 1024, 
                        freeMemory / 1024 / 1024);
                        
        } catch (Exception e) {
            logger.error("系统健康检查失败", e);
        }
    }
}