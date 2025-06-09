package com.moon.cloud.tree;

/**
 * Matrix类的测试示例
 */
public class MatrixTest {
    
    public static void main(String[] args) {
        // 创建一个3x3的矩阵
        Matrix<Integer> matrix = new Matrix<>(3);
        
        // 填充矩阵数据
        int start = 1;
        initializeMatrixData(matrix, start);
        
        System.out.println("\n=== 矩阵内容 ===");
        matrix.printMatrix();
        
        // 测试节点连接
        System.out.println("\n=== 测试节点连接 ===");
        Node<Integer> centerNode = matrix.getNode(1, 1);
        System.out.println("中心节点(1,1)的值: " + centerNode.getData());
        System.out.println("上方节点的值: " + (centerNode.getUp() != null ? centerNode.getUp().getData() : "null"));
        System.out.println("下方节点的值: " + (centerNode.getDown() != null ? centerNode.getDown().getData() : "null"));
        System.out.println("左侧节点的值: " + (centerNode.getLeft() != null ? centerNode.getLeft().getData() : "null"));
        System.out.println("右侧节点的值: " + (centerNode.getRight() != null ? centerNode.getRight().getData() : "null"));
        
        // 测试边界节点
        System.out.println("\n=== 测试边界节点 ===");
        Node<Integer> topLeftNode = matrix.getTopLeft();
        System.out.println("左上角节点(0,0)的值: " + topLeftNode.getData());
        System.out.println("左上角节点的上方: " + (topLeftNode.getUp() != null ? topLeftNode.getUp().getData() : "null"));
        System.out.println("左上角节点的左侧: " + (topLeftNode.getLeft() != null ? topLeftNode.getLeft().getData() : "null"));
        
        Node<Integer> bottomRightNode = matrix.getBottomRight();
        System.out.println("右下角节点的值: " + bottomRightNode.getData());
        System.out.println("右下角节点的下方: " + (bottomRightNode.getDown() != null ? bottomRightNode.getDown().getData() : "null"));
        System.out.println("右下角节点的右侧: " + (bottomRightNode.getRight() != null ? bottomRightNode.getRight().getData() : "null"));
        
        // 使用访问器遍历矩阵
        System.out.println("\n=== 使用访问器遍历矩阵 ===");
        matrix.traverse((node, row, col) -> {
            System.out.println("位置(" + row + "," + col + "): " + node.getData());
        });
        
        // 测试字符串类型矩阵
        System.out.println("\n=== 测试字符串类型矩阵 ===");
        Matrix<String> stringMatrix = new Matrix<>(2);
        stringMatrix.setData(0, 0, "A");
        stringMatrix.setData(0, 1, "B");
        stringMatrix.setData(1, 0, "C");
        stringMatrix.setData(1, 1, "D");
        
        stringMatrix.printMatrix();
        
        // 通过节点链接遍历
        System.out.println("\n=== 通过节点链接遍历 ===");
        Node<String> current = stringMatrix.getTopLeft();
        System.out.println("从左上角开始，向右遍历第一行:");
        while (current != null) {
            System.out.print(current.getData() + " ");
            current = current.getRight();
        }
        System.out.println();
        
        current = stringMatrix.getTopLeft();
        System.out.println("从左上角开始，向下遍历第一列:");
        while (current != null) {
            System.out.print(current.getData() + " ");
            current = current.getDown();
        }
        System.out.println();
    }

    private static void initializeMatrixData(Matrix<Integer> matrix, int start) {
        System.out.println(String.format("=== 开始初始化矩阵数据，起始值 %s ===", start));
        for (int i = 0; i < matrix.getSize(); i++) {
            for (int j = 0; j < matrix.getSize(); j++) {
                matrix.setData(i, j, start++);
            }
        }
    }
}