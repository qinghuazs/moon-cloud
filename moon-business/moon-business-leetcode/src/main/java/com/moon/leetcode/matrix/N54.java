package com.moon.leetcode.matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * 螺旋矩阵
 */
public class N54 {

    public static void main(String[] args) {
        N54 n54 = new N54();
        int[][] matrix = {{1,2,3},{4,5,6},{7,8,9}};
        List<Integer> list = n54.spiralOrder(matrix);
        System.out.println(list);
    }

    public List<Integer> spiralOrder(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int direction = 1;
        List<Integer> list = new ArrayList<>();
        int k = 0;
        while(k < m * n) {
            if(direction == 1) {
                for(int i = 0; i < n; i++) {
                    if (matrix[0][i] == -101) {
                        i--;
                        break;
                    }
                    list.add(matrix[0][i]);
                    matrix[0][i] = -101;

                }
                direction = 2;
            } else if(direction == 2) {
                for(int i = 0; i < m; i++) {
                    if (matrix[i][n - 1] == -101) {
                        i--;
                        break;
                    }
                    list.add(matrix[i][n - 1]);
                    matrix[i][n - 1] = -101;
                }
                direction = 3;
            }
            else if(direction == 3) {
                for(int i = n - 1; i >= 0; i--) {

                    if (matrix[m - 1][i] == -101) {
                        i++;
                        break;
                    }
                    list.add(matrix[m - 1][i]);
                    matrix[m - 1][i] = -101;
                }
                direction = 4;
            } else if(direction == 4) {
                for(int i = m - 1; i >= 0; i--) {
                    if (matrix[i][0] == -101) {
                        i++;
                        break;
                    }
                    list.add(matrix[i][0]);
                    matrix[i][0] = -101;
                }
                direction = 1;
            }
            k++;
        }
        return list;
    }
}
