package com.moon.cloud.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableThread implements Callable<Integer> {

    private int capcity;

    @Override
    public Integer call() throws Exception {
        return capcity;
    }

    public static void main(String[] args) {
        CallableThread callableThread = new CallableThread();
        callableThread.capcity = 100;
        FutureTask<Integer> futureTask = new FutureTask<>(callableThread);
        Thread t  =new Thread(futureTask);
        t.start();
        try {
            System.out.println(futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
