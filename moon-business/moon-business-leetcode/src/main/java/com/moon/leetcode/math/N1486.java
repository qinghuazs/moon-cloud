package com.moon.leetcode.math;

/**
 * 数组异或操作
 */
public class N1486 {

    public int xorOperation(int n, int start) {
        int res = start;
        for (int i = 1; i< n; i++) {
            res = res ^ (start + 2 * i);
        }
        return res;
    }
}
