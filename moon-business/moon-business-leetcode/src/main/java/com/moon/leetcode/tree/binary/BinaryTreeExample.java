package com.moon.leetcode.tree.binary;

import java.util.Arrays;
import java.util.List;

/**
 * 二叉树使用示例
 */
public class BinaryTreeExample {

    public static void main(String[] args) {
        // 测试数据
        List<Integer> dataList = Arrays.asList(50, 30, 70, 20, 40, 60, 80, 10, 25, 35, 45);
        
        System.out.println("原始数据: " + dataList);
        System.out.println("=".repeat(50));
        
        // 根据List构建二叉搜索树
        BinaryTreeNode<Integer> root = BinaryTreeNode.buildTreeFromList(dataList);
        
        if (root != null) {
            System.out.println("构建的二叉搜索树结构:");
            root.printTree();
            System.out.println();
            
            // 中序遍历（对于二叉搜索树，结果应该是有序的）
            System.out.println("中序遍历 (左-根-右): " + root.getInorderList());
            
            // 前序遍历
            System.out.println("前序遍历 (根-左-右): " + root.getPreorderList());
            
            // 后序遍历
            System.out.println("后序遍历 (左-右-根): " + root.getPostorderList());
            
            // 层级遍历
            System.out.println("层级遍历 (广度优先): " + root.getLevelOrderList());
            
            System.out.println("\n" + "=".repeat(50));
            
            // 使用Consumer进行自定义遍历
            System.out.println("使用Consumer进行中序遍历:");
            root.inorderTraversal(data -> System.out.print(data + " "));
            System.out.println();
            
            System.out.println("\n使用Consumer进行层级遍历:");
            root.levelOrderTraversal(data -> System.out.print("[" + data + "] "));
            System.out.println();
        } else {
            System.out.println("构建的树为空");
        }
        
        // 测试字符串类型的二叉树
        System.out.println("\n" + "=".repeat(50));
        System.out.println("字符串类型二叉树测试:");
        
        List<String> stringList = Arrays.asList("dog", "cat", "elephant", "ant", "bird", "fish", "giraffe");
        System.out.println("原始数据: " + stringList);
        
        BinaryTreeNode<String> stringRoot = BinaryTreeNode.buildTreeFromList(stringList);
        if (stringRoot != null) {
            System.out.println("\n构建的字符串二叉搜索树结构:");
            stringRoot.printTree();
            System.out.println();
            
            System.out.println("中序遍历 (按字典序): " + stringRoot.getInorderList());
            System.out.println("层级遍历: " + stringRoot.getLevelOrderList());
        }
    }
}