# Moon Spring Boot Starter ThreadPool

## 概述

`moon-spring-boot-starter-threadpool` 是一个功能强大的线程池管理组件，提供了线程池的创建、管理、监控和优雅关闭等功能。

### 主要特性

- ✅ **多种线程池类型**：支持 IO 密集型、CPU 密集型、定时任务、自定义线程池
- ✅ **自动配置**：基于 Spring Boot 3.x 自动配置机制
- ✅ **线程池监控**：通过 Spring Boot Actuator 提供实时监控
- ✅ **优雅关闭**：应用关闭时自动优雅关闭所有线程池
- ✅ **拒绝策略**：支持重试拒绝策略，提高任务执行成功率
- ✅ **指标收集**：自动收集任务执行时间等关键指标
- ✅ **动态调整**：支持运行时动态调整线程池参数
- ✅ **统一管理**：集中注册和管理所有线程池

## 版本要求

- Java 21+
- Spring Boot 3.4.1+
- Maven 3.6+

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.moon.cloud.threadpool</groupId>
    <artifactId>moon-spring-boot-starter-threadpool</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基础使用

#### 2.1 创建 IO 密集型线程池

```java
@Component
public class MyService {

    @Autowired
    private MoonThreadPoolFactory threadPoolFactory;

    private ThreadPoolExecutor ioPool;

    @PostConstruct
    public void init() {
        // 创建 IO 密集型线程池
        ioPool = threadPoolFactory.createIoIntensiveThreadPool("my-io-pool");
    }

    public void executeIoTask(Runnable task) {
        ioPool.execute(task);
    }
}
```

#### 2.2 创建 CPU 密集型线程池

```java
@Component
public class ComputeService {

    @Autowired
    private MoonThreadPoolFactory threadPoolFactory;

    private ThreadPoolExecutor cpuPool;

    @PostConstruct
    public void init() {
        // 创建 CPU 密集型线程池
        cpuPool = threadPoolFactory.createCpuIntensiveThreadPool("my-cpu-pool");
    }

    public Future<String> computeAsync(Callable<String> task) {
        return cpuPool.submit(task);
    }
}
```

### 3. 配置文件

```yaml
moon:
  threadpool:
    # 是否启用线程池自动配置
    enabled: true
    # 是否启用监控
    monitor-enabled: true
    # 优雅关闭超时时间（秒）
    shutdown-timeout: 60
    # 是否等待任务完成后再关闭
    wait-for-tasks-to-complete-on-shutdown: true
    # 是否预启动所有核心线程
    prestart-all-core-threads: false
    # 线程名称前缀
    thread-name-prefix: moon-pool-
    # 线程优先级（1-10）
    thread-priority: 5
    # 是否设置为守护线程
    daemon: false

    # IO 密集型线程池配置
    io-intensive:
      core-pool-size: 16  # 默认为 CPU 核数 * 2
      maximum-pool-size: 32  # 默认为 corePoolSize * 2
      keep-alive-time: 60
      queue-capacity: 1000
      allow-core-thread-time-out: false
      rejected-execution-handler: RETRY  # CALLER_RUNS, ABORT, DISCARD, DISCARD_OLDEST, RETRY

    # CPU 密集型线程池配置
    cpu-intensive:
      core-pool-size: 9  # 默认为 CPU 核数 + 1
      maximum-pool-size: 9
      keep-alive-time: 60
      queue-capacity: 500
      allow-core-thread-time-out: false
      rejected-execution-handler: RETRY

    # 自定义线程池配置
    custom:
      order-process-pool:
        core-pool-size: 10
        maximum-pool-size: 20
        keep-alive-time: 120
        queue-capacity: 2000
        rejected-execution-handler: CALLER_RUNS

      report-generate-pool:
        core-pool-size: 5
        maximum-pool-size: 10
        keep-alive-time: 300
        queue-capacity: 100
        rejected-execution-handler: ABORT

    # 重试拒绝策略配置
    retry:
      enabled: true
      max-attempts: 5
      retry-interval: 100  # 毫秒
      backoff-multiplier: 1.5
      max-retry-interval: 1000  # 毫秒
```

### 4. 高级功能

#### 4.1 自定义线程池

```java
@Component
public class CustomPoolService {

    @Autowired
    private MoonThreadPoolFactory threadPoolFactory;

    public void createCustomPool() {
        ThreadPoolExecutor customPool = threadPoolFactory.createCustomThreadPool(
            "custom-pool",
            10,  // 核心线程数
            20,  // 最大线程数
            60,  // 空闲时间
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),  // 队列
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
    }
}
```

#### 4.2 定时任务线程池

```java
@Component
public class ScheduledTaskService {

    @Autowired
    private MoonThreadPoolFactory threadPoolFactory;

    private ScheduledThreadPoolExecutor scheduledPool;

    @PostConstruct
    public void init() {
        scheduledPool = threadPoolFactory.createScheduledThreadPool("scheduled-pool", 5);
    }

    public void scheduleTask() {
        // 延迟执行
        scheduledPool.schedule(() -> {
            System.out.println("延迟任务执行");
        }, 5, TimeUnit.SECONDS);

        // 周期执行
        scheduledPool.scheduleAtFixedRate(() -> {
            System.out.println("周期任务执行");
        }, 0, 10, TimeUnit.SECONDS);
    }
}
```

#### 4.3 线程池监控

通过 Spring Boot Actuator 端点监控线程池：

```bash
# 获取所有线程池信息
GET http://localhost:8080/actuator/threadpools

# 获取特定线程池信息
GET http://localhost:8080/actuator/threadpools/{poolName}

# 动态调整线程池参数
POST http://localhost:8080/actuator/threadpools/{poolName}
Content-Type: application/json
{
    "corePoolSize": 20,
    "maximumPoolSize": 40
}

# 关闭线程池
DELETE http://localhost:8080/actuator/threadpools/{poolName}
```

