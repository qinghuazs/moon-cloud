package com.moon.cloud.design.decorator;

public class DecoratorMain {

    public static void main(String[] args) {
        TextEditor textEditor = new AutoSaveDecorator(new SpellCheckEditor(new BasicEditor()));
        textEditor.render();
    }
}
