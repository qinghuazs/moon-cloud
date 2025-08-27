package com.moon.cloud.design.adapter;

public class AdapterMain {

    public static void main(String[] args) {
        MediaPlayer mediaPlayer = new MediaAdapter(new LegacyPlayer());
        mediaPlayer.play("文件");
    }
}
