package io.github.defective4.javajam.tuisweeper.core;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public final class DiscordIntegr {
    private DiscordIntegr() {
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
    }

    public static void init() {
        DiscordRPC.discordInitialize("1158412076516114472", new DiscordEventHandlers(), true);
        DiscordRPC.discordUpdatePresence(new DiscordRichPresence.Builder("In title screen")
                                                 .setBigImage("sweeper-logo", "TUI Sweeper")
                                                 .build());
    }

    private static long LAST_UPDATE = System.currentTimeMillis();
    private static long START_DATE = System.currentTimeMillis();

    public static void update(TUISweeper game) {
        if (System.currentTimeMillis() - LAST_UPDATE < 2000) {
            return;
        }
        LAST_UPDATE = System.currentTimeMillis();
        String state = game.isReplay() ? "Watching a replay" : game.getGameOver() == 2 ? "Game won! (in " + game.getCurrentPlayingTime() + ")" : game.getGameOver() == 1 ? "Game Over" : "Playing";
        DiscordRPC.discordUpdatePresence(new DiscordRichPresence.Builder(TUISweeper.capitalize(game.getLocalDifficulty()) + " (" + game.getPercentDone() + "%)")
                                                 .setBigImage("sweeper-logo", "TUI Sweeper")
                                                 .setSmallImage(game.getLocalDifficulty().name().toLowerCase(),
                                                                String.format("%s (%sx%s - %s bombs)",
                                                                              TUISweeper.capitalize(game.getLocalDifficulty()),
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
