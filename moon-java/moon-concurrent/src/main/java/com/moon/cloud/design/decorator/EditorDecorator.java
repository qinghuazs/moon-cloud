package com.moon.cloud.design.decorator;

public class EditorDecorator implements TextEditor {
    /**
     * 被装饰的对象
     */
    protected TextEditor textEditor;

    public EditorDecorator(TextEditor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public void render() {
        textEditor.render();
    }
}
