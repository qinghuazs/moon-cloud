package com.moon.cloud.thread;

import java.util.concurrent.locks.LockSupport;

/**
 * 两个线程循环打印 1~100
 */
public class Num123 {

    private static Thread t2;
    private static Thread t1;

    public static void main(String[] args) throws InterruptedException {
        t1 = new Thread(() -> {
            for (int i = 0; i < 100; i+=2) {
                System.out.println(Thread.currentThread().getName() + ":" + i);
                LockSupport.unpark(t2);
                LockSupport.park();
            }
        });
        t2 = new Thread(() -> {
            for (int i = 1; i < 100; i+=2) {
                System.out.println(Thread.currentThread().getName() + ":" + i);
                LockSupport.unpark(t1);
                LockSupport.park();
            }
        });
        t1.start();
        Thread.currentThread().sleep(10);
        t2.start();
    }
}
