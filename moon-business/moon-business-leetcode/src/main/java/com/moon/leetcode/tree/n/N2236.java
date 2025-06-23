package com.moon.leetcode.tree.n;

/**
 * 判断根节点是否等于子节点之和
 */
public class N2236 {
    
    public static void main(String[] args) {
        TreeNode root = new TreeNode(10);
        root.left = new TreeNode(4);
        root.right = new TreeNode(6);   
        System.out.println(new N2236().checkTree(root));
    }

    int ans = 0;

    public boolean checkTree(TreeNode root) {
        preorder(root);
        ans = ans - root.val;
        return root.val == ans;
    }

    public void preorder(TreeNode root) {
        if (root == null) {
            return;
        }
        ans += root.val;
        preorder(root.left);
        preorder(root.right);
    }

    static class TreeNode {
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
