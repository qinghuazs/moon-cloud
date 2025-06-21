package com.moon.leetcode.arrays;

/**
 * 移除元素
 */
public class N27 {

    public int removeElement(int[] nums, int val) {
       int left = -1, right = 0;
       while (right < nums.length) {
           if (nums[right] != val) {
               left++;
               nums[left] = nums[right];
           }
           right++;
       }
       return left;
    }

    public void remove(int[] nums, int target, int left, int right) {

    }
}
