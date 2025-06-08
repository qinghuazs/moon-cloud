package com.moon.cloud.threadpool.endpoint;

import com.moon.cloud.threadpool.endpoint.dto.ThreadPoolInfoDTO;
import com.moon.cloud.threadpool.registry.ThreadPoolRegistry;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Component
@Endpoint(id = "threadpools")
public class MoonThreadPoolEndpoint {

    /**
     * 获取所有的线程池信息
     * @return
     */
    @ReadOperation
    public Map<String, Object> threadPools() {
        try {
            Set<String> poolNames = ThreadPoolRegistry.getAllPoolNames();

            List<ThreadPoolInfoDTO> poolInfos = poolNames.stream()
                    .map(poolName -> {
                        ThreadPoolExecutor executor = ThreadPoolRegistry.getExecutor(poolName);
                        return executor != null ? buildThreadPoolInfoDTO(poolName, executor) : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("total", poolInfos.size());
            result.put("pools", poolInfos);

            return result;
        } catch (Exception e) {
            log.error("获取线程池列表失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("msg", "获取线程池列表失败");
            result.put("error", e.getMessage());
            return null;
        }
    }

    /**
     * 构建线程池信息DTO
     * @param poolName 线程池名称
     * @param executor 线程池执行器
     * @return 线程池信息DTO
     */
    private ThreadPoolInfoDTO buildThreadPoolInfoDTO(String poolName, ThreadPoolExecutor executor) {
        ThreadPoolInfoDTO info = new ThreadPoolInfoDTO();
        info.setPoolName(poolName);
        info.setCorePoolSize(executor.getCorePoolSize());
        info.setMaximumPoolSize(executor.getMaximumPoolSize());
        info.setActiveCount(executor.getActiveCount());
        info.setPoolSize(executor.getPoolSize());
        info.setLargestPoolSize(executor.getLargestPoolSize());
        info.setTaskCount(executor.getTaskCount());
        info.setCompletedTaskCount(executor.getCompletedTaskCount());
        info.setQueueSize(executor.getQueue().size());
        info.setQueueRemainingCapacity(executor.getQueue().remainingCapacity());
        info.setShutdown(executor.isShutdown());
        info.setTerminated(executor.isTerminated());
        info.setTerminating(executor.isTerminating());

        // 计算线程池使用率
        double utilizationRate = executor.getMaximumPoolSize() > 0 ?
                (double) executor.getActiveCount() / executor.getMaximumPoolSize() * 100 : 0;
        info.setUtilizationRate(String.format("%.2f%%", utilizationRate));

        // 计算队列使用率
        int totalQueueCapacity = executor.getQueue().size() + executor.getQueue().remainingCapacity();
        double queueUtilizationRate = totalQueueCapacity > 0 ?
                (double) executor.getQueue().size() / totalQueueCapacity * 100 : 0;
        info.setQueueUtilizationRate(String.format("%.2f%%", queueUtilizationRate));

        return info;
    }

    /**
     * 获取线程池信息
     * @param poolName
     * @return
     */
    @ReadOperation
    public Map<String, Object> threadPool(@Selector @Nullable String poolName) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (poolName == null) {
                result.put("msg", "线程池名称不能为空");
                return result;
            }
            
            ThreadPoolExecutor executor = ThreadPoolRegistry.getExecutor(poolName);
            if (executor == null) {
                result.put("msg", String.format("线程池[%s]不存在", poolName));
                return result;
            }

            ThreadPoolInfoDTO info = buildThreadPoolInfoDTO(poolName, executor);
            result.put("info", info);
            return result;
        } catch (Exception e) {
            log.error("获取线程池详情失败: {}", poolName, e);
            result.put("msg", String.format("获取线程池[%s]详情失败", poolName));
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * 调整线程池参数
     * @param poolName
     * @param corePoolSize
     * @param maximumPoolSize
     * @return
     */
    @WriteOperation
    public Map<String, Object> adjustThreadPool(
            @Selector @Nullable  String poolName,
            @Selector @Nullable  Integer corePoolSize,
            @Selector @Nullable  Integer maximumPoolSize) {
        Map<String, Object> result = new HashMap<>();
        // 动态调整线程池参数
        try {
            if (poolName == null || corePoolSize == null || maximumPoolSize == null) {
                result.put("msg", "参数不能为空");
                return result;
            }
            
            ThreadPoolExecutor executor = ThreadPoolRegistry.getExecutor(poolName);
            if (executor == null) {
                result.put("msg", String.format("线程池[%s]不存在", poolName));
                return result;
            }

            if (corePoolSize <= 0) {
                result.put("msg", "核心线程数必须大于0");
                return result;
            }

            if (corePoolSize > executor.getMaximumPoolSize()) {
                result.put("msg", "核心线程数不能大于最大线程数");
                return result;
            }

            int oldCorePoolSize = executor.getCorePoolSize();
            executor.setCorePoolSize(corePoolSize);

            log.info("线程池 {} 核心线程数已从 {} 调整为 {}", poolName, oldCorePoolSize, corePoolSize);

            int oldMaximumPoolSize = executor.getMaximumPoolSize();
            executor.setMaximumPoolSize(maximumPoolSize);
            log.info("线程池 {} 最大线程数已从 {} 调整为 {}", poolName, oldMaximumPoolSize, maximumPoolSize);
           
            result.put("poolName", poolName);
            result.put("oldCorePoolSize", oldCorePoolSize);
            result.put("newCorePoolSize", corePoolSize);
            result.put("oldMaximumPoolSize", oldMaximumPoolSize);
            result.put("newMaximumPoolSize", maximumPoolSize);
            result.put("msg", "线程核心线程数和最大线程数调整成功！");
            return result;
        } catch (Exception e) {
            log.error("调整线程池{}核心线程数失败: ", poolName, e);
            result.put("msg", "调整核心线程数失败");
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * 关闭线程
     * @param poolName
     * @return
     */
    @DeleteOperation
    public Map<String, Object> shutdownThreadPool(@Selector @Nullable String poolName) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (poolName == null) {
                result.put("msg", "线程池名称不能为空");
                return result;
            }
            
            ThreadPoolExecutor executor = ThreadPoolRegistry.getExecutor(poolName);
            if (executor == null) {
                result.put("msg", String.format("线程池[%s]不存在", poolName));
                return result;
            }
            
            if (executor.isShutdown()) {
                result.put("msg", String.format("线程池[%s]已经关闭", poolName));
                return result;
            }
            
            ThreadPoolRegistry.shutdown(executor);

            result.put("poolName", poolName);
            log.info("线程池 {} 已开始关闭", poolName);
            
            
            return result;
        } catch (Exception e) {
            result.put("msg", "关闭线程池失败");
            result.put("error", e.getMessage());
            return result;
        }
    }
}
