package io.github.defective4.javajam.tuisweeper.discord;


import com.sun.jna.Callback;

public interface DiscordReadyCallback extends Callback {
    void ready(DiscordUser user);
}
