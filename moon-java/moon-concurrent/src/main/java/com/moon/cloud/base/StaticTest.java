package com.moon.cloud.base;

public class StaticTest {
    public static void main(String[] args) {
        new StaticTest();
    }

    static {
        System.out.println("static block");
    }

    public StaticTest() {
        System.out.println("constructor");
    }
}
