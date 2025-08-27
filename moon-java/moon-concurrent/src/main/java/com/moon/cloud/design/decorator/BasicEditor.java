package com.moon.cloud.design.decorator;

public class BasicEditor implements TextEditor {

    @Override
    public void render() {
        System.out.println("BasicEditor render");
    }
}
