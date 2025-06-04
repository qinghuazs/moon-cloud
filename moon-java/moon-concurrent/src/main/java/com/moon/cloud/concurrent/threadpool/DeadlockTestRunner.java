package com.moon.cloud.concurrent.threadpool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 死锁测试运行器
 * 提供多种死锁场景的测试和演示
 */
public class DeadlockTestRunner {
    
    private static final AtomicInteger taskCounter = new AtomicInteger(0);
    
    public static void main(String[] args) {
        System.out.println("=== 线程池死锁测试套件 ===");
        System.out.println("CPU核心数: " + Runtime.getRuntime().availableProcessors());
        System.out.println();
        
        // 创建死锁检测器
        ThreadPoolDeadlockDetector detector = new ThreadPoolDeadlockDetector();
        detector.setDetectionInterval(1000); // 1秒检测一次
        detector.setMaxWaitingThreadsThreshold(3);
        
        try {
            // 测试场景1：主子任务共用线程池导致的死锁
            System.out.println("\n" + "=".repeat(60));
            System.out.println("测试场景1: 主子任务共用线程池死锁");
            System.out.println("=".repeat(60));
            testMainSubTaskDeadlock(detector);
            
            Thread.sleep(3000);
            
            // 测试场景2：循环依赖任务死锁
            System.out.println("\n" + "=".repeat(60));
            System.out.println("测试场景2: 循环依赖任务死锁");
            System.out.println("=".repeat(60));
            testCircularDependencyDeadlock(detector);
            
            Thread.sleep(3000);
            
            // 测试场景3：正确的解决方案
            System.out.println("\n" + "=".repeat(60));
            System.out.println("测试场景3: 正确的解决方案");
            System.out.println("=".repeat(60));
            testCorrectSolution(detector);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            detector.stopDetection();
        }
    }
    
    /**
     * 测试主子任务共用线程池导致的死锁
     */
    private static void testMainSubTaskDeadlock(ThreadPoolDeadlockDetector detector) {
        // 创建小容量线程池
        ThreadPoolExecutor executor = (ThreadPoolExecutor) MoonThreadPool.createCustomThreadPool(
            2, 2, 60L, 5, "deadlock-test"
        );
        
        // 添加到监控
        detector.addMonitoredThreadPool("deadlock-test", executor);
        detector.startDetection();
        
        try {
            System.out.println("线程池配置: 核心线程数=2, 最大线程数=2, 队列容量=5");
            System.out.println("提交4个主任务，每个主任务都会提交子任务并等待结果...");
            
            Future<?>[] futures = new Future[4];
            
            for (int i = 0; i < 4; i++) {
                final int taskId = i;
                futures[i] = executor.submit(() -> {
                    return executeMainTaskWithSubTask(executor, taskId);
                });
                System.out.println("已提交主任务 " + taskId);
            }
            
            // 等待任务完成（会发生死锁）
            System.out.println("\n等待任务完成...");
            for (int i = 0; i < futures.length; i++) {
                try {
                    String result = (String) futures[i].get(8, TimeUnit.SECONDS);
                    System.out.println("✅ 主任务 " + i + " 完成: " + result);
                } catch (TimeoutException e) {
                    System.err.println("❌ 主任务 " + i + " 超时，发生死锁!");
                } catch (Exception e) {
                    System.err.println("❌ 主任务 " + i + " 异常: " + e.getMessage());
                }
            }
            
        } finally {
            System.out.println("\n强制关闭线程池...");
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("线程池未能在指定时间内关闭");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 测试循环依赖任务死锁
     */
    private static void testCircularDependencyDeadlock(ThreadPoolDeadlockDetector detector) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) MoonThreadPool.createCustomThreadPool(
            3, 3, 60L, 10, "circular-deadlock"
        );
        
        detector.addMonitoredThreadPool("circular-deadlock", executor);
        
        try {
            System.out.println("创建循环依赖的任务: A等待B, B等待C, C等待A");
            
            CompletableFuture<String> taskA = new CompletableFuture<>();
            CompletableFuture<String> taskB = new CompletableFuture<>();
            CompletableFuture<String> taskC = new CompletableFuture<>();
            
            // 任务A等待任务B
            executor.submit(() -> {
                try {
                    System.out.println("[" + Thread.currentThread().getName() + "] 任务A开始，等待任务B");
                    String resultB = taskB.get(5, TimeUnit.SECONDS);
                    taskA.complete("任务A完成，依赖: " + resultB);
                } catch (Exception e) {
                    taskA.completeExceptionally(e);
                }
            });
            
            // 任务B等待任务C
            executor.submit(() -> {
                try {
                    System.out.println("[" + Thread.currentThread().getName() + "] 任务B开始，等待任务C");
                    String resultC = taskC.get(5, TimeUnit.SECONDS);
                    taskB.complete("任务B完成，依赖: " + resultC);
                } catch (Exception e) {
                    taskB.completeExceptionally(e);
                }
            });
            
            // 任务C等待任务A
            executor.submit(() -> {
                try {
                    System.out.println("[" + Thread.currentThread().getName() + "] 任务C开始，等待任务A");
                    String resultA = taskA.get(5, TimeUnit.SECONDS);
                    taskC.complete("任务C完成，依赖: " + resultA);
                } catch (Exception e) {
                    taskC.completeExceptionally(e);
                }
            });
            
            // 等待任务完成
            try {
                String result = taskA.get(10, TimeUnit.SECONDS);
                System.out.println("✅ 循环依赖任务完成: " + result);
            } catch (TimeoutException e) {
                System.err.println("❌ 循环依赖任务超时，发生死锁!");
            } catch (Exception e) {
                System.err.println("❌ 循环依赖任务异常: " + e.getMessage());
            }
            
        } finally {
            executor.shutdownNow();
        }
    }
    
