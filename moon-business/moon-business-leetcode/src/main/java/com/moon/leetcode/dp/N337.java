package com.moon.leetcode.dp;

import com.moon.leetcode.tree.n.N26;

public class N337 {

    public int rob(TreeNode root) {
        int[] res = dfs(root);
        return Math.max(res[0], res[1]);
    }

    public int[] dfs(TreeNode root) {
        if (root == null) {
            return new int[]{0, 0};
        }
        int[] left = dfs(root.left);
        int[] right = dfs(root.right);
        int rob = root.val + left[0] + right[0];
        int noRob = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);
        return new int[]{rob, noRob};
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
