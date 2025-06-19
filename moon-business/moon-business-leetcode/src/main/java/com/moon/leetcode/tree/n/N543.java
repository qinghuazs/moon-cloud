package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.List;

/**
 * 左子叶之和
 */
public class N543 {

    public int diameterOfBinaryTree(TreeNode root) {
        if (root == null) {
            return 0;
        }
        if (root.left == null && root.right == null) {
            return 0;
        }
        return diameterOfBinaryTree(root.left) + diameterOfBinaryTree(root.right) + 1;
    }

    public int point(TreeNode root, int max) {
        return 0;
    }


    class TreeNode {

        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {
        }

        TreeNode(int val) {
            this.val = val;
        }

    }
}
