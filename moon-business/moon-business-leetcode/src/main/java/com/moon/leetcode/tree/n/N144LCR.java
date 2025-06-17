package com.moon.leetcode.tree.n;

/**
 * 反转二叉树
 */
public class N144LCR {

    public TreeNode flipTree(TreeNode root) {
        if (root == null) {
            return null;
        }
        TreeNode tmp = root.left;
        root.left = root.right;
        root.right = tmp;
        flipTree(root.left);
        flipTree(root.right);
        return root;
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
