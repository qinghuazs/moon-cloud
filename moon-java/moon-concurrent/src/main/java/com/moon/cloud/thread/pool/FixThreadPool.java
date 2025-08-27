package com.moon.cloud.thread.pool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FixThreadPool {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future future = executorService.submit(() -> {
            System.out.println(Thread.currentThread().getName());
            int i = 1 / 0;
        });
        executorService.submit(() -> {
            System.out.println(Thread.currentThread().getName());
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }
}
