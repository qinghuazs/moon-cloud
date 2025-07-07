package com.moon.leetcode.dp;

public class N746 {

    public static void main(String[] args) {
        N746 n746 = new N746();
        int[] cost = {1,100,1,1,1,100,1,1,100,1};
        System.out.println(n746.minCostClimbingStairs(cost));
    }

    public int minCostClimbingStairs(int[] cost) {
        if (cost.length == 2) {
            return Math.min(cost[0], cost[1]);
        }
        int[] dp = new int[cost.length+1];
        dp[0] = cost[0];
        dp[1] = cost[1];
        for (int i = 2; i <= cost.length; i++) {
            if (i == cost.length) {
                dp[i] = Math.min(dp[i-2], dp[i-1]);
            } else {
                dp[i] = Math.min(dp[i-2] + cost[i], dp[i-1] + cost[i]);
            }
        }
        return dp[cost.length];
    }
}
