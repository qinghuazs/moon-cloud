package com.moon.leetcode.math;

public class N1281 {

    public static void main(String[] args) {
        N1281 n1281 = new N1281();
        int ans = n1281.subtractProductAndSum(234);
        System.out.println(ans);
    }

    public int subtractProductAndSum(int n) {
        int p = 1;
        int sum = 0;
        while (n > 9) {
            p *= n % 10;
            sum += n % 10;
            n = n / 10;
        }
        p *= n;
        sum += n;
        return p - sum;
    }
}
