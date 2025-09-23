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

        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        int i = 0;
        int j = 0;
        List<Integer> list = new ArrayList<>();
        int direction = 0;
        for(int k = 0; k < n * m; k++) {
            list.add(matrix[i][j]);
            matrix[i][j] = -101;
            int nextI = i + dirs[direction][0];
            int nextJ = j + dirs[direction][1];
            if(nextI < 0 || nextI >= m || nextJ < 0 || nextJ >= n || matrix[nextI][nextJ] == -101) {
                direction = (direction+1) % 4;
            }
            i += dirs[direction][0];
            j += dirs[direction][1];
        }
        return list;
    }
}
