package com.moon.leetcode.arrays;

import java.util.HashMap;
import java.util.Map;

public class N219 {

    public static void main(String[] args) {
        N219 n219 = new N219();
        int[] nums = {1,2,3,1};
        int k = 3;
        boolean res = n219.containsNearbyDuplicate(nums, k);
        System.out.println(res);
    }

    public boolean containsNearbyDuplicate(int[] nums, int k) {
        if (nums == null || nums.length == 0) {
            return false;
        }
        Map<Integer , Integer> map = new HashMap<>();
        for (int i = 0, j=0; j < nums.length; i++, j++) {
            while (j - i < k) {
                int cur = map.getOrDefault(nums[j], 0) + 1;
                map.putIfAbsent(nums[j], cur);
                if (cur > 1) {
                    return true;
                }
                j++;
            }
            if (j >= nums.length) {
                return false;
            }
            int cur = map.getOrDefault(nums[j], 0) + 1;
            if (cur > 1) {
                return true;
            }
            map.putIfAbsent(nums[j], cur);
            map.remove(nums[i]);


        }
        return false;
    }
}
