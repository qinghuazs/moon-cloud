package com.moon.leetcode.arrays;

/**
 * 有序数组的平方
 */
public class N977 {

    public int[] sortedSquares(int[] nums) {
        int left = 0, right = nums.length - 1;
        int[] res = new int[nums.length];
        int index = nums.length - 1;
        while (left < right) {
            int leftVal = nums[left] * nums[left];
            int rightVal = nums[right] * nums[right];
            if (leftVal > rightVal) {
                res[index] = leftVal;
                left++;
            } else {
                res[index] = rightVal;
                right--;
            }
            index--;
        }
        return res;
    }

    public void squares(int[] nums, int left, int right) {

    }
}
