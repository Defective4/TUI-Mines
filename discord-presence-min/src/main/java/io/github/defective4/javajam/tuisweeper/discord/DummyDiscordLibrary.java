package io.github.defective4.javajam.tuisweeper.discord;

/**
 * A no-op implementation of {@link DiscordLibrary}.
 * Used as a fallback when natives are not available.
 *
 * @author Defective
 */
public class DummyDiscordLibrary implements DiscordLibrary {
    @Override
    public void Discord_Initialize(String applicationId, DiscordEventHandlers handlers, int autoRegister, String optionalSteamId) {

    }

    @Override
    public void Discord_RunCallbacks() {

    }

    @Override
    public void Discord_UpdatePresence(DiscordPresenceData presence) {

    }

    @Override
    public void Discord_ClearPresence() {

    }

    @Override
    public void Discord_Shutdown() {

    }
}
