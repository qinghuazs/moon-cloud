package com.moon.resume.alicloud;

import java.util.concurrent.locks.LockSupport;

public class Solution0916001 {

    Thread t1;
    Thread t2;


    public static void main(String[] args) {
        Solution0916001 solution = new Solution0916001();
        solution.print();
    }


    public void print() {
        t1 = new Thread(
                new Runnable() {
                    public void run() {
                        int i = 0;
                        while(i <= 50) {
                            System.out.println(i * 2);
                            i++;
                            LockSupport.unpark(t2);
                            LockSupport.park();
                        }
                    }
                }
        );

        t2 = new Thread(
                new Runnable() {
                    public void run() {
                        int i = 0;
                        while(i <= 50) {
                            System.out.println(i * 2 + 1);
                            i++;
                            LockSupport.unpark(t1);
                            LockSupport.park();
                        }
                    }
                }
        );
        t1.start();
        t2.start();
    }
}
