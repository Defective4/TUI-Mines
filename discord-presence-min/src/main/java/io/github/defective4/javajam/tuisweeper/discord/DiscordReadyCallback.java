package io.github.defective4.javajam.tuisweeper.discord;


import com.sun.jna.Callback;

/**
 * Callbacks to be called when certain events occur
 *
 * @author Defective
 */
public interface DiscordReadyCallback extends Callback {
    void ready(DiscordUser user);
}
