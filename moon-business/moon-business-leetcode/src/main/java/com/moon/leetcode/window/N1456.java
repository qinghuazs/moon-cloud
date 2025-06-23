package com.moon.leetcode.window;

/**
 * 定长子串中元音的最大数目
 */
public class N1456 {

    public static void main(String[] args) {
        N1456 t = new N1456();
        System.out.println(t.maxVowels("abciiidef", 3));
    }

    public int maxVowels(String s, int k) {
        int left = 0;
        int right = 0;
        int ans = 0;
        int max = 0;
        while(right < s.length()) {
            char c = s.charAt(right);
            if (right - left + 1 < k) {
                if(isVowel(c)) {
                    ans++;
                    max = Math.max(ans, max);
                }
                right++;
                continue;
            }
            if(isVowel(c)) {
                ans++;
                max = Math.max(ans, max);
            }
            char leftc = s.charAt(left);
            if (isVowel(leftc)) {
                ans--;
            }
            left++;
            right++;
        }   
        return max;
    }

    public boolean isVowel(char c) {
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
    }
}
