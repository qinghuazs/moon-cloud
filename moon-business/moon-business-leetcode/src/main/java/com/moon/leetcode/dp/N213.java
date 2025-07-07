package com.moon.leetcode.dp;

import java.util.Scanner;

public class N213 {

    public static void main(String[] args) {
        N213 n213 = new N213();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("请输入数组长度: ");
            int n = scanner.nextInt();

            int[] nums = new int[n];
            System.out.print("请输入数组元素(用空格分隔): ");
            for (int i = 0; i < n; i++) {
                nums[i] = scanner.nextInt();
            }

            System.out.println("结果: " + n213.rob(nums));
        }

    }

    public int rob(int[] nums) {
        int n = nums.length;
        if (n == 1) {
            return nums[0];
        }
        if (n == 2) {
            return Math.max(nums[0], nums[1]);
        }
        int num0 = rob(nums, 0, n - 2);
        int num1 = rob(nums, 1, n - 1);
        return Math.max(num0, num1);
    }

    public int rob(int[] nums, int start, int end) {
        int f0 = 0;
        int f1 = 0;
        for (int i = start; i <= end; i++) {
            int max = Math.max(f0 + nums[i], f1);
            f0 = f1;
            f1 = max;
        }
        return f1;
    }
}
