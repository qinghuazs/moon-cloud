package com.moon.cloud.design.decorator;

public class SpellCheckEditor extends EditorDecorator {

    public SpellCheckEditor(TextEditor textEditor) {
        super(textEditor);
    }

    @Override
    public void render() {
        super.render();
        System.out.println("SpellCheckEditor render");
    }
}
