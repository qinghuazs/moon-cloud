package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 左子叶之和
 */
public class N530 {

    public int getMinimumDifference(TreeNode root)  {
        List<Integer> list = new ArrayList<>();
        inOrder(root, list);
        int ans = Integer.MAX_VALUE;
        for (int i = 0; i < list.size() - 1; i++) {
            ans = Math.min(ans, Math.abs(list.get(i+1) - list.get(i)));
        }
        return ans;
    }

    public void inOrder(TreeNode treeNode, List<Integer> list) {
        if (treeNode == null) {
            return;
        }
        inOrder(treeNode.left, list);
        list.add(treeNode.val);
        inOrder(treeNode.right, list);
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
