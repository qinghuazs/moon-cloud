package com.moon.cloud.concurrent.lock;

import java.util.concurrent.locks.LockSupport;

public class LockSupportDemo {
    private static Thread thread1;
    private static Thread thread2;
    private static int counter = 1; // 共享计数器
    private static final Object lock = new Object(); // 用于同步计数器的锁

    public static void main(String[] args) {
        // 创建第一个线程
        thread1 = new Thread(() -> {
            while (true) {
                synchronized (lock) {
                    if (counter > 100) break;
                    System.out.println(Thread.currentThread().getName() + ": " + counter++);
                }
                // 唤醒另一个线程
                LockSupport.unpark(thread2);
                // 当前线程阻塞
                LockSupport.park();
            }
        }, "线程1");

        // 创建第二个线程
        thread2 = new Thread(() -> {
            while (true) {
                // 先阻塞，等待t1唤醒
                LockSupport.park();
                synchronized (lock) {
                    if (counter > 100) break;
                    System.out.println(Thread.currentThread().getName() + ": " + counter++);
                }
                // 唤醒t1线程
                LockSupport.unpark(thread1);
            }
        }, "线程2");
        
        // 启动线程
        thread1.start();
        thread2.start();
        
        // 等待线程结束
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
