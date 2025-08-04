package com.moon.leetcode.arrays;

import java.util.HashSet;
import java.util.Set;

public class N349 {

    public int[] intersection(int[] nums1, int[] nums2) {
        Set<Integer> set = new HashSet();
        for(int i=0; i<nums1.length; i++) {
            set.add(nums1[i]);
        }
        Set<Integer> list = new HashSet();
        for(int i=0; i<nums2.length; i++) {
            if (set.contains(nums2[i])) {
                list.add(nums2[i]);
            }
        }
        int[] res = new int[list.size()];
        int i = 0;
        for (Integer integer : list) {
            res[i++] = integer;
        }
        return res;
    }
}
