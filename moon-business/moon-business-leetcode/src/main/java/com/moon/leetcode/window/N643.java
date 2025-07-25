package com.moon.leetcode.window;

/**
 * 子数组最大平均数 I
 */
public class N643 {

    public static void main(String[] args) {
        int[] nums = {1,12,-5,-6,50,3};
        int k = 4;
        System.out.println(new N643().findMaxAverage(nums, 4));
    }

    public double findMaxAverage(int[] nums, int k) {
        int sum = 0;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i];
            if (i + 1 < k) {
                continue;
            }
            max = Math.max(max, sum);
            sum -= nums[i-k+1];
        }
        return max * 1.0 / k;
    }
}