#### 4.4 指标收集

```java
@Component
public class MetricsService {

    public void printMetrics() {
        // 获取线程池统计信息
        Map<String, ThreadPoolRegistry.ThreadPoolStats> stats =
            ThreadPoolRegistry.getStatistics();

        stats.forEach((poolName, stat) -> {
            System.out.println("线程池: " + poolName);
            System.out.println("  核心线程数: " + stat.corePoolSize);
            System.out.println("  最大线程数: " + stat.maximumPoolSize);
            System.out.println("  当前线程数: " + stat.poolSize);
            System.out.println("  活跃线程数: " + stat.activeCount);
            System.out.println("  总任务数: " + stat.taskCount);
            System.out.println("  完成任务数: " + stat.completedTaskCount);
            System.out.println("  队列大小: " + stat.queueSize);
            System.out.println("  使用率: " + stat.utilizationRate + "%");
        });

        // 获取任务执行指标
        ThreadPoolRegistry.ThreadPoolMetrics metrics =
            ThreadPoolRegistry.getMetrics("my-io-pool");

        if (metrics != null) {
            System.out.println("平均执行时间: " + metrics.getAverageExecutionTime() + "ms");
            System.out.println("最大执行时间: " + metrics.getMaxExecutionTime() + "ms");
            System.out.println("最小执行时间: " + metrics.getMinExecutionTime() + "ms");
        }
    }
}
```

#### 4.5 任务包装器

使用任务包装器自动收集执行指标：

```java
@Component
public class TaskWrapperService {

    @Autowired
    private MoonThreadPoolFactory threadPoolFactory;

    public void executeWithMetrics() {
        ThreadPoolExecutor pool = threadPoolFactory.createIoIntensiveThreadPool("metric-pool");

        // 包装任务以收集指标
        Runnable wrappedTask = MetricTaskWrapper.wrap(() -> {
            // 业务逻辑
            System.out.println("执行业务任务");
        }, "metric-pool");

        pool.execute(wrappedTask);
    }
}
```

### 5. 监控端点响应示例

#### 获取所有线程池信息

```json
{
    "total": 3,
    "pools": [
        {
            "poolName": "my-io-pool",
            "corePoolSize": 16,
            "maximumPoolSize": 32,
            "activeCount": 5,
            "poolSize": 16,
            "largestPoolSize": 20,
            "taskCount": 1000,
            "completedTaskCount": 995,
            "queueSize": 10,
            "queueRemainingCapacity": 990,
            "isShutdown": false,
            "isTerminated": false,
            "isTerminating": false,
            "utilizationRate": "15.63%",
            "queueUtilizationRate": "1.00%",
            "status": "RUNNING"
        }
    ]
}
```

### 6. 最佳实践

#### 6.1 线程池命名

使用有意义的名称，便于监控和调试：

```java
// 好的命名
threadPoolFactory.createIoIntensiveThreadPool("order-process-pool");
threadPoolFactory.createCpuIntensiveThreadPool("data-compute-pool");

// 避免的命名
threadPoolFactory.createIoIntensiveThreadPool("pool1");
threadPoolFactory.createCpuIntensiveThreadPool("tp");
```

#### 6.2 选择合适的线程池类型

- **IO 密集型任务**：文件读写、网络请求、数据库操作
- **CPU 密集型任务**：复杂计算、数据处理、加密解密
- **定时任务**：周期性执行的任务
- **混合型任务**：使用自定义线程池，根据实际情况调整参数

#### 6.3 异常处理

```java
public void safeExecute(Runnable task) {
    try {
        pool.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("任务执行失败", e);
                // 业务补偿逻辑
            }
        });
    } catch (RejectedExecutionException e) {
        log.error("任务被拒绝", e);
        // 降级处理
    }
}
```

#### 6.4 资源清理

```java
@PreDestroy
public void cleanup() {
    // 框架会自动关闭注册的线程池
    // 如需手动关闭：
    ThreadPoolRegistry.shutdown("my-pool");
}
```

### 7. 性能优化建议

1. **合理设置线程池大小**
   - IO 密集型：2 * CPU 核数
   - CPU 密集型：CPU 核数 + 1
   - 混合型：根据实际测试调整

2. **选择合适的队列**
   - LinkedBlockingQueue：无界队列，适合任务量不确定
   - ArrayBlockingQueue：有界队列，防止内存溢出
   - SynchronousQueue：直接交接，适合高并发短任务

3. **监控和调优**
   - 定期查看线程池使用率
   - 根据业务峰值调整参数
   - 关注拒绝任务数量

4. **避免线程泄漏**
   - 使用 try-finally 确保资源释放
   - 设置合理的 keepAliveTime
   - 及时关闭不需要的线程池

## 优化说明

本次优化主要包括：

### 代码优化
1. **添加线程工厂**：自定义线程名称、优先级、异常处理
2. **增强线程池注册器**：支持批量操作、指标收集、事件通知
3. **配置属性类**：提供灵活的配置选项
4. **任务包装器**：自动收集执行指标

### 功能增强
1. **主自动配置类**：统一管理所有配置
2. **关闭监听器**：应用关闭时优雅关闭线程池
3. **指标收集**：记录任务执行时间等关键指标
4. **动态调整**：运行时修改线程池参数

### 性能优化
1. **预启动核心线程**：减少首次执行延迟
2. **线程池复用**：通过注册中心统一管理
3. **重试机制**：提高任务执行成功率

## License

MIT License