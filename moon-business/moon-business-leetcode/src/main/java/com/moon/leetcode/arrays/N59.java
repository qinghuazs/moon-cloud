package com.moon.leetcode.arrays;

/**
 * 螺旋矩阵2
 */
public class N59 {

    public int[][] generateMatrix(int n) {
        int[][] matrix = new int[n][n];
        int i = 0, j = 0;
        int direction = 0;
        for (int k = 1; k <= n * n; k++) {
            matrix[i][j] = k;
            int x = i;
            int y = j;
            if (direction == 0) {
                y++;
            } else if (direction == 1) {
                x++;
            } else if (direction == 2) {
                y--;
            } else if (direction == 3) {
                x--;
            }
            //不等于0代表被访问过，则转变方向,或者到头了,也变方向
            if ( x >= n || y >= n || x < 0 || y <0 || matrix[x][y] != 0) {
                direction = (direction + 1) % 4;
            }
            if (direction == 0) {
                j++;
            } else if (direction == 1) {
                i++;
            } else if (direction == 2) {
                j--;
            } else if (direction == 3) {
                i--;
            }
        }
        return matrix;
    }
}
