package com.moon.leetcode.tree.n;

import java.util.ArrayList;
import java.util.List;

/**
 * N叉树的前序遍历
 * https://leetcode.cn/problems/n-ary-tree-preorder-traversal/
 */
public class N589 {

    public List<Integer> preorder(Node root) {
        List<Integer> res = new ArrayList<>();
        traverse(root, res);
        return res;
    }

    public void traverse(Node root, List<Integer> res) {
        if (root == null) {
            return;
        }
        res.add(root.val);
        if (root.children != null) {
            for (Node child : root.children) {
                traverse(child, res);
            }
        }
    }
}
class Node {
    public int val;
    public List<Node> children;

    public Node() {}

    public Node(int _val) {
        val = _val;
    }

    public Node(int _val, List<Node> _children) {
        val = _val;
        children = _children;
    }
};
