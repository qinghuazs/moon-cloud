package com.moon.leetcode.strings;

public class N5 {

    public static void main(String[] args) {
        N5 n5 = new N5();
        String s = "bb";
        System.out.println(n5.longestPalindrome(s));
    }

    public String longestPalindrome(String s) {
        int n = s.length();
        int left = 0;
        int right = 0;
        int maxLen = 0;
        for(int i = 0; i < 2 * n - 1; i++) {
            int l = i / 2;
            int r = (i + 1) / 2;
            while(l >= 0 && r < n && s.charAt(l) == s.charAt(r)) {
                l--;
                r++;
            }
            if (maxLen < r - l + 1) {
                maxLen = r - l + 1;
                left = l + 1;
                right = r - 1;
            }
        }
        return s.substring(left, right + 1);
    }
}
