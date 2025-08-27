package com.moon.resume;

public class BalanceFulcrum {

    public static void main(String[] args) {
        BalanceFulcrum balanceFulcrum = new BalanceFulcrum();
        //int[] lever = {2, 5, 10, 5, 2};
        int[] lever = {3, 6, 1, 2, 2, 2};
        int res = balanceFulcrum.findBalanceFulcrum(lever);
        System.out.println(res);
    }

    /**
     * 代码中的类名、方法名、参数名已经指定，请勿修改，直接返回方法规定的值即可
     *
     *
     * @param lever int整型一维数组
     * @return int整型
     */
    public int findBalanceFulcrum (int[] lever) {
        if (lever == null || lever.length <= 2) {
            return -1;
        }
        for (int i = 1; i < lever.length - 1; i++) {
            int left = leftSum(lever, i);
            int right = rightSum(lever, i);
            if (left == right) {
                return i;
            }
        }

        return -1;
    }

    public int leftSum(int[] lever, int mid) {
        int sum = 0;
        for (int i = 0; i < mid; i++) {
            sum += lever[i] * (mid - i);
        }
        return sum;
    }

    public int rightSum(int[] lever, int mid) {
        int sum = 0;
        for (int i = mid + 1; i < lever.length; i++) {
            sum += lever[i] * (i - mid);
        }
        return sum;
    }
}
