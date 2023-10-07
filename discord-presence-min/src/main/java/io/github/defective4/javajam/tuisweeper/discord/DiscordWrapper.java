package io.github.defective4.javajam.tuisweeper.discord;


import com.sun.jna.Native;

/**
 * Wraps {@link DiscordLibrary}.
 * It takes care of library loading so it's safer to use this wrapper than the library itself.
 *
 * @author Defective
 */
public final class DiscordWrapper {

    private static final String[] LIBS = {
            "discord-rpc-x64.dll",
            "discord-rpc-x86.dll",
            "libdiscord-rpc.so"
    };

    private static DiscordLibrary LIB;
    private static boolean AVAILABLE;

    static {
        for (String l : LIBS) {
            try {
                LIB = Native.load("natives/" + l, DiscordLibrary.class);
                AVAILABLE = true;
                break;
            } catch (Throwable ignored) {
            }
        }
        if (LIB == null)
            LIB = new DummyDiscordLibrary();
    }

    private DiscordWrapper() {
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    public static void initialize(String applicationId, DiscordEventHandlers handlers) {
        LIB.Discord_Initialize(applicationId, handlers, 1, null);
    }

    public static void runCallbacks() {
        LIB.Discord_RunCallbacks();
    }

    public static void updatePresence(DiscordPresenceData presence) {
        LIB.Discord_UpdatePresence(presence);
    }

    public static void clearPresence() {
        LIB.Discord_ClearPresence();
    }

    public static void shutdown() {
        LIB.Discord_Shutdown();
    }
}
