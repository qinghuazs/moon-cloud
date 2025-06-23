package com.moon.leetcode.window;

/**
 * 得到 K 个黑块的最少涂色次数
 */
public class N2379 {

    public int minimumRecolors(String blocks, int k) {
        int sum = 0;
        int max = 0;
        for (int i = 0; i < blocks.length(); i++) {
            if (blocks.charAt(i) == 'B') {
                sum++;
            }
            if (i + 1 < k) {
                continue;
            }
            max = Math.max(max, sum);
            char left = blocks.charAt(i+1-k);
            if (left == 'B') {
                sum--;
            }
        }
        return k - max;
    }
}
