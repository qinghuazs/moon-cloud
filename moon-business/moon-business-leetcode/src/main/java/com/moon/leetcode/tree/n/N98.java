package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证二叉搜索树
 */
public class N98 {


    public boolean isValidBST(TreeNode root) {
        List<Integer> nums = new ArrayList<>();
        inOrder(root, nums);
        for (int i=0; i<nums.size()-1; i++) {
            if (nums.get(i) >= nums.get(i+1)) {
                return false;
            }
        }
        return true;
    }

    public boolean inOrder(TreeNode root, List<Integer> nums) {
        if (root == null) {
            return true;
        }
        inOrder(root.left, nums);
        nums.add(root.val);
        inOrder(root.right, nums);
        return true;
    }





    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {
        }

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }
}
