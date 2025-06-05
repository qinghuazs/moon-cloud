package com.moon.cloud.concurrent.thread;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * 邮件发送线程管理演示
 * 展示如何正确处理邮件发送线程的启动和终止
 */
public class EmailSendDemo {
    
    public static void main(String[] args) {
        EmailSendThreadManager emailManager = new EmailSendThreadManager();
        
        // 模拟问题账号数据
        List<String> problemAccounts = Arrays.asList(
            "account001@bank.com",
            "account002@bank.com", 
            "account003@bank.com",
            "account004@bank.com",
            "account005@bank.com",
            "account006@bank.com",
            "account007@bank.com",
            "account008@bank.com",
            "account009@bank.com",
            "account010@bank.com"
        );
        
        System.out.println("=== 邮件发送线程管理演示 ===");
        System.out.println("发现 " + problemAccounts.size() + " 个问题账号");
        System.out.println();
        
        // 演示菜单
        showMenu();
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("请选择操作 (输入数字): ");
            String input = scanner.nextLine().trim();
            
            try {
                int choice = Integer.parseInt(input);
                
                switch (choice) {
                    case 1:
                        // 正确的批量发送
                        System.out.println("\n--- 执行正确的批量邮件发送 ---");
                        emailManager.sendBatchEmail(problemAccounts);
                        break;
                        
                    case 2:
                        // 错误的循环发送
                        System.out.println("\n--- 执行错误的循环邮件发送 (问题代码) ---");
                        emailManager.sendEmailsIncorrectly(problemAccounts);
                        break;
                        
                    case 3:
                        // 中断当前任务
                        System.out.println("\n--- 中断当前邮件发送任务 ---");
                        emailManager.interruptCurrentTask();
                        break;
                        
                    case 4:
                        // 查看状态
                        System.out.println("\n--- 当前状态 ---");
                        System.out.println("服务是否关闭: " + emailManager.isShutdown());
                        System.out.println("活跃任务数: " + emailManager.getActiveTaskCount());
                        System.out.println("是否有任务在执行: " + emailManager.hasActiveTask());
                        break;
                        
                    case 5:
                        // 优雅关闭
                        System.out.println("\n--- 优雅关闭邮件发送服务 ---");
                        emailManager.shutdown(10);
                        System.out.println("程序即将退出...");
                        scanner.close();
                        return;
                        
                    case 6:
                        // 显示菜单
                        showMenu();
                        break;
                        
                    default:
                        System.out.println("无效选择，请重新输入");
                }
                
            } catch (NumberFormatException e) {
                System.out.println("请输入有效的数字");
            }
            
            System.out.println();
        }
    }
    
    private static void showMenu() {
        System.out.println("\n=== 操作菜单 ===");
        System.out.println("1. 正确的批量邮件发送 (推荐)");
        System.out.println("2. 错误的循环邮件发送 (问题演示)");
        System.out.println("3. 中断当前邮件发送任务");
        System.out.println("4. 查看当前状态");
        System.out.println("5. 优雅关闭服务并退出");
        System.out.println("6. 显示菜单");
        System.out.println();
    }
    
    /**
     * 演示自动中断场景
     */
    public static void demonstrateAutoInterrupt() {
        System.out.println("\n=== 自动中断演示 ===");
        
        EmailSendThreadManager emailManager = new EmailSendThreadManager();
        List<String> accounts = Arrays.asList("test1@bank.com", "test2@bank.com");
        
        // 启动错误的循环发送
        emailManager.sendEmailsIncorrectly(accounts);
        
        // 等待3秒后自动中断
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
                System.out.println("\n检测到错误的邮件发送模式，自动中断...");
                emailManager.interruptCurrentTask();
                
                TimeUnit.SECONDS.sleep(2);
                System.out.println("重新使用正确的方式发送邮件...");
                emailManager.sendBatchEmail(accounts);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}