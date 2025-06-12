package com.moon.leetcode.tree;

/**
 * Node二叉树使用示例
 */
public class NodeExample {

    public static void main(String[] args) {
        // 创建Node实例
        Node nodeHelper = new Node(0); // 创建一个辅助实例用于调用方法
        
        // 测试数据
        int[] arr = {50, 30, 70, 20, 40, 60, 80, 10, 25, 35, 45};
        
        System.out.println("原始数组: ");
        for (int value : arr) {
            System.out.print(value + " ");
        }
        System.out.println();
        System.out.println("=".repeat(50));
        
        // 构建二叉搜索树
        Node root = nodeHelper.buildTree(arr);
        
        if (root != null) {
            System.out.println("构建的二叉搜索树结构:");
            nodeHelper.printTree(root);
            System.out.println();
            
            // 中序遍历（对于二叉搜索树，结果应该是有序的）
            System.out.print("中序遍历 (左-根-右): ");
            nodeHelper.inorderTraversal(root);
            System.out.println();
            
            // 前序遍历
            System.out.print("前序遍历 (根-左-右): ");
            nodeHelper.preorderTraversal(root);
            System.out.println();
            
            // 后序遍历
            System.out.print("后序遍历 (左-右-根): ");
            nodeHelper.postorderTraversal(root);
            System.out.println();
            
        } else {
            System.out.println("构建的树为空");
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // 测试单独插入节点
        System.out.println("测试单独插入节点:");
        Node singleRoot = null;
        int[] insertValues = {15, 10, 20, 8, 12, 25};
        
        System.out.print("插入顺序: ");
        for (int value : insertValues) {
            System.out.print(value + " ");
            singleRoot = nodeHelper.insert(singleRoot, value);
        }
        System.out.println();
        
        System.out.println("\n插入后的树结构:");
        nodeHelper.printTree(singleRoot);
        
        System.out.print("\n中序遍历结果: ");
        nodeHelper.inorderTraversal(singleRoot);
        System.out.println();
        
        // 测试重复元素
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试重复元素插入:");
        singleRoot = nodeHelper.insert(singleRoot, 15); // 插入重复元素
        singleRoot = nodeHelper.insert(singleRoot, 10); // 插入重复元素
        
        System.out.println("插入重复元素后的树结构:");
        nodeHelper.printTree(singleRoot);
        
        System.out.print("中序遍历结果: ");
        nodeHelper.inorderTraversal(singleRoot);
        System.out.println();
        
        // 测试空数组
        System.out.println("\n" + "=".repeat(50));
        System.out.println("测试空数组:");
        int[] emptyArr = {};
        Node emptyRoot = nodeHelper.buildTree(emptyArr);
        nodeHelper.printTree(emptyRoot);
    }
}