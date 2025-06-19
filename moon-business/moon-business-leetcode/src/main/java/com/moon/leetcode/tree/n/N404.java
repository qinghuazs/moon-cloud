package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.List;

/**
 * 左子叶之和
 */
public class N404 {

    public int sumOfLeftLeaves(TreeNode root) {
        int ans = 0;
        add(root, 0, true);
        return ans;
    }

    public void add(TreeNode root, int ans, boolean left) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null && left) {
            ans += root.val;
            return;
        }
        if (root.left != null) {
            add(root.left, ans, true);
        }
        if (root.right != null) {
            add(root.right, ans, false);
        }
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
