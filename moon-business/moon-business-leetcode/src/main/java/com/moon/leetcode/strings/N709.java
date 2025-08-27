package com.moon.leetcode.strings;

public class N709 {

    public static void main(String[] args) {
        N709 n709 = new N709();
        String s = n709.toLowerCase("Hello");
        System.out.println(s);
    }

    public String toLowerCase(String s) {
        int n = s.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + 'a' - 'A');
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
