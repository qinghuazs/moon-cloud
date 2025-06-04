package com.moon.cloud.concurrent.threadpool;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 线程池死锁检测器
 * 提供全面的死锁检测、分析和预警功能
 */
public class ThreadPoolDeadlockDetector {
    
    private final ThreadMXBean threadMXBean;
    private final ScheduledExecutorService detectorExecutor;
    private final Set<String> monitoredThreadPools;
    private final Map<String, ThreadPoolExecutor> threadPoolMap;
    private volatile boolean isRunning = false;
    
    // 检测配置
    private long detectionInterval = 2000; // 检测间隔（毫秒）
    private int maxWaitingThreadsThreshold = 5; // 等待线程数阈值
    private double highUtilizationThreshold = 0.9; // 高利用率阈值
    
    public ThreadPoolDeadlockDetector() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.detectorExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ThreadPool-DeadlockDetector");
            t.setDaemon(true);
            return t;
        });
        this.monitoredThreadPools = new HashSet<>();
        this.threadPoolMap = new HashMap<>();
    }
    
    /**
     * 添加要监控的线程池
     */
    public void addMonitoredThreadPool(String poolName, ThreadPoolExecutor threadPool) {
        monitoredThreadPools.add(poolName);
        threadPoolMap.put(poolName, threadPool);
        System.out.println("已添加监控线程池: " + poolName);
    }
    
    /**
     * 开始死锁检测
     */
    public void startDetection() {
        if (isRunning) {
            System.out.println("死锁检测器已在运行中");
            return;
        }
        
        isRunning = true;
        System.out.println("启动线程池死锁检测器，检测间隔: " + detectionInterval + "ms");
        
        detectorExecutor.scheduleAtFixedRate(
            this::performDetection,
            0,
            detectionInterval,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * 停止死锁检测
     */
    public void stopDetection() {
        isRunning = false;
        detectorExecutor.shutdown();
        System.out.println("线程池死锁检测器已停止");
    }
    
    /**
     * 执行检测
     */
    private void performDetection() {
        try {
            // 1. 检测JVM级别的死锁
            detectJVMDeadlock();
            
            // 2. 检测线程池状态异常
            detectThreadPoolAnomalies();
            
            // 3. 检测潜在的死锁风险
            detectPotentialDeadlockRisks();
            
        } catch (Exception e) {
            System.err.println("死锁检测过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检测JVM级别的死锁
     */
    private void detectJVMDeadlock() {
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            System.err.println("\n🚨 检测到JVM级别死锁!");
            System.err.println("死锁线程数量: " + deadlockedThreads.length);
            System.err.println("检测时间: " + new Date());
            
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreads);
            for (ThreadInfo threadInfo : threadInfos) {
                printDeadlockThreadInfo(threadInfo);
            }
            
            // 生成死锁分析报告
            generateDeadlockReport(threadInfos);
        }
    }
    
    /**
     * 检测线程池状态异常
     */
    private void detectThreadPoolAnomalies() {
        for (Map.Entry<String, ThreadPoolExecutor> entry : threadPoolMap.entrySet()) {
            String poolName = entry.getKey();
            ThreadPoolExecutor pool = entry.getValue();
            
            analyzeThreadPoolStatus(poolName, pool);
        }
    }
    
    /**
     * 分析线程池状态
     */
    private void analyzeThreadPoolStatus(String poolName, ThreadPoolExecutor pool) {
        int corePoolSize = pool.getCorePoolSize();
        int maximumPoolSize = pool.getMaximumPoolSize();
        int activeCount = pool.getActiveCount();
        int poolSize = pool.getPoolSize();
        long taskCount = pool.getTaskCount();
        long completedTaskCount = pool.getCompletedTaskCount();
        int queueSize = pool.getQueue().size();
        
        // 计算利用率
        double utilization = (double) activeCount / maximumPoolSize;
        
        // 检测高利用率
        if (utilization >= highUtilizationThreshold) {
            System.out.println("\n⚠️  线程池高利用率警告: " + poolName);
            System.out.println("当前利用率: " + String.format("%.2f%%", utilization * 100));
            System.out.println("活跃线程: " + activeCount + "/" + maximumPoolSize);
        }
        
        // 检测队列积压
        if (queueSize > corePoolSize * 2) {
            System.out.println("\n⚠️  线程池队列积压警告: " + poolName);
            System.out.println("队列大小: " + queueSize);
            System.out.println("建议增加线程数或优化任务处理速度");
        }
        
        // 检测所有线程都在工作但队列还有任务的情况
        if (activeCount == maximumPoolSize && queueSize > 0) {
            System.out.println("\n⚠️  线程池饱和警告: " + poolName);
            System.out.println("所有线程都在工作，队列中还有 " + queueSize + " 个任务等待");
        }
    }
    
    /**
     * 检测潜在的死锁风险
     */
    private void detectPotentialDeadlockRisks() {
        ThreadInfo[] allThreads = threadMXBean.dumpAllThreads(false, false);
        Map<String, List<ThreadInfo>> poolThreads = new HashMap<>();
        
        // 按线程池分组
        for (ThreadInfo threadInfo : allThreads) {
            String threadName = threadInfo.getThreadName();
            for (String poolName : monitoredThreadPools) {
                if (threadName.contains(poolName)) {
                    poolThreads.computeIfAbsent(poolName, k -> new ArrayList<>()).add(threadInfo);
                    break;
                }
            }
        }
        
        // 分析每个线程池的线程状态
        for (Map.Entry<String, List<ThreadInfo>> entry : poolThreads.entrySet()) {
            String poolName = entry.getKey();
            List<ThreadInfo> threads = entry.getValue();
            
            analyzeThreadStates(poolName, threads);
        }
    }
    
    /**
     * 分析线程状态
     */
    private void analyzeThreadStates(String poolName, List<ThreadInfo> threads) {
        int waitingCount = 0;
        int blockedCount = 0;
        int runnableCount = 0;
        int timedWaitingCount = 0;
        
        for (ThreadInfo threadInfo : threads) {
            Thread.State state = threadInfo.getThreadState();
            switch (state) {
                case WAITING:
                    waitingCount++;
                    break;
                case BLOCKED:
                    blockedCount++;
                    break;
                case RUNNABLE:
                    runnableCount++;
                    break;
                case TIMED_WAITING:
                    timedWaitingCount++;
                    break;
            }
        }
        
        // 检测异常状态
        if (waitingCount + timedWaitingCount >= maxWaitingThreadsThreshold) {
            System.out.println("\n⚠️  潜在死锁风险: " + poolName);
            System.out.println("等待线程过多 - WAITING: " + waitingCount + ", TIMED_WAITING: " + timedWaitingCount);
            System.out.println("线程状态分布: RUNNABLE=" + runnableCount + ", BLOCKED=" + blockedCount + 
                             ", WAITING=" + waitingCount + ", TIMED_WAITING=" + timedWaitingCount);
            
            // 详细分析等待的线程
            analyzeWaitingThreads(poolName, threads);
        }
        
        // 如果所有线程都不在运行状态，高度怀疑死锁
        if (runnableCount == 0 && threads.size() > 0) {
            System.err.println("\n🚨 高度怀疑死锁: " + poolName);
            System.err.println("所有线程都不在RUNNABLE状态!");
            for (ThreadInfo threadInfo : threads) {
                printThreadDetailInfo(threadInfo);
            }
        }
    }
    
    /**
     * 分析等待中的线程
     */
    private void analyzeWaitingThreads(String poolName, List<ThreadInfo> threads) {
        System.out.println("\n--- " + poolName + " 等待线程详情 ---");
        for (ThreadInfo threadInfo : threads) {
            if (threadInfo.getThreadState() == Thread.State.WAITING || 
                threadInfo.getThreadState() == Thread.State.TIMED_WAITING) {
                
                System.out.println("线程: " + threadInfo.getThreadName());
                System.out.println("状态: " + threadInfo.getThreadState());
                if (threadInfo.getLockInfo() != null) {
                    System.out.println("等待锁: " + threadInfo.getLockInfo());
                }
                if (threadInfo.getLockOwnerName() != null) {
                    System.out.println("锁持有者: " + threadInfo.getLockOwnerName());
                }
                System.out.println();
            }
        }
    }
    
    /**
     * 打印死锁线程信息
     */
    private void printDeadlockThreadInfo(ThreadInfo threadInfo) {
        System.err.println("\n--- 死锁线程信息 ---");
        System.err.println("线程名: " + threadInfo.getThreadName());
        System.err.println("线程ID: " + threadInfo.getThreadId());
        System.err.println("线程状态: " + threadInfo.getThreadState());
        
        if (threadInfo.getLockInfo() != null) {
            System.err.println("等待的锁: " + threadInfo.getLockInfo());
        }
        
        if (threadInfo.getLockOwnerName() != null) {
            System.err.println("锁的持有者: " + threadInfo.getLockOwnerName() + 
                             " (ID: " + threadInfo.getLockOwnerId() + ")");
        }
        
        // 打印堆栈跟踪
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        if (stackTrace.length > 0) {
            System.err.println("堆栈跟踪:");
            for (int i = 0; i < Math.min(stackTrace.length, 10); i++) {
                System.err.println("  " + stackTrace[i]);
            }
        }
    }
    
    /**
     * 打印线程详细信息
     */
    private void printThreadDetailInfo(ThreadInfo threadInfo) {
        System.out.println("线程: " + threadInfo.getThreadName() + 
                         " (" + threadInfo.getThreadState() + ")");
        if (threadInfo.getLockInfo() != null) {
            System.out.println("  等待: " + threadInfo.getLockInfo());
        }
    }
    
    /**
     * 生成死锁分析报告
     */
    private void generateDeadlockReport(ThreadInfo[] deadlockedThreads) {
        System.err.println("\n=== 死锁分析报告 ===");
        System.err.println("检测时间: " + new Date());
        System.err.println("涉及线程数: " + deadlockedThreads.length);
        
        // 分析死锁环
        System.err.println("\n死锁环分析:");
        for (ThreadInfo threadInfo : deadlockedThreads) {
            if (threadInfo.getLockOwnerName() != null) {
                System.err.println(threadInfo.getThreadName() + " 等待 " + 
                                 threadInfo.getLockOwnerName() + " 持有的锁");
            }
        }
        
        // 建议解决方案
        System.err.println("\n建议解决方案:");
        System.err.println("1. 检查代码中的锁获取顺序，确保一致性");
        System.err.println("2. 避免在持有锁的情况下调用可能阻塞的操作");
        System.err.println("3. 考虑使用超时机制避免无限等待");
        System.err.println("4. 对于线程池，避免主任务等待子任务且共用同一线程池");
        System.err.println("5. 考虑重新设计任务分解和线程池配置");
    }
    
    /**
     * 设置检测间隔
     */
    public void setDetectionInterval(long intervalMs) {
        this.detectionInterval = intervalMs;
    }
    
    /**
     * 设置等待线程数阈值
     */
    public void setMaxWaitingThreadsThreshold(int threshold) {
        this.maxWaitingThreadsThreshold = threshold;
    }
    
    /**
     * 设置高利用率阈值
     */
    public void setHighUtilizationThreshold(double threshold) {
        this.highUtilizationThreshold = threshold;
    }
    
    /**
     * 手动触发一次检测
     */
    public void triggerDetection() {
        System.out.println("手动触发死锁检测...");
        performDetection();
    }
    
    /**
     * 获取当前监控的线程池状态摘要
     */
    public void printThreadPoolSummary() {
        System.out.println("\n=== 线程池状态摘要 ===");
        for (Map.Entry<String, ThreadPoolExecutor> entry : threadPoolMap.entrySet()) {
            String poolName = entry.getKey();
            ThreadPoolExecutor pool = entry.getValue();
            
            System.out.println("\n线程池: " + poolName);
            System.out.println("  核心线程数: " + pool.getCorePoolSize());
            System.out.println("  最大线程数: " + pool.getMaximumPoolSize());
            System.out.println("  当前线程数: " + pool.getPoolSize());
            System.out.println("  活跃线程数: " + pool.getActiveCount());
            System.out.println("  队列大小: " + pool.getQueue().size());
            System.out.println("  已完成任务: " + pool.getCompletedTaskCount());
            System.out.println("  总任务数: " + pool.getTaskCount());
            
            double utilization = (double) pool.getActiveCount() / pool.getMaximumPoolSize();
            System.out.println("  利用率: " + String.format("%.2f%%", utilization * 100));
        }
    }
}