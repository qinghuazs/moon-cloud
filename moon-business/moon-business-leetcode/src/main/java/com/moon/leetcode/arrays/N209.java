package com.moon.leetcode.arrays;

/**
 * 长度最小的子数组
 */
public class N209 {

    public int minSubArrayLen(int target, int[] nums) {
        int left = 0;
        int right = 0;
        int sum = 0;
        int ans = Integer.MAX_VALUE;
        while (right < nums.length) {
            sum += nums[right];
            while (sum >= target && left < right) {
                ans = Math.min(ans, right - left + 1);
                sum -= nums[left];
                left++;
            }
            right++;
        }
        return ans == Integer.MAX_VALUE ? 0 : ans;
    }
}
