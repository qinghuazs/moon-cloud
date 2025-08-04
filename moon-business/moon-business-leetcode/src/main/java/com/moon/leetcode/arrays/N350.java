package com.moon.leetcode.arrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class N350 {
    public int[] intersect(int[] nums1, int[] nums2) {
        Arrays.sort(nums1);
        Arrays.sort(nums2);
        int p1 = 0, p2 = 0;
        List<Integer> list = new ArrayList<>();
        while (p1 < nums1.length && p2 < nums2.length) {
            int v1 = nums1[p1], v2 = nums2[p2];
            if (v1 == v2) {
                p1++;
                p2++;
                list.add(v1);
            }
            else if (v1 < v2) {
                p1++;
            }
            else {
                p2++;
            }
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

}
