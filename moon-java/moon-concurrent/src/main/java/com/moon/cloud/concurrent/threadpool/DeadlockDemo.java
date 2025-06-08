package com.moon.cloud.concurrent.threadpool;

import com.moon.cloud.threadpool.factory.MoonThreadPoolFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池死锁演示和检测
 * 演示主子线程共用线程池时可能出现的死锁情况
 */
public class DeadlockDemo {
    
    private static final int POOL_SIZE = 2; // 故意设置较小的线程池大小来容易触发死锁
    private static final AtomicInteger taskCounter = new AtomicInteger(0);
    
    public static void main(String[] args) {
        System.out.println("=== 线程池死锁演示 ===");
        System.out.println("线程池大小: " + POOL_SIZE);
        System.out.println();
        
        // 启动死锁检测线程
        startDeadlockDetector();
        
        // 演示死锁场景
        demonstrateDeadlock();
    }
    
    /**
     * 演示死锁场景：主任务等待子任务完成，但子任务无法获得线程执行
     */
    private static void demonstrateDeadlock() {
        // 创建小容量线程池，容易触发死锁
        ExecutorService executor = MoonThreadPoolFactory.createCustomThreadPool(
            POOL_SIZE, POOL_SIZE, 60L, 10, "deadlock-demo"
        );
        
        try {
            System.out.println("开始提交主任务...");
            
            // 提交多个主任务，每个主任务都会提交子任务并等待结果
            Future<?>[] mainTasks = new Future[POOL_SIZE + 2]; // 故意超过线程池大小
            
            for (int i = 0; i < mainTasks.length; i++) {
                final int taskId = i;
                mainTasks[i] = executor.submit(() -> {
                    return executeMainTask(executor, taskId);
                });
            }
            
            // 等待所有主任务完成（这里会发生死锁）
            System.out.println("等待所有主任务完成...");
            for (int i = 0; i < mainTasks.length; i++) {
                try {
                    Object result = mainTasks[i].get(10, TimeUnit.SECONDS); // 设置超时
                    System.out.println("主任务 " + i + " 完成，结果: " + result);
                } catch (TimeoutException e) {
                    System.err.println("主任务 " + i + " 超时，可能发生死锁!");
                } catch (Exception e) {
                    System.err.println("主任务 " + i + " 执行异常: " + e.getMessage());
                }
            }
            
        } finally {
            System.out.println("\n强制关闭线程池...");
            executor.shutdownNow();
        }
    }
    
