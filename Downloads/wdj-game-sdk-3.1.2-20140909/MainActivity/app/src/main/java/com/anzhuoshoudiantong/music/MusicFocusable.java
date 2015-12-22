package com.anzhuoshoudiantong.music;

public interface MusicFocusable {
    public void onGainedAudioFocus();

    public void onLostAudioFocus(boolean canDuck);
}
