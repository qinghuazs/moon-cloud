package com.moon.leetcode.strings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class n992 {

    public int subarraysWithKDistinct(int[] nums, int k) {
        int n = nums.length;
        if (n < k) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < n; i++) {
            if (n - i < k) {
                continue;
            }
            Map<Integer, Integer> map = new HashMap<>();
            int left = i;
            int right = i;
            int ans = 0;
            while(right < n) {
                map.put(nums[right], right);
                int size = map.size();
                if (size > k) {
                    left = map.get(Collections.min(map.values())) + 1;
                    ans = Math.max(ans, right - left + 1);
                } else if (size == k) {
                    ans += 1;
                }
                right++;
            }
            sum += ans;
        }
        return sum;
    }
}
