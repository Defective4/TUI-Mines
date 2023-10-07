package io.github.defective4.javajam.tuisweeper.discord;

import com.sun.jna.Library;

/**
 * Discord API interface
 *
 * @author Defective
 */
public interface DiscordLibrary extends Library {
    void Discord_Initialize(String applicationId,
                            DiscordEventHandlers handlers,
                            int autoRegister,
                            String optionalSteamId);

    void Discord_RunCallbacks();

    void Discord_UpdatePresence(DiscordPresenceData presence);

    void Discord_ClearPresence();

    void Discord_Shutdown();
}
