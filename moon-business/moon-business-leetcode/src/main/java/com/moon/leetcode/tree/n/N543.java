package com.moon.leetcode.tree.n;

/**
 * 完全二叉树的节点个数
 */
public class N543 {

    public TreeNode mergeTrees(TreeNode root1, TreeNode root2) {
        TreeNode root = buildTree(root1, root2);
        return root;
    }

    public TreeNode buildTree(TreeNode root1, TreeNode root2) {
        TreeNode node = new TreeNode();
        if (root1 == null) {
            node.val = root2.val;
        }
        if (root2 == null) {
            node.val = root1.val;
        }
        if (root1 == null && root2 == null) {
            return null;
        }
        node.left = buildTree(root1.left, root2.left);
        node.right = buildTree(root1.right, root2.right);
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
