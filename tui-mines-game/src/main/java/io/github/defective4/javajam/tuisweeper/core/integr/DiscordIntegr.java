package io.github.defective4.javajam.tuisweeper.core.integr;

import io.github.defective4.javajam.tuisweeper.core.TUIMines;
import io.github.defective4.javajam.tuisweeper.discord.DiscordEventHandlers;
import io.github.defective4.javajam.tuisweeper.discord.DiscordPresenceData;
import io.github.defective4.javajam.tuisweeper.discord.DiscordUser;
import io.github.defective4.javajam.tuisweeper.discord.DiscordWrapper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides methods to easily init and update Discord Rich Presence
 *
 * @author Defective
 */
public final class DiscordIntegr {
    private static final Timer cTimer = new Timer(true);
    private static long LAST_UPDATE = System.currentTimeMillis();
    private static long START_DATE = System.currentTimeMillis();
    private static boolean enabled;
    private static DiscordUser user;
    private static boolean cTimerStarted;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DiscordWrapper::shutdown));
    }

    private DiscordIntegr() {
    }

    public static DiscordUser getUser() {
        return user;
    }

    public static void init() {
        DiscordWrapper.initialize("1158412076516114472",
                                  new DiscordEventHandlers(
                                          u -> user = u
                                  ));
        if (!cTimerStarted) {
            cTimerStarted = true;
            cTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    DiscordWrapper.runCallbacks();
                }
            }, 2500, 15000);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled, TUIMines game) {
        DiscordIntegr.enabled = enabled;
        if (!isEnabled()) {
            DiscordWrapper.clearPresence();
        } else {
            if (game == null)
                title();
            else
                update(game);
        }
    }

    public static void title() {
        if (isEnabled())
            DiscordWrapper.updatePresence(new DiscordPresenceData.Builder()
                                                  .setState("In title screen")
                                                  .setLargeImageKey("sweeper-logo")
                                                  .setLargeImageText("TUI Mines")
                                                  .createDiscordPresenceData());
    }

    public static void update(TUIMines game) {
        if (!isEnabled())
            return;
        if (System.currentTimeMillis() - LAST_UPDATE < 2000) {
            return;
        }
        LAST_UPDATE = System.currentTimeMillis();
        String state = game.isReplay() ? "Watching a replay" : game.getGameOver() == 2 ? "Game won! (in " + game.getCurrentPlayingTime() + ")" : game.getGameOver() == 1 ? "Game Over" : "Playing";
        DiscordWrapper.updatePresence(new DiscordPresenceData.Builder()
                                              .setState(TUIMines.capitalize(game.getLocalDifficulty()) + " (" + game.getPercentDone() + "%)")
                                              .setLargeImageKey("sweeper-logo")
                                              .setLargeImageText("TUI Mines")
                                              .setSmallImageKey(game.isReplay() ? "replay" : game.getLocalDifficulty()
                                                                                                 .name()
                                                                                                 .toLowerCase())
                                              .setSmallImageText(String.format("%s (%sx%s - %s bombs)" + (game.isReplay() ? "(Replay)" : ""),
                                                                               TUIMines.capitalize(
                                                                                       game.getLocalDifficulty()),
                                                                               game.getWidth(),
                                                                               game.getHeight(),
                                                                               game.getBombs()))
                                              .setDetails(state)
                                              .setStartTimestamp(START_DATE)
                                              .createDiscordPresenceData());
    }

    public static void updateStartDate() {
        START_DATE = System.currentTimeMillis();
    }
}
