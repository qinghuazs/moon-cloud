# 邮件发送线程管理解决方案

## 问题描述

在银行账号监控系统中，当检测到问题账号时需要给用户发送邮件通知。原本的设计是：
- **期望行为**：如果有10个问题账号，发送1封邮件，邮件中包含所有10个账号的信息
- **实际问题**：代码错误导致每个账号发送1封邮件，总共发送10封邮件

当这种错误的循环发送邮件逻辑正在执行时，需要有办法及时终止发送线程。

## 解决方案

### 1. 线程管理策略

#### 使用单线程执行器
```java
// 创建单线程执行器，确保邮件发送的顺序性
this.emailExecutor = Executors.newSingleThreadExecutor(r -> {
    Thread thread = new Thread(r, "email-sender-thread");
    thread.setDaemon(false);
    return thread;
});
```

#### 任务状态跟踪
```java
private final AtomicBoolean isShutdown = new AtomicBoolean(false);
private final AtomicInteger activeTasks = new AtomicInteger(0);
private volatile Future<?> currentEmailTask;
```

### 2. 正确的邮件发送方式

```java
public void sendBatchEmail(List<String> problemAccounts) {
    currentEmailTask = emailExecutor.submit(() -> {
        // 构建包含所有账号的邮件内容
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("以下是检测到的问题银行账号：\n\n");
        
        for (int i = 0; i < problemAccounts.size(); i++) {
            // 检查是否被中断
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            emailContent.append(String.format("%d. 账号: %s\n", i + 1, problemAccounts.get(i)));
        }
        
        // 发送一封包含所有账号的邮件
        sendSingleEmail("问题账号汇总报告", emailContent.toString());
    });
}
```

### 3. 线程中断机制

#### 立即中断当前任务
```java
public void interruptCurrentTask() {
    if (currentEmailTask != null && !currentEmailTask.isDone()) {
        boolean cancelled = currentEmailTask.cancel(true); // true表示允许中断正在执行的任务
        if (cancelled) {
            System.out.println("邮件发送任务已成功中断");
        }
    }
}
```

#### 响应中断的任务实现
```java
private void sendSingleEmail(String subject, String content) throws InterruptedException {
    // 模拟邮件发送过程中检查中断
    for (int i = 0; i < 5; i++) {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("邮件发送过程中被中断");
        }
        Thread.sleep(1000); // 模拟网络延迟
    }
}
```

### 4. 优雅关闭机制

```java
public void shutdown(long timeoutSeconds) {
    // 1. 设置关闭标志
    isShutdown.compareAndSet(false, true);
    
    // 2. 中断当前任务
    interruptCurrentTask();
    
    // 3. 关闭线程池
    emailExecutor.shutdown();
    
    // 4. 等待任务完成或超时
    if (!emailExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
        emailExecutor.shutdownNow(); // 强制关闭
    }
}
```

## 使用示例

### 基本使用

```java
// 创建邮件发送管理器
EmailSendThreadManager emailManager = new EmailSendThreadManager();

// 问题账号列表
List<String> problemAccounts = Arrays.asList(
    "account001@bank.com", "account002@bank.com", "account003@bank.com"
);

// 正确的批量发送
emailManager.sendBatchEmail(problemAccounts);

// 如果需要中断
emailManager.interruptCurrentTask();

// 程序结束时优雅关闭
emailManager.shutdown(10);
```

### 运行演示程序

```bash
# 编译
javac -cp . com/moon/cloud/concurrent/thread/*.java

# 运行演示
java com.moon.cloud.concurrent.thread.EmailSendDemo
```

## 关键特性

### 1. 线程安全
- 使用 `AtomicBoolean` 和 `AtomicInteger` 确保状态的线程安全
- 使用 `volatile` 关键字确保 `currentEmailTask` 的可见性

### 2. 中断响应
- 任务执行过程中定期检查 `Thread.currentThread().isInterrupted()`
- 使用 `Future.cancel(true)` 支持中断正在执行的任务

### 3. 资源管理
- 提供优雅关闭机制，避免资源泄露
- 支持超时等待和强制关闭

### 4. 状态监控
- 提供活跃任务数查询
- 支持服务状态检查

## 最佳实践

### 1. 邮件发送设计原则
- **批量优于单个**：尽量将相关信息合并到一封邮件中
- **避免循环发送**：检查业务逻辑，确保不会产生重复邮件
- **设置发送频率限制**：防止邮件轰炸

### 2. 线程管理原则
- **可中断设计**：长时间运行的任务应该支持中断
- **状态跟踪**：维护任务执行状态，便于监控和管理
- **优雅关闭**：程序退出时正确关闭线程池

### 3. 错误处理
- **异常捕获**：妥善处理邮件发送过程中的异常
- **重试机制**：对于临时性错误，可以考虑重试
- **日志记录**：记录邮件发送的成功和失败情况

## 扩展功能

### 1. 邮件发送队列
可以扩展为支持邮件队列的版本：

```java
public class EmailQueueManager {
    private final BlockingQueue<EmailTask> emailQueue = new LinkedBlockingQueue<>();
    private final ExecutorService emailProcessor;
    
    public void addEmailTask(EmailTask task) {
        emailQueue.offer(task);
    }
    
    // 队列处理逻辑...
}
```

### 2. 邮件发送限流
防止邮件发送过于频繁：

```java
public class RateLimitedEmailSender {
    private final RateLimiter rateLimiter = RateLimiter.create(1.0); // 每秒1封邮件
    
    public void sendEmail(String subject, String content) {
        rateLimiter.acquire(); // 获取许可
        // 发送邮件逻辑...
    }
}
```

### 3. 邮件模板管理
使用模板引擎生成邮件内容：

```java
public class EmailTemplateManager {
    public String generateBatchAccountEmail(List<String> accounts) {
        // 使用模板引擎生成邮件内容
        return templateEngine.process("batch-account-template", 
            Map.of("accounts", accounts, "timestamp", new Date()));
    }
}
```

## 总结

通过 `EmailSendThreadManager`，我们解决了以下问题：

1. **业务逻辑错误**：从循环发送改为批量发送
2. **线程控制**：提供了中断正在执行的邮件发送任务的能力
3. **资源管理**：确保线程池能够优雅关闭
4. **状态监控**：提供了任务状态查询功能

这个解决方案不仅解决了当前的问题，还为未来的扩展提供了良好的基础。