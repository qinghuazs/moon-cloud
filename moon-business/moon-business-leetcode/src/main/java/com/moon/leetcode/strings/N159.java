package com.moon.leetcode.strings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class N159 {

    public static void main(String[] args) {
        N159 n159 = new N159();
        System.out.println(n159.length_of_longest_substring_two_distinct("abaccc"));
        System.out.println(n159.length_of_longest_substring_two_distinct("abadcc"));
        System.out.println(n159.length_of_longest_substring_two_distinct("eaedbc"));
    }

    public int length_of_longest_substring_two_distinct(String s) {
        int n = s.length();
        if (n < 3) {
            return n;
        }
        int left = 0, right = 0;
        int ans = 0;
        Map<Character, Integer> map = new HashMap<>();
        while (right < n) {
            map.put(s.charAt(right), right);
            if (map.size() > 2) {
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
