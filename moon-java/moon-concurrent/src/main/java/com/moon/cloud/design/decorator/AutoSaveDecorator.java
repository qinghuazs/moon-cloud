package com.moon.cloud.design.decorator;

public class AutoSaveDecorator extends EditorDecorator {

    public AutoSaveDecorator(TextEditor textEditor) {
        super(textEditor);
    }

    @Override
    public void render() {
        super.render();
        System.out.println("AutoSaveDecorator render");
    }
}
