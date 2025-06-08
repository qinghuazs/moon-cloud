package com.moon.cloud.threadpool.endpoint.dto;

import lombok.Data;

/**
 * 线程池信息DTO
 */
@Data
public class ThreadPoolInfoDTO {
    
    /**
     * 线程池名称
     */
    private String poolName;
    
    /**
     * 核心线程数
     */
    private int corePoolSize;
    
    /**
     * 最大线程数
     */
    private int maximumPoolSize;
    
    /**
     * 当前活跃线程数
     */
    private int activeCount;
    
    /**
     * 当前线程池大小
     */
    private int poolSize;
    
    /**
     * 历史最大线程池大小
     */
    private int largestPoolSize;
    
    /**
     * 总任务数
     */
    private long taskCount;
    
    /**
     * 已完成任务数
     */
    private long completedTaskCount;
    
    /**
     * 队列中等待的任务数
     */
    private int queueSize;
    
    /**
     * 队列剩余容量
     */
    private int queueRemainingCapacity;
    
    /**
     * 是否已关闭
     */
    private boolean isShutdown;
    
    /**
     * 是否已终止
     */
    private boolean isTerminated;
    
    /**
     * 是否正在终止
     */
    private boolean isTerminating;
    
    /**
     * 线程池使用率
     */
    private String utilizationRate;
    
    /**
     * 队列使用率
     */
    private String queueUtilizationRate;
    
    /**
     * 线程池状态
     */
    private String status;
    
    /**
     * 获取线程池状态描述
     * @return 状态描述
     */
    public String getStatus() {
        if (isTerminated) {
            return "TERMINATED";
        } else if (isTerminating) {
            return "TERMINATING";
        } else if (isShutdown) {
            return "SHUTDOWN";
        } else {
            return "RUNNING";
        }
    }
}