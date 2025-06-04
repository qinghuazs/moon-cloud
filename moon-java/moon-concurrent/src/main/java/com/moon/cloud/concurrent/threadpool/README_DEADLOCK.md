# 线程池死锁问题详解

## 概述

本文档详细介绍了线程池中可能出现的死锁问题，特别是主子线程共用线程池时的死锁情况，以及如何检测和解决这些问题。

## 死锁场景

### 1. 主子任务共用线程池死锁

**问题描述：**
当主任务和子任务共用同一个线程池，且主任务需要等待子任务完成时，可能发生死锁。

**死锁条件：**
- 线程池容量有限（如核心线程数=2，最大线程数=2）
- 主任务占用所有线程
- 主任务提交子任务到同一线程池
- 主任务等待子任务完成
- 子任务无法获得线程执行

**示例场景：**
```
线程池: 2个线程
主任务1 (线程1) -> 提交子任务1 -> 等待子任务1完成
主任务2 (线程2) -> 提交子任务2 -> 等待子任务2完成
子任务1、子任务2 在队列中等待，但没有可用线程执行
```

### 2. 循环依赖任务死锁

**问题描述：**
任务之间存在循环依赖关系，如任务A等待任务B，任务B等待任务C，任务C等待任务A。

### 3. 资源竞争死锁

**问题描述：**
多个任务竞争多个资源，且获取顺序不一致导致的死锁。

## 死锁检测方法

### 1. JVM级别死锁检测

使用 `ThreadMXBean.findDeadlockedThreads()` 检测JVM级别的死锁：

```java
ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
if (deadlockedThreads != null) {
    // 发现死锁
    ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreads);
    // 分析死锁信息
}
```

### 2. 线程池状态监控

监控线程池的关键指标：
- 活跃线程数 vs 最大线程数
- 队列大小
- 等待/阻塞线程数量
- 任务完成率

### 3. 线程状态分析

分析线程状态分布：
- `RUNNABLE`: 正在运行的线程
- `WAITING`: 无限期等待的线程
- `TIMED_WAITING`: 有超时的等待线程
- `BLOCKED`: 被阻塞的线程

**死锁特征：**
- 所有线程都不在 `RUNNABLE` 状态
- 大量线程处于 `WAITING` 或 `BLOCKED` 状态
- 线程池利用率100%但任务无法完成

### 4. 自动检测工具

使用 `ThreadPoolDeadlockDetector` 进行自动检测：

```java
ThreadPoolDeadlockDetector detector = new ThreadPoolDeadlockDetector();
detector.addMonitoredThreadPool("my-pool", threadPool);
detector.startDetection();
```

## 解决方案

### 1. 分离线程池

**最佳实践：** 为主任务和子任务使用不同的线程池

```java
// 主任务线程池
ExecutorService mainTaskPool = MoonThreadPool.createCustomThreadPool(
    2, 2, 60L, 10, "main-task"
);

// 子任务线程池
ExecutorService subTaskPool = MoonThreadPool.createCustomThreadPool(
    4, 4, 60L, 20, "sub-task"
);

// 主任务提交子任务到独立线程池
mainTaskPool.submit(() -> {
    Future<String> subTask = subTaskPool.submit(() -> {
        // 子任务逻辑
        return "result";
    });
    return subTask.get(); // 安全等待
});
```

### 2. 异步处理

使用 `CompletableFuture` 进行异步处理，避免阻塞等待：

```java
CompletableFuture<String> mainTask = CompletableFuture.supplyAsync(() -> {
    // 主任务逻辑
    return "main-result";
}, mainTaskPool);

CompletableFuture<String> subTask = mainTask.thenComposeAsync(mainResult -> {
    return CompletableFuture.supplyAsync(() -> {
        // 子任务逻辑
        return "sub-result";
    }, subTaskPool);
});
```

### 3. 超时机制

为所有阻塞操作设置超时：

```java
try {
    String result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    // 处理超时情况
    future.cancel(true);
}
```

### 4. 线程池配置优化

**CPU密集型任务：**
```java
int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
```

**IO密集型任务：**
```java
int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
```

**混合型任务：**
- 使用不同的线程池
- 根据任务特性调整线程数

### 5. 任务设计原则

1. **避免嵌套提交：** 不要在任务中向同一线程池提交新任务并等待
2. **减少依赖：** 尽量减少任务间的依赖关系
3. **资源隔离：** 不同类型的任务使用不同的资源
4. **优雅降级：** 提供超时和失败处理机制

## 预防措施

### 1. 代码审查检查点

- [ ] 是否存在任务向同一线程池提交子任务并等待？
- [ ] 是否存在循环依赖的任务？
- [ ] 是否为所有阻塞操作设置了超时？
- [ ] 线程池配置是否合理？

### 2. 监控告警

设置监控指标和告警：
- 线程池利用率 > 90%
- 队列积压 > 阈值
- 等待线程数 > 阈值
- 任务平均执行时间异常

### 3. 测试验证

- 压力测试验证线程池配置
- 模拟死锁场景进行测试
- 验证超时和降级机制

## 示例代码

### 运行死锁演示

```bash
# 编译
javac -cp . com/moon/cloud/concurrent/threadpool/*.java

# 运行基本死锁演示
java com.moon.cloud.concurrent.threadpool.DeadlockDemo

# 运行完整测试套件
java com.moon.cloud.concurrent.threadpool.DeadlockTestRunner
```

### 使用死锁检测器

```java
// 创建检测器
ThreadPoolDeadlockDetector detector = new ThreadPoolDeadlockDetector();

// 配置检测参数
detector.setDetectionInterval(1000); // 1秒检测一次
detector.setMaxWaitingThreadsThreshold(5); // 等待线程阈值
detector.setHighUtilizationThreshold(0.9); // 高利用率阈值

// 添加监控的线程池
detector.addMonitoredThreadPool("my-pool", threadPool);

// 开始检测
detector.startDetection();

// 手动触发检测
detector.triggerDetection();

// 查看状态摘要
detector.printThreadPoolSummary();

// 停止检测
detector.stopDetection();
```

## 总结

线程池死锁是并发编程中的常见问题，主要原因是资源竞争和任务依赖。通过合理的线程池设计、任务分离、超时机制和实时监控，可以有效预防和解决死锁问题。

**关键要点：**
1. 主子任务使用不同的线程池
2. 避免任务间的循环依赖
3. 为所有阻塞操作设置超时
4. 实施实时监控和告警
5. 定期进行压力测试和死锁场景验证