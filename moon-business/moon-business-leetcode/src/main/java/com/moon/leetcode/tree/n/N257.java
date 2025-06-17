package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 二叉树的所有路径
 */
public class N257 {

    public List<String> binaryTreePaths(TreeNode root) {
        if (root == null) {
            return new ArrayList<>();
        }
        return new LinkedList<>(binaryTreePaths(root));
    }

    public void buildPath(TreeNode root, StringBuilder path) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null) {
            path.append("->").append(root.val);
            return;
        }
        if (root.left != null) {
            buildPath(root.left, path.append("->").append(root.val));
        }
        if (root.right != null) {
            buildPath(root.right, path.append("->").append(root.val));
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
