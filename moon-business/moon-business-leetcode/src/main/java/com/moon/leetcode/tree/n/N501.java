package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 左子叶之和
 */
public class N501 {

    public int[] findMode(TreeNode root) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        inOrder(root, map);
        int max = 0;
        List<Integer> list =new ArrayList<>();
        for (Map.Entry entry : map.entrySet()) {
            Integer key = (Integer) entry.getKey();
            Integer count = (Integer) entry.getValue();
            if (count > max) {
                max = count;
                list.clear();
                list.add(key);
            } else if (count == max) {
                list.add(key);
            }
        }
        int[] res = new int[list.size()];
        int i = 0;
        for (Integer key : list) {
            res[i++] = key;
        }
        return res;
    }

    public void inOrder(TreeNode treeNode, Map<Integer, Integer> map) {
        if (treeNode == null) {
            return;
        }
        inOrder(treeNode.left, map);
        Integer count = map.get(treeNode.val);
        if (count == null) {
            map.put(treeNode.val, 1);
        } else {
            map.put(treeNode.val, count+1);
        }
        inOrder(treeNode.right, map);
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
