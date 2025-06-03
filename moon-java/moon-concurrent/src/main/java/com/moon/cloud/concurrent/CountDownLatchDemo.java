package com.moon.cloud.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        executor.execute(() -> {
            queryOrders();
            System.out.println("查询订单信息完成！");
            latch.countDown();
        });

        executor.execute(() -> {
            queryUsers();
            System.out.println("查询用户信息完成！");
            latch.countDown();
        });

        executor.execute(() -> {
            queryLogistics();
            System.out.println("查询物流信息完成！");
            latch.countDown();
        });
        latch.await();

       
        // 合并处理结果
        System.out.println("等待查询完成，再干其他事情！");

        // 优雅关闭线程池
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    
    }

    public static List<Object> queryOrders()  {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public static List<Object> queryUsers()  {
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public static List<Object> queryLogistics() {
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }



}
