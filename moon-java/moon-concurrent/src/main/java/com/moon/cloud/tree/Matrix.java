package com.moon.cloud.tree;

/**
 * N*N矩阵类，基于四向链表Node实现
 * @param <T> 数据类型
 */
public class Matrix<T> {
    private Node<T>[][] matrix;
    private int size;
    private Node<T> topLeft; // 矩阵左上角节点

    /**
     * 构造一个N*N的矩阵
     * @param size 矩阵大小
     */
    public Matrix(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("矩阵大小必须大于0");
        }
        this.size = size;
        this.matrix = new Node[size][size];
        initializeMatrix();
        connectNodes();
    }

    /**
     * 初始化矩阵中的所有节点
     */
    private void initializeMatrix() {
        System.out.println(String.format("=== 开始初始化%s x %s 矩阵 ===", size, size));
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = new Node<>(null);
            }
        }
        topLeft = matrix[0][0];
    }

    /**
     * 连接所有节点的四向链接
     */
    private void connectNodes() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Node<T> current = matrix[i][j];
                
                // 连接上方节点
                if (i > 0) {
                    current.setUp(matrix[i - 1][j]);
                }
                
                // 连接下方节点
                if (i < size - 1) {
                    current.setDown(matrix[i + 1][j]);
                }
                
                // 连接左侧节点
                if (j > 0) {
                    current.setLeft(matrix[i][j - 1]);
                }
                
                // 连接右侧节点
                if (j < size - 1) {
                    current.setRight(matrix[i][j + 1]);
                }
            }
        }
    }

    /**
     * 获取指定位置的节点
     * @param row 行索引
     * @param col 列索引
     * @return 节点
     */
    public Node<T> getNode(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IndexOutOfBoundsException("索引超出矩阵范围");
        }
        return matrix[row][col];
    }

    /**
     * 设置指定位置节点的数据
     * @param row 行索引
     * @param col 列索引
     * @param data 数据
     */
    public void setData(int row, int col, T data) {
        getNode(row, col).setData(data);
    }

    /**
     * 获取指定位置节点的数据
     * @param row 行索引
     * @param col 列索引
     * @return 数据
     */
    public T getData(int row, int col) {
        return getNode(row, col).getData();
    }

    /**
     * 获取矩阵大小
     * @return 矩阵大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取矩阵左上角节点
     * @return 左上角节点
     */
    public Node<T> getTopLeft() {
        return topLeft;
    }

    /**
     * 获取矩阵右下角节点
     * @return 右下角节点
     */
    public Node<T> getBottomRight() {
        return matrix[size - 1][size - 1];
    }

    /**
     * 打印矩阵（用于调试）
     */
    public void printMatrix() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                T data = matrix[i][j].getData();
                System.out.print((data != null ? data.toString() : "null") + "\t");
            }
            System.out.println();
        }
    }

    /**
     * 遍历矩阵的所有节点（按行优先）
     * @param visitor 访问器接口
     */
    public void traverse(MatrixVisitor<T> visitor) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                visitor.visit(matrix[i][j], i, j);
            }
        }
    }

    /**
     * 矩阵访问器接口
     * @param <T> 数据类型
     */
    public interface MatrixVisitor<T> {
        void visit(Node<T> node, int row, int col);
    }
}