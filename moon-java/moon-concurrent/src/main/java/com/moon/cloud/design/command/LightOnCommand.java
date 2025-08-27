package com.moon.cloud.design.command;

public class LightOnCommand implements Command {

    private Ligt light;

    public LightOnCommand(Ligt light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.on();
    }
}
