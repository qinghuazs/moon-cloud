package com.moon.leetcode.window;

/**
 * 大小为 K 且平均值大于等于阈值的子数组数目
 */
public class N1343 {

    public int numOfSubarrays(int[] arr, int k, int threshold) {
        int sum = 0;
        int res = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
            if (i + 1 < k) {
                continue;
            }
            if (sum >= threshold * k) {
                res++;
            }
            sum -= arr[i-k+1];
        }
        return res;
    }
}
