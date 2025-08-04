package com.moon.leetcode.arrays;

public class N414 {

    public int thirdMax(int[] nums) {
        int n = nums.length;
        if (n <= 2) {
            return -1;
        }
        int first = nums[0] > nums[1] ? nums[0] : nums[1];
        int third = nums[0] > nums[1] ? nums[1] : nums[0];

        for (int i = 2; i < n; i++) {
            if (nums[i] > first) {
                return first;
            }
            else if (nums[i] < third) {
                return third;
            }
            else if (nums[i] > third && nums[i] < first) {
                return nums[i];
            }
        }
        return -1;
    }
}