    /**
     * 测试正确的解决方案
     */
    private static void testCorrectSolution(ThreadPoolDeadlockDetector detector) {
        // 使用分离的线程池
        ThreadPoolExecutor mainTaskPool = (ThreadPoolExecutor) MoonThreadPool.createCustomThreadPool(
            2, 2, 60L, 10, "main-task"
        );
        ThreadPoolExecutor subTaskPool = (ThreadPoolExecutor) MoonThreadPool.createCustomThreadPool(
            4, 4, 60L, 20, "sub-task"
        );
        
        detector.addMonitoredThreadPool("main-task", mainTaskPool);
        detector.addMonitoredThreadPool("sub-task", subTaskPool);
        
        try {
            System.out.println("使用分离的线程池: 主任务池(2线程) + 子任务池(4线程)");
            System.out.println("提交4个主任务...");
            
            Future<?>[] futures = new Future[4];
            
            for (int i = 0; i < 4; i++) {
                final int taskId = i;
                futures[i] = mainTaskPool.submit(() -> {
                    return executeMainTaskCorrectly(subTaskPool, taskId);
                });
                System.out.println("已提交主任务 " + taskId);
            }
            
            // 等待任务完成
            System.out.println("\n等待任务完成...");
            for (int i = 0; i < futures.length; i++) {
                try {
                    String result = (String) futures[i].get(10, TimeUnit.SECONDS);
                    System.out.println("✅ 主任务 " + i + " 完成: " + result);
                } catch (Exception e) {
                    System.err.println("❌ 主任务 " + i + " 异常: " + e.getMessage());
                }
            }
            
            // 打印线程池状态摘要
            detector.printThreadPoolSummary();
            
        } finally {
            MoonThreadPool.shutdownGracefully(mainTaskPool, 5);
            MoonThreadPool.shutdownGracefully(subTaskPool, 5);
        }
    }
    
    /**
     * 执行会导致死锁的主任务
     */
    private static String executeMainTaskWithSubTask(ExecutorService executor, int mainTaskId) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] 主任务 " + mainTaskId + " 开始执行");
        
        try {
            // 模拟一些工作
            Thread.sleep(100);
            
            // 提交子任务到同一个线程池
            System.out.println("[" + threadName + "] 主任务 " + mainTaskId + " 提交子任务");
            Future<String> subTaskFuture = executor.submit(() -> {
                return executeSubTask(mainTaskId);
            });
            
            // 等待子任务完成 - 这里会导致死锁
            String subResult = subTaskFuture.get(6, TimeUnit.SECONDS);
            
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
     * 正确的主任务实现
     */
    private static String executeMainTaskCorrectly(ExecutorService subTaskPool, int mainTaskId) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] 正确的主任务 " + mainTaskId + " 开始执行");
        
        try {
            // 模拟一些工作
            Thread.sleep(100);
            
            // 提交子任务到独立的线程池
            System.out.println("[" + threadName + "] 主任务 " + mainTaskId + " 提交子任务到独立线程池");
            Future<String> subTaskFuture = subTaskPool.submit(() -> {
                return executeSubTask(mainTaskId);
            });
            
            String subResult = subTaskFuture.get();
            
            String result = "正确的主任务" + mainTaskId + "完成，子任务结果: " + subResult;
            System.out.println("[" + threadName + "] " + result);
            return result;
            
        } catch (Exception e) {
            String error = "正确的主任务" + mainTaskId + "异常: " + e.getMessage();
            System.err.println("[" + threadName + "] " + error);
            return error;
        }
    }
    
    /**
     * 子任务实现
     */
    private static String executeSubTask(int mainTaskId) {
        String threadName = Thread.currentThread().getName();
        int subTaskId = taskCounter.incrementAndGet();
        
        System.out.println("[" + threadName + "] 子任务 " + subTaskId + " (属于主任务" + mainTaskId + ") 开始执行");
        
        try {
            // 模拟子任务工作
            Thread.sleep(300);
            
            String result = "子任务" + subTaskId + "完成";
            System.out.println("[" + threadName + "] " + result);
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "子任务" + subTaskId + "被中断";
        }
    }
    
    /**
     * 演示如何使用死锁检测器进行实时监控
     */
    public static void demonstrateRealTimeMonitoring() {
        System.out.println("\n=== 实时监控演示 ===");
        
        ThreadPoolDeadlockDetector detector = new ThreadPoolDeadlockDetector();
        detector.setDetectionInterval(500); // 500ms检测一次
        
        ThreadPoolExecutor pool = (ThreadPoolExecutor) MoonThreadPool.createCustomThreadPool(
            2, 2, 60L, 5, "monitor-demo"
        );
        
        detector.addMonitoredThreadPool("monitor-demo", pool);
        detector.startDetection();
        
        try {
            // 提交一些正常任务
            for (int i = 0; i < 3; i++) {
                final int taskId = i;
                pool.submit(() -> {
                    try {
                        System.out.println("正常任务 " + taskId + " 执行中...");
                        Thread.sleep(2000);
                        System.out.println("正常任务 " + taskId + " 完成");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            // 手动触发检测
            Thread.sleep(1000);
            detector.triggerDetection();
            
            Thread.sleep(3000);
            detector.printThreadPoolSummary();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            detector.stopDetection();
            MoonThreadPool.shutdownGracefully(pool, 5);
        }
    }
}