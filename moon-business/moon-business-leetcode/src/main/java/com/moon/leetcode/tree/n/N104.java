package com.moon.leetcode.tree.n;

import java.util.LinkedList;

/**
 * 二叉树的最大深度
 */
public class N104 {

    public int maxDepth(TreeNode root) {
        int res = 0;
        if (root == null) {
            return res;
        }
        return Math.max(maxDepth(root.left) + 1, maxDepth(root.right) + 1);
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
