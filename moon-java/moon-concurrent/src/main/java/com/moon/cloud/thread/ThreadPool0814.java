package com.moon.cloud.thread;

import java.util.Objects;
import java.util.concurrent.*;

public class ThreadPool0814 {


    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                10,
                10,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.AbortPolicy()
        );

        Future future = threadPoolExecutor.submit(() -> {
            System.out.println("hello world");
        });
        Future future2 = threadPoolExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return "hello world future2";
            }
        });
        try {
            Object o = future.get();
            System.out.println(o);

            Object o2 = future2.get();
            System.out.println(o2);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
