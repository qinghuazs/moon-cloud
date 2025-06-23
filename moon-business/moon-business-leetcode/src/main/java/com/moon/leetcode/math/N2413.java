package com.moon.leetcode.math;

/**
 * 最小偶倍数
 */
public class N2413 {
    public int smallestEvenMultiple(int n) {
        return n % 2 == 0 ? n : n * 2;
    }
}
