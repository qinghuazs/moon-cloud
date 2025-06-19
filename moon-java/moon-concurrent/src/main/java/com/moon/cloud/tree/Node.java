package com.moon.cloud.tree;

public class Node<T> {
    private Node<T> up;
    private Node<T> down;
    private Node<T> left;
    private Node<T> right;
    private T data;
    private boolean visited;

    public Node(T data) {
        this.data = data;
    }

    public Node(Node<T> up, Node<T> down, Node<T> left, Node<T> right, T data, boolean visited) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.data = data;
        this.visited = visited;
    }

    public Node<T> getUp() {
        return up;
    }
    public void setUp(Node<T> up) {
        this.up = up;
    }
    public Node<T> getDown() {
        return down;
    }
    public void setDown(Node<T> down) {
        this.down = down;
    }
    public Node<T> getLeft() {
        return left;
    }
    public void setLeft(Node<T> left) {
        this.left = left;
    }
    public Node<T> getRight() {
        return right;
    }
    public void setRight(Node<T> right) {
        this.right = right;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public void setVisited(boolean visited) {this.visited = visited;}
    public boolean isVisited() {return visited;}
}
