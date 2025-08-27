package com.moon.cloud.design.adapter;

public class MediaAdapter implements MediaPlayer {

   private LegacyPlayer legacyPlayer;

   public MediaAdapter(LegacyPlayer legacyPlayer) {
       this.legacyPlayer = legacyPlayer;
   }

    public void setLegacyPlayer(LegacyPlayer legacyPlayer) {
        this.legacyPlayer = legacyPlayer;
    }

    @Override
    public void play(String fileName) {
        legacyPlayer.playFile(fileName);
    }
}
