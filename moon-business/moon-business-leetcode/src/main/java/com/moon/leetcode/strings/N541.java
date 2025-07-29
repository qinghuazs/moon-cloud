package com.moon.leetcode.strings;

public class N541 {

    public String reverseStr(String s, int k) {
        char[] chars = s.toCharArray();
        for (int i=0; i<chars.length; i+=2*k) {
            int left = i;
            int right = Math.min(i+k-1, chars.length-1);
            while (left < right) {
                char temp = chars[left];
                chars[left] = chars[right];
                chars[right] = temp;
                left++;
                right--;
            }
        }
        return new String(chars);
    }
}
