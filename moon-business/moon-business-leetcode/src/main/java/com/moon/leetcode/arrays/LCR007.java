package com.moon.leetcode.arrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LCR007 {

    public static void main(String[] args) {
        //int[] nums = {-1,0,1,2,-1,-4};
        int[] nums = {0,0,0};
        LCR007 lcr007 = new LCR007();
        List<List<Integer>> res = lcr007.threeSum(nums);
        System.out.println(res);
    }

    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        int n = nums.length;
        if (n <= 2) {
            return new ArrayList();
        }
        List<List<Integer>> res = new ArrayList();
        for(int i = 0; i < n; i++) {
            int left = i + 1;
            int right = n - 1;
            if (i > 0 && nums[i] == nums[i-1]) {
                continue;
            }
            while(left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                if (sum == 0) {
                    List<Integer> tmp = new ArrayList(3);
                    tmp.add(nums[i]);
                    tmp.add(nums[left]);
                    tmp.add(nums[right]);
                    res.add(tmp);
                    left++;
                    while(nums[left] == nums[left-1]) {
                        left++;
                    }
                    right--;
                    while(right < n-1 && nums[right] == nums[right+1]) {
                        right--;
                    }
                } else if(sum < 0) {
                    left++;
                    while(nums[left] == nums[left-1]) {
                        left++;
                    }
                } else {
                    right--;
                    while(right < n-1 && nums[right] == nums[right+1]) {
                        right--;
                    }
                }
            }
        }
        return res;
    }
}
