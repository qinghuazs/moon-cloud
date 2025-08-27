package com.moon.leetcode.strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 滑动窗口
 */
public class N3 {

    private static final ThreadLocal<Object> threadLocal = ThreadLocal.withInitial(() -> new Object());

    public static void main(String[] args) {
        String s = "a";
        List<String> list = new ArrayList<>();
        list.add("a");//1
        list.add("abcabcbb");//3
        list.add("abcadcbb");//4
        list.add("bbb");//1
        list.add("");//0
        N3 n3 = new N3();
//        int ans = n3.lengthOfLongestSubstring(s);
//        System.out.println(ans);
        list.stream().forEach(item -> System.out.println(n3.lengthOfLongestSubstring2(item)));
    }

    /**
     * 无重复字符的最长子串长度
     * @param s
     * @return
     */
    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        Map<Character, Integer> map = new HashMap<>();
        int ans = 0;
        int left = 0, right = 0;
        while (right < s.length()) {
            char c = s.charAt(right);
            if (map.containsKey(c)) {
                int index = map.get(c);
                if (index >= left) {
                    left = index + 1;
                    ans = Math.max(ans, right - left + 1);
                    map.put(c, right);
                }
            } else {
                map.put(c, right);
                ans = Math.max(ans, right - left + 1);
            }
            right++;
        }
        return ans;
    }

    public int lengthOfLongestSubstring2(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        Map<Character, Integer> map = new HashMap<>();
        int ans = 0;
        int left =0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (map.containsKey(c)) {
                int index = map.get(c);
                left = Math.max(left, index + 1);
            }
            map.put(c, i);
            ans = Math.max(ans, i - left + 1);
        }
        return ans;

    }
}
