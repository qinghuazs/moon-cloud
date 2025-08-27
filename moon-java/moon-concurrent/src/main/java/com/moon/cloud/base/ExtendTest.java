package com.moon.cloud.base;

import java.lang.reflect.Method;

public class ExtendTest {

    public static void main(String[] args) {
        Child child = new Child();
//        child.print();

        Class clazz = child.getClass();
        System.out.println(clazz.toString());
        System.out.println(clazz.getSimpleName());
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println(method.toString());
        }
    }

   static class Parent {
        public Parent() {
            System.out.println("parent constructor");
        }

        public void print() {
            System.out.println("parent print");
        }
    }

   static class Child extends Parent {
        public Child() {
            System.out.println("child constructor");
        }

        public void print() {
            System.out.println("child print");
        }
    }
}
