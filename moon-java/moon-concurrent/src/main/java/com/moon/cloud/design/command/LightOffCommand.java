package com.moon.cloud.design.command;

public class LightOffCommand implements Command {

    private Ligt light;

    public LightOffCommand(Ligt light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.off();
    }
}
