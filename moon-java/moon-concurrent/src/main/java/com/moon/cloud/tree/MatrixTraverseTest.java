package com.moon.cloud.tree;

/**
 * Matrix类的测试示例
 */
public class MatrixTraverseTest {
    
    public static void main(String[] args) {
        // 创建一个3x3的矩阵
        Matrix<Integer> matrix = new Matrix<>(4);
        
        // 填充矩阵数据
        int start = 1;
        initializeMatrixData(matrix, start);
        
        System.out.println("\n=== 矩阵内容 ===");
        matrix.printMatrix();

        System.out.println("\n=== 矩阵输出顺序 ===");
        matrix.traverse((node) -> {
            System.out.println(node.getData());
        }, 3, 1);


    }

    private static void initializeMatrixData(Matrix<Integer> matrix, int start) {
        System.out.println(String.format("=== 开始初始化矩阵数据，起始值 %s ===", start));
        for (int i = 0; i < matrix.getSize(); i++) {
            for (int j = 0; j < matrix.getSize(); j++) {
                matrix.setData(i, j, start++);
                matrix.getNode(i, j).setVisited(false);
            }
        }
    }


}