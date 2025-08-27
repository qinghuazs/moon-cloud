package com.moon.leetcode.strings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class N340 {

    public int length_of_longest_substring_k_distinct(String s, int k) {
        int n = s.length();
        if (n < k) {
            return n;
        }
        int left = 0, right = 0;
        int ans = 0;
        Map<Character, Integer> map = new HashMap<>();
        while (right < n) {
            map.put(s.charAt(right), right);
            if (map.size() > k) {
                int minIndex = Collections.min(map.values());
                left = minIndex + 1;
                map.remove(s.charAt(minIndex));
            }
            ans = Math.max(ans, right - left + 1);
            right++;
        }
        return ans;
    }
}
