package com.moon.leetcode.math;

public class N231 {

    public static void main(String[] args) {
        N231 n231 = new N231();
        boolean ans = n231.isPowerOfTwo(16);
        System.out.println(ans);
    }

    public boolean isPowerOfTwo(int n) {
        if (n < 2) {
            return n == 1;
        }

        while(n > 1) {
            n = n / 2 ;
        }
        return n == 1;
    }
}
