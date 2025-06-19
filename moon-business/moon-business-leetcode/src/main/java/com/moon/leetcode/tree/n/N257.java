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
        List<String> res = new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        binaryTreePaths(root, path, res);
        return res;
    }

    public void binaryTreePaths(TreeNode root, List<Integer> list, List<String> res) {
        list.add(root.val);
        if (root.left == null && root.right == null) {
            for (int i=0; i<list.size(); i++) {
                String str = "";
                if (i == 0) {
                    str = str + list.get(i);
                } else {
                    str = str + "->" + list.get(i);
                }
                res.add(str);
            }
            list.remove(list.size()-1);
            return;
        }
        if (root.left != null) {
            list.add(root.left.val);
            binaryTreePaths(root.left, list, res);
        }
        if (root.right != null) {
            list.add(root.right.val);
            binaryTreePaths(root.right, list, res);
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