    /**
     * 主任务：提交子任务并等待结果
     */
    private static String executeMainTask(ExecutorService executor, int mainTaskId) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] 主任务 " + mainTaskId + " 开始执行");
        
        try {
            // 模拟一些工作
            Thread.sleep(100);
            
            // 提交子任务并等待结果
            System.out.println("[" + threadName + "] 主任务 " + mainTaskId + " 提交子任务");
            Future<String> subTaskFuture = executor.submit(() -> {
                return executeSubTask(mainTaskId);
            });
            
            // 等待子任务完成 - 这里可能导致死锁
            String subResult = subTaskFuture.get(5, TimeUnit.SECONDS);
            
            String result = "主任务" + mainTaskId + "完成，子任务结果: " + subResult;
            System.out.println("[" + threadName + "] " + result);
            return result;
            
        } catch (TimeoutException e) {
            String error = "主任务" + mainTaskId + "的子任务超时";
            System.err.println("[" + threadName + "] " + error);
            return error;
        } catch (Exception e) {
            String error = "主任务" + mainTaskId + "执行异常: " + e.getMessage();
            System.err.println("[" + threadName + "] " + error);
            return error;
        }
    }
    
    /**
     * 子任务：执行具体的业务逻辑
     */
    private static String executeSubTask(int mainTaskId) {
        String threadName = Thread.currentThread().getName();
        int subTaskId = taskCounter.incrementAndGet();
        
        System.out.println("[" + threadName + "] 子任务 " + subTaskId + " (属于主任务" + mainTaskId + ") 开始执行");
        
        try {
            // 模拟子任务工作
            Thread.sleep(200);
            
            String result = "子任务" + subTaskId + "完成";
            System.out.println("[" + threadName + "] " + result);
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "子任务" + subTaskId + "被中断";
        }
    }
    
    /**
     * 启动死锁检测线程
     */
    private static void startDeadlockDetector() {
        Thread deadlockDetector = new Thread(() -> {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(200); // 每2秒检测一次
                    
                    // 检测死锁
                    long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
                    if (deadlockedThreads != null && deadlockedThreads.length > 0) {
                        System.err.println("\n!!! 检测到死锁 !!!");
                        System.err.println("死锁线程数量: " + deadlockedThreads.length);
                        
                        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreads);
                        for (ThreadInfo threadInfo : threadInfos) {
                            System.err.println("死锁线程: " + threadInfo.getThreadName() + 
                                             " (ID: " + threadInfo.getThreadId() + ")");
                            System.err.println("线程状态: " + threadInfo.getThreadState());
                            System.err.println("阻塞信息: " + threadInfo.getLockInfo());
                        }
                        System.err.println();
                    }
                    
                    // 检测线程池状态
                    checkThreadPoolStatus();
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "DeadlockDetector");
        
        deadlockDetector.setDaemon(true);
        deadlockDetector.start();
        System.out.println("死锁检测线程已启动");
    }
    
    /**
     * 检测线程池状态，识别潜在的死锁情况
     */
    private static void checkThreadPoolStatus() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] allThreads = threadMXBean.dumpAllThreads(false, false);
        
        int waitingThreads = 0;
        int blockedThreads = 0;
        int runnableThreads = 0;
        
        for (ThreadInfo threadInfo : allThreads) {
            if (threadInfo.getThreadName().contains("deadlock-demo")) {
                Thread.State state = threadInfo.getThreadState();
                switch (state) {
                    case WAITING:
                    case TIMED_WAITING:
                        waitingThreads++;
                        break;
                    case BLOCKED:
                        blockedThreads++;
                        break;
                    case RUNNABLE:
                        runnableThreads++;
                        break;
                }
            }
        }
        
        if (waitingThreads > 0 || blockedThreads > 0) {
            System.out.println("\n=== 线程池状态检测 ===");
            System.out.println("运行中线程: " + runnableThreads);
            System.out.println("等待中线程: " + waitingThreads);
            System.out.println("阻塞中线程: " + blockedThreads);
            
            // 如果所有线程都在等待，可能发生了死锁
            if (runnableThreads == 0 && (waitingThreads > 0 || blockedThreads > 0)) {
                System.err.println("⚠️  警告: 所有线程都在等待，可能发生死锁!");
            }
            System.out.println();
        }
    }
    
    /**
     * 演示如何避免死锁的正确做法
     */
    public static void demonstrateCorrectApproach() {
        System.out.println("\n=== 正确的做法：避免死锁 ===");
        
        // 使用两个不同的线程池：主任务池和子任务池
        ExecutorService mainTaskPool = MoonThreadPoolFactory.createCustomThreadPool(
            2, 2, 60L, 10, "main-task"
        );
        ExecutorService subTaskPool = MoonThreadPoolFactory.createCustomThreadPool(
            4, 4, 60L, 20, "sub-task"
        );
        
        try {
            Future<?>[] mainTasks = new Future[4];
            
            for (int i = 0; i < mainTasks.length; i++) {
                final int taskId = i;
                mainTasks[i] = mainTaskPool.submit(() -> {
                    return executeMainTaskCorrectly(subTaskPool, taskId);
                });
            }
            
            // 等待所有主任务完成
            for (int i = 0; i < mainTasks.length; i++) {
                try {
                    Object result = mainTasks[i].get();
                    System.out.println("主任务 " + i + " 完成: " + result);
                } catch (Exception e) {
                    System.err.println("主任务 " + i + " 异常: " + e.getMessage());
                }
            }
            
        } finally {
            MoonThreadPoolFactory.shutdownGracefully(mainTaskPool, 10);
            MoonThreadPoolFactory.shutdownGracefully(subTaskPool, 10);
        }
    }
    
    /**
     * 正确的主任务实现：使用独立的子任务线程池
     */
    private static String executeMainTaskCorrectly(ExecutorService subTaskPool, int mainTaskId) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] 正确的主任务 " + mainTaskId + " 开始执行");
        
        try {
            // 提交子任务到独立的线程池
            Future<String> subTaskFuture = subTaskPool.submit(() -> {
                return executeSubTask(mainTaskId);
            });
            
            String subResult = subTaskFuture.get();
            return "正确的主任务" + mainTaskId + "完成，子任务结果: " + subResult;
            
        } catch (Exception e) {
            return "正确的主任务" + mainTaskId + "异常: " + e.getMessage();
        }
    }
}