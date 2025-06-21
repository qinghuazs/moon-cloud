package com.moon.leetcode.arrays;

/**
 * 二分查找
 */
public class N704 {

    public int search(int[] nums, int target) {
        return midSearch(nums, target, 0, nums.length - 1);
    }

    public int midSearch(int[] nums, int target, int left, int right) {
        if (left > right) {
            return -1;
        }
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) {
            return mid;
        }
        else if (nums[mid] > target) {
            return midSearch(nums, target, left, mid - 1);
        }
        else {
            return midSearch(nums, target, mid + 1, right);
        }
    }
}
