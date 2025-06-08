package com.moon.cloud.concurrent.threadpool;

import com.moon.cloud.threadpool.factory.MoonThreadPoolFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CPU密集型线程池性能对比演示
 * 测试不同线程数对CPU利用率、任务完成时间的影响
 */
public class MoonThreadPoolFactoryDemo {
    
    private static final int TASK_COUNT = 1000; // 总任务数
    private static final int CALCULATION_ITERATIONS = 1000000; // 每个任务的计算迭代次数
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CPU密集型线程池性能对比测试 ===");
        System.out.println("CPU核心数: " + Runtime.getRuntime().availableProcessors());
        System.out.println("总任务数: " + TASK_COUNT);
        System.out.println("每个任务计算迭代次数: " + CALCULATION_ITERATIONS);
        System.out.println();
        
        // 测试不同的线程数配置
        int[] threadCounts = {
            1,                                                    // 单线程
            Runtime.getRuntime().availableProcessors(),          // CPU核心数
            Runtime.getRuntime().availableProcessors() + 1,      // CPU核心数 + 1 (推荐配置)
            Runtime.getRuntime().availableProcessors() * 2,      // CPU核心数 * 2
            Runtime.getRuntime().availableProcessors() * 4       // CPU核心数 * 4
        };
        
        for (int threadCount : threadCounts) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("测试线程数: " + threadCount);
            System.out.println("=".repeat(50));
            
            performanceTest(threadCount);
            
            // 等待一段时间让系统稳定
            Thread.sleep(2000);
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    /**
     * 执行性能测试
     * @param threadCount 线程数
     */
    private static void performanceTest(int threadCount) throws InterruptedException {
        // 创建自定义线程池
        ExecutorService executor = MoonThreadPoolFactory.createCustomThreadPool(
            threadCount, threadCount, 60L, 1000, "perf-test"
        );
        
        // 获取线程管理Bean用于监控
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        
        // 记录开始时间和线程信息
        long startTime = System.currentTimeMillis();
        long startCpuTime = getCurrentThreadCpuTime();
        
        // 提交任务
        List<Future<Long>> futures = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(TASK_COUNT);
        AtomicLong totalCalculationTime = new AtomicLong(0);
        
        for (int i = 0; i < TASK_COUNT; i++) {
            final int taskId = i;
            Future<Long> future = executor.submit(() -> {
                long taskStartTime = System.nanoTime();
                
                // CPU密集型计算任务：计算素数
                long result = calculatePrimes(CALCULATION_ITERATIONS);
                
                long taskEndTime = System.nanoTime();
                long taskDuration = taskEndTime - taskStartTime;
                totalCalculationTime.addAndGet(taskDuration);
                
                latch.countDown();
                return result;
            });
            futures.add(future);
        }
        
        // 等待所有任务完成
        latch.await();
        
        // 记录结束时间
        long endTime = System.currentTimeMillis();
        long endCpuTime = getCurrentThreadCpuTime();
        
        // 计算性能指标
        long totalTime = endTime - startTime;
        long cpuTime = endCpuTime - startCpuTime;
        double cpuUtilization = (double) cpuTime / (totalTime * 1000000) * 100; // 转换为百分比
        
        // 获取所有任务结果
        long totalResults = 0;
        for (Future<Long> future : futures) {
            try {
                totalResults += future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        // 输出性能指标
        System.out.println("总执行时间: " + totalTime + " ms");
        System.out.println("平均每个任务时间: " + (totalTime * 1.0 / TASK_COUNT) + " ms");
        System.out.println("CPU利用时间: " + (cpuTime / 1000000) + " ms");
        System.out.println("CPU利用率: " + String.format("%.2f", cpuUtilization) + "%");
        System.out.println("吞吐量: " + String.format("%.2f", TASK_COUNT * 1000.0 / totalTime) + " 任务/秒");
        System.out.println("总计算结果: " + totalResults);
        
        // 线程池信息
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
            System.out.println("线程池核心线程数: " + tpe.getCorePoolSize());
            System.out.println("线程池最大线程数: " + tpe.getMaximumPoolSize());
            System.out.println("已完成任务数: " + tpe.getCompletedTaskCount());
        }
        
        // 优雅关闭线程池
        MoonThreadPoolFactory.shutdownGracefully(executor, 30);
    }
    
    /**
     * CPU密集型计算任务：计算指定范围内的素数个数
     * @param limit 计算范围
     * @return 素数个数
     */
    private static long calculatePrimes(int limit) {
        long count = 0;
        for (int i = 2; i <= limit; i++) {
            if (isPrime(i)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 判断是否为素数
     * @param n 待判断的数
     * @return 是否为素数
     */
    private static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取当前线程的CPU时间（纳秒）
     * @return CPU时间
     */
    private static long getCurrentThreadCpuTime() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
            return threadMXBean.getCurrentThreadCpuTime();
        }
        return 0;
    }
}
