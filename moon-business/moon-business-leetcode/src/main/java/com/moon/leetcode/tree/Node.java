package com.moon.leetcode.tree;

import lombok.Data;

@Data
public class Node {

    public int data;

    public Node left;

    public Node right;

    public Node(int data) {
        this.data = data;
    }

    /**
     * 根据数组构建二叉搜索树
     * @param arr 输入数组
     * @return 根节点
     */
    public Node buildTree(int[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }
        Node root = null;
        for (int value : arr) {
            root = insert(root, value);
        }
        return root;
    }

    /**
     * 向二叉搜索树中插入节点
     * @param root 根节点
     * @param data 要插入的数据
     * @return 插入后的根节点
     */
    public Node insert(Node root, int data) {
        if (root == null) {
            return new Node(data);
        }
        
        if (data < root.data) {
            root.left = insert(root.left, data);
        } else if (data > root.data) {
            root.right = insert(root.right, data);
        }
        // 如果data等于root.data，不插入重复元素
        
        return root;
    }
    
    /**
     * 中序遍历（左-根-右）
     * @param root 根节点
     */
    public void inorderTraversal(Node root) {
        if (root != null) {
            inorderTraversal(root.left);
            System.out.print(root.data + " ");
            inorderTraversal(root.right);
        }
    }
    
    /**
     * 前序遍历（根-左-右）
     * @param root 根节点
     */
    public void preorderTraversal(Node root) {
        if (root != null) {
            System.out.print(root.data + " ");
            preorderTraversal(root.left);
            preorderTraversal(root.right);
        }
    }
    
    /**
     * 后序遍历（左-右-根）
     * @param root 根节点
     */
    public void postorderTraversal(Node root) {
        if (root != null) {
            postorderTraversal(root.left);
            postorderTraversal(root.right);
            System.out.print(root.data + " ");
        }
    }
    
    /**
     * 打印树的结构
     * @param root 根节点
     * @param prefix 前缀
     * @param isLast 是否为最后一个节点
     */
    public void printTree(Node root, String prefix, boolean isLast) {
        if (root != null) {
            System.out.println(prefix + (isLast ? "└── " : "├── ") + root.data);
            
            if (root.left != null || root.right != null) {
                if (root.left != null) {
                    printTree(root.left, prefix + (isLast ? "    " : "│   "), root.right == null);
                }
                if (root.right != null) {
                    printTree(root.right, prefix + (isLast ? "    " : "│   "), true);
                }
            }
        }
    }
    
    /**
     * 打印树的结构（重载方法）
     * @param root 根节点
     */
    public void printTree(Node root) {
        if (root == null) {
            System.out.println("树为空");
            return;
        }
        printTree(root, "", true);
    }
}
