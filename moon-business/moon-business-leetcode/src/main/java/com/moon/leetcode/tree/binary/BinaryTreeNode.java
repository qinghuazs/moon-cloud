package com.moon.leetcode.tree.binary;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;

@Data
public class BinaryTreeNode<T extends Comparable<T>> {

    private T data;

    private BinaryTreeNode<T> left;

    private BinaryTreeNode<T> right;

    public BinaryTreeNode() {}

    public BinaryTreeNode(T data) {
        this.data = data;
    }

    public BinaryTreeNode(T data, BinaryTreeNode<T> left, BinaryTreeNode<T> right) {
        this.data = data;
        this.left = left;
        this.right = right;
    }

    /**
     * 根据传入的List构建二叉搜索树
     * @param dataList 数据列表
     * @return 根节点
     */
    public static <T extends Comparable<T>> BinaryTreeNode<T> buildTreeFromList(List<T> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return null;
        }
        
        BinaryTreeNode<T> root = null;
        for (T data : dataList) {
            if (data != null) {
                root = insertNode(root, data);
            }
        }
        return root;
    }

    /**
     * 插入节点到二叉搜索树
     * @param root 根节点
     * @param data 要插入的数据
     * @return 根节点
     */
    private static <T extends Comparable<T>> BinaryTreeNode<T> insertNode(BinaryTreeNode<T> root, T data) {
        if (root == null) {
            return new BinaryTreeNode<>(data);
        }
        
        int compareResult = data.compareTo(root.data);
        if (compareResult < 0) {
            root.left = insertNode(root.left, data);
        } else if (compareResult > 0) {
            root.right = insertNode(root.right, data);
        }
        // 如果相等，不插入重复元素
        
        return root;
    }

    /**
     * 中序遍历（左-根-右）
     * @param visitor 访问节点的函数
     */
    public void inorderTraversal(Consumer<T> visitor) {
        inorderTraversalHelper(this, visitor);
    }

    private void inorderTraversalHelper(BinaryTreeNode<T> node, Consumer<T> visitor) {
        if (node != null) {
            inorderTraversalHelper(node.left, visitor);
            visitor.accept(node.data);
            inorderTraversalHelper(node.right, visitor);
        }
    }

    /**
     * 前序遍历（根-左-右）
     * @param visitor 访问节点的函数
     */
    public void preorderTraversal(Consumer<T> visitor) {
        preorderTraversalHelper(this, visitor);
    }

    private void preorderTraversalHelper(BinaryTreeNode<T> node, Consumer<T> visitor) {
        if (node != null) {
            visitor.accept(node.data);
            preorderTraversalHelper(node.left, visitor);
            preorderTraversalHelper(node.right, visitor);
        }
    }

    /**
     * 后序遍历（左-右-根）
     * @param visitor 访问节点的函数
     */
    public void postorderTraversal(Consumer<T> visitor) {
        postorderTraversalHelper(this, visitor);
    }

    private void postorderTraversalHelper(BinaryTreeNode<T> node, Consumer<T> visitor) {
        if (node != null) {
            postorderTraversalHelper(node.left, visitor);
            postorderTraversalHelper(node.right, visitor);
            visitor.accept(node.data);
        }
    }

    /**
     * 层级遍历（广度优先遍历）
     * @param visitor 访问节点的函数
     */
    public void levelOrderTraversal(Consumer<T> visitor) {
        if (this == null) {
            return;
        }
        
        Queue<BinaryTreeNode<T>> queue = new LinkedList<>();
        queue.offer(this);
        
        while (!queue.isEmpty()) {
            BinaryTreeNode<T> current = queue.poll();
            visitor.accept(current.data);
            
            if (current.left != null) {
                queue.offer(current.left);
            }
            if (current.right != null) {
                queue.offer(current.right);
            }
        }
    }

    /**
     * 获取中序遍历结果列表
     * @return 中序遍历结果
     */
    public List<T> getInorderList() {
        List<T> result = new ArrayList<>();
        inorderTraversal(result::add);
        return result;
    }

    /**
     * 获取前序遍历结果列表
     * @return 前序遍历结果
     */
    public List<T> getPreorderList() {
        List<T> result = new ArrayList<>();
        preorderTraversal(result::add);
        return result;
    }

    /**
     * 获取后序遍历结果列表
     * @return 后序遍历结果
     */
    public List<T> getPostorderList() {
        List<T> result = new ArrayList<>();
        postorderTraversal(result::add);
        return result;
    }

    /**
     * 获取层级遍历结果列表
     * @return 层级遍历结果
     */
    public List<T> getLevelOrderList() {
        List<T> result = new ArrayList<>();
        levelOrderTraversal(result::add);
        return result;
    }

    /**
     * 打印树的结构（用于调试）
     */
    public void printTree() {
        printTreeHelper(this, "", true);
    }

    private void printTreeHelper(BinaryTreeNode<T> node, String prefix, boolean isLast) {
        if (node != null) {
            System.out.println(prefix + (isLast ? "└── " : "├── ") + node.data);
            
            if (node.left != null || node.right != null) {
                if (node.left != null) {
                    printTreeHelper(node.left, prefix + (isLast ? "    " : "│   "), node.right == null);
                }
                if (node.right != null) {
                    printTreeHelper(node.right, prefix + (isLast ? "    " : "│   "), true);
                }
            }
        }
    }

}
