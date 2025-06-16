package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 平衡二叉树
 */
public class N110 {

    public boolean isBalanced(TreeNode root) {
        if (root == null) {
            return true;
        }
        int  maxRight = maxDepth(root.right);
        int  maxLeft = maxDepth(root.left);
        return Math.abs(maxRight - maxLeft) <= 1;
    }

    public int maxDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        return Math.max(maxDepth(root.left), maxDepth(root.right)) + 1;
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
