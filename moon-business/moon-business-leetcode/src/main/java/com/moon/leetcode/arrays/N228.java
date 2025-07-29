package com.moon.leetcode.arrays;

import java.util.ArrayList;
import java.util.List;

public class N228 {

    public static void main(String[] args) {
        N228 n228 = new N228();
        int[] nums = {0,1,2,4,5,7};
        List<String> res = n228.summaryRanges(nums);
        System.out.println(res);
    }

    public List<String> summaryRanges(int[] nums) {
        int l = 0, r = 0;
        List<String> res = new ArrayList<>();
        while ( r < nums.length) {
            while ( r + 1 < nums.length && nums[r] + 1 == nums[r + 1]) {
                r++;
            }
            StringBuilder sb = new StringBuilder();
            if (l == r) {
                sb.append(nums[l]);
            } else {
                sb.append(nums[l]).append("->").append(nums[r]);
            }
            l = r + 1;
            r = r + 1;
            res.add(sb.toString());
        }
        return res;
    }
}
