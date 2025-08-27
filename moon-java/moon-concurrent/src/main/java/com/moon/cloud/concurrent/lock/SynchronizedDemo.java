package com.moon.cloud.concurrent.lock;

public class SynchronizedDemo {

    private int sum1 = 0;
    private int sum2 = 0;
    private Integer lock1 = Integer.valueOf(1);
    private Integer lock2 = Integer.valueOf(2);

    public void plus(int sum1, int sum2) throws InterruptedException {
        synchronized (lock1) {
            this.sum1 += sum1;
        }
        synchronized (lock2) {
            this.sum2 += sum2;
        }
    }

    public static void main(String[] args) {
        SynchronizedDemo demo = new SynchronizedDemo();
        Thread thread1 = new Thread(() -> {
            try {
                demo.plus(1, 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                demo.plus(1, 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(demo.sum1);
        System.out.println(demo.sum2);
    }

}
