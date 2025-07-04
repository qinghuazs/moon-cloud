package com.moon.leetcode.tree.n;

/**
 * 对称二叉树
 */
public class N101 {

    public boolean isSymmetric(TreeNode root) {
        if (root == null) {
            return true;
        }
        return same(root.left, root.right);
    }

    public boolean same(TreeNode left, TreeNode right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.val == right.val &&  same(left.left, right.right) && same(left.right, right.left);
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
