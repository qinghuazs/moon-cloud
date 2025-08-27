package com.moon.leetcode.linklist;

public class N206 {

    static class Node {
        int val;
        Node next;

        public void print() {
            Node cur = this;
            while (cur != null) {
                System.out.print(cur.val + " ");
                cur = cur.next;
            }
        }
    }

    public static void main(String[] args) {
        Node head = new Node();
        head.val = 1;
        head.next = new Node();
        head.next.val = 2;
        head.next.next = new Node();
        head.next.next.val = 3;
        head.next.next.next = new Node();
        head.next.next.next.val = 4;
        head.next.next.next.next = new Node();
        head.next.next.next.next.val = 5;
        head.print();
        System.out.println();
        Node head2 = reverse(head);
        head2.print();
    }

    public static Node reverse(Node head) {
        Node pre = null;
        Node cur = head;
        while (cur != null) {
            Node next = cur.next;
            cur.next = pre;
            pre = cur;
            cur = next;
        }
        return pre;
    }
}
