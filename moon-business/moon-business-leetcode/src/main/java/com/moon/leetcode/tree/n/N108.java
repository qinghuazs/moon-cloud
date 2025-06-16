package com.moon.leetcode.tree.n;

/**
 * 平衡二叉树
 */
public class N108 {

    public TreeNode sortedArrayToBST(int[] nums) {
        int n = nums.length;
        if (n == 0) {
            return null;
        }
        if (n == 1) {
            return new TreeNode(nums[0]);
        }
        int mid = n >> 1;
        TreeNode root = new TreeNode(nums[mid]);
        int leftStart = 0;
        int rightStart = mid + 1;

        root.left = subTree(nums, leftStart, mid - 1);
        root.right = subTree(nums, rightStart, n-1);
        return root;
    }

    public TreeNode subTree(int[] nums, int start, int end) {
        if (start > end) {
            return null;
        }
        int mid = start + (end - start) / 2;
        TreeNode node = new TreeNode(nums[mid]);
        node.left = subTree(nums, start, mid - 1);
        node.right = subTree(nums, mid + 1, end);
        return node;
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
