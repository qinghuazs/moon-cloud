package com.moon.leetcode.tree.n;

/**
 * 完全二叉树的节点个数
 */
public class N98 {


    public boolean isValidBST(TreeNode root) {
        return validate(root);
    }

    public boolean validate(TreeNode root) {
        if (root == null) {
            return true;
        }

        return false;
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
