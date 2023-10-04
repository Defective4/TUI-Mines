package io.github.defective4.javajam.tuisweeper.core;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

/**
 * Provides methods to easily init and update Discord Rich Presence
 *
 * @author Defective
 */
public final class DiscordIntegr {
    private static long LAST_UPDATE = System.currentTimeMillis();
    private static long START_DATE = System.currentTimeMillis();
    private static boolean enabled;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
    }

    private DiscordIntegr() {
    }

    public static void init() {
        DiscordRPC.discordInitialize("1158412076516114472", new DiscordEventHandlers(), true);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled, TUIMines game) {
        DiscordIntegr.enabled = enabled;
        if (!enabled)
            DiscordRPC.discordClearPresence();
        else {
            if (game == null)
                title();
            else
                update(game);
        }
    }

    public static void title() {
        if (enabled)
            DiscordRPC.discordUpdatePresence(new DiscordRichPresence.Builder("In title screen")
                                                     .setBigImage("sweeper-logo", "TUI Mines")
                                                     .build());
    }

    public static void update(TUIMines game) {
        if (!enabled)
            return;
        if (System.currentTimeMillis() - LAST_UPDATE < 2000) {
            return;
        }
        LAST_UPDATE = System.currentTimeMillis();
        String state = game.isReplay() ? "Watching a replay" : game.getGameOver() == 2 ? "Game won! (in " + game.getCurrentPlayingTime() + ")" : game.getGameOver() == 1 ? "Game Over" : "Playing";
        DiscordRPC.discordUpdatePresence(new DiscordRichPresence.Builder(TUIMines.capitalize(game.getLocalDifficulty()) + " (" + game.getPercentDone() + "%)")
                                                 .setBigImage("sweeper-logo", "TUI Mines")
                                                 .setSmallImage(game.isReplay() ? "replay" : game.getLocalDifficulty()
                                                                                                 .name()
                                                                                                 .toLowerCase(),
                                                                String.format("%s (%sx%s - %s bombs)" + (game.isReplay() ? "(Replay)" : ""),
                                                                              TUIMines.capitalize(
                                                                                      game.getLocalDifficulty()),
                                                                              game.getWidth(),
                                                                              game.getHeight(),
                                                                              game.getBombs()))
                                                 .setDetails(state)
                                                 .setStartTimestamps(START_DATE)
                                                 .build());
    }

    public static void updateStartDate() {
        START_DATE = System.currentTimeMillis();
    }
}
