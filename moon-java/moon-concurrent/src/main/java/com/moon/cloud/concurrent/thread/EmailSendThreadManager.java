package com.moon.cloud.concurrent.thread;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 邮件发送线程管理器
 * 解决邮件发送逻辑中的线程控制问题
 * 
 * 问题场景：原本应该发送一封包含10个问题账号的邮件，
 * 但代码错误导致每个账号发送一封邮件
 */
public class EmailSendThreadManager {
    
    private final ExecutorService emailExecutor;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private volatile Future<?> currentEmailTask;
    
    public EmailSendThreadManager() {
        // 创建单线程执行器，确保邮件发送的顺序性
        this.emailExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "email-sender-thread");
            thread.setDaemon(false);
            return thread;
        });
    }
    
    /**
     * 正确的邮件发送方式：批量发送
     * @param problemAccounts 问题账号列表
     */
    public void sendBatchEmail(List<String> problemAccounts) {
        if (isShutdown.get()) {
            System.out.println("邮件发送服务已关闭，无法发送邮件");
            return;
        }
        
        currentEmailTask = emailExecutor.submit(() -> {
            activeTasks.incrementAndGet();
            try {
                // 检查是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("邮件发送任务被中断");
                    return;
                }
                
                System.out.println("开始发送批量邮件，包含 " + problemAccounts.size() + " 个问题账号");
                
                // 构建邮件内容
                StringBuilder emailContent = new StringBuilder();
                emailContent.append("以下是检测到的问题银行账号：\n\n");
                
                for (int i = 0; i < problemAccounts.size(); i++) {
                    // 检查是否被中断
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("邮件内容构建过程中被中断");
                        return;
                    }
                    
                    emailContent.append(String.format("%d. 账号: %s\n", i + 1, problemAccounts.get(i)));
                }
                
                // 模拟发送邮件
                sendSingleEmail("问题账号汇总报告", emailContent.toString());
                
                System.out.println("批量邮件发送完成");
                
            } catch (InterruptedException e) {
                System.out.println("邮件发送任务被中断: " + e.getMessage());
                Thread.currentThread().interrupt(); // 恢复中断状态
            } catch (Exception e) {
                System.err.println("邮件发送失败: " + e.getMessage());
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }
    
    /**
     * 错误的邮件发送方式：循环发送（问题代码示例）
     * @param problemAccounts 问题账号列表
     */
    public void sendEmailsIncorrectly(List<String> problemAccounts) {
        if (isShutdown.get()) {
            System.out.println("邮件发送服务已关闭，无法发送邮件");
            return;
        }
        
        // 这是错误的实现方式 - 每个账号发送一封邮件
        for (String account : problemAccounts) {
            currentEmailTask = emailExecutor.submit(() -> {
                activeTasks.incrementAndGet();
                try {
                    // 检查是否被中断
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("邮件发送任务被中断");
                        return;
                    }
                    
                    String subject = "问题账号通知: " + account;
                    String content = "检测到问题账号: " + account;
                    sendSingleEmail(subject, content);
                    
                } catch (InterruptedException e) {
                    System.out.println("邮件发送任务被中断: " + e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("邮件发送失败: " + e.getMessage());
                } finally {
                    activeTasks.decrementAndGet();
                }
            });
        }
    }
    
    /**
     * 模拟发送单封邮件
     */
    private void sendSingleEmail(String subject, String content) throws InterruptedException {
        System.out.println("正在发送邮件...");
        System.out.println("主题: " + subject);
        System.out.println("内容: " + content.substring(0, Math.min(50, content.length())) + "...");
        
        // 模拟邮件发送耗时
        for (int i = 0; i < 5; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("邮件发送过程中被中断");
            }
            Thread.sleep(1000); // 模拟网络延迟
            System.out.println("发送进度: " + ((i + 1) * 20) + "%");
        }
        
        System.out.println("邮件发送成功!");
    }
    
    /**
     * 立即中断当前正在执行的邮件发送任务
     */
    public void interruptCurrentTask() {
        if (currentEmailTask != null && !currentEmailTask.isDone()) {
            System.out.println("正在中断当前邮件发送任务...");
            boolean cancelled = currentEmailTask.cancel(true); // true表示允许中断正在执行的任务
            if (cancelled) {
                System.out.println("邮件发送任务已成功中断");
            } else {
                System.out.println("邮件发送任务中断失败，可能已经完成");
            }
        } else {
            System.out.println("当前没有正在执行的邮件发送任务");
        }
    }
    
    /**
     * 优雅关闭邮件发送服务
     * @param timeoutSeconds 等待超时时间（秒）
     */
    public void shutdown(long timeoutSeconds) {
        if (isShutdown.compareAndSet(false, true)) {
            System.out.println("开始关闭邮件发送服务...");
            
            // 先中断当前任务
            interruptCurrentTask();
            
            // 关闭线程池
            emailExecutor.shutdown();
            
            try {
                // 等待现有任务完成
                if (!emailExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    System.out.println("等待超时，强制关闭邮件发送服务");
                    emailExecutor.shutdownNow();
                    
                    // 再次等待
                    if (!emailExecutor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                        System.err.println("邮件发送服务无法正常关闭");
                    }
                }
                System.out.println("邮件发送服务已关闭");
            } catch (InterruptedException e) {
                System.err.println("关闭过程被中断");
                emailExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 获取当前活跃任务数
     */
    public int getActiveTaskCount() {
        return activeTasks.get();
    }
    
    /**
     * 检查服务是否已关闭
     */
    public boolean isShutdown() {
        return isShutdown.get();
    }
    
    /**
     * 检查当前是否有任务在执行
     */
    public boolean hasActiveTask() {
        return currentEmailTask != null && !currentEmailTask.isDone();
    }
}