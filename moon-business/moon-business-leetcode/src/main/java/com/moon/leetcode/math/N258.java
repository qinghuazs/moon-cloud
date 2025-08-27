package com.moon.leetcode.math;

public class N258 {

    public static void main(String[] args) {
        N258 n258 = new N258();
        int ans = n258.addDigits(10);
        System.out.println(ans);
    }

    public int addDigits(int num) {
        int ans = 0;
        while(num >= 10) {
            ans += num % 10;
            num = num / 10;
        }
        ans += num;
        if (ans > 10) {
            ans = addDigits(ans);
        }
        return ans;
    }
}
