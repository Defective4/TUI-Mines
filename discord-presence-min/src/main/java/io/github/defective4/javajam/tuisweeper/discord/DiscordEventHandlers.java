package io.github.defective4.javajam.tuisweeper.discord;

public class DiscordEventHandlers extends OrderedStructure {
    public DiscordReadyCallback ready;

    public DiscordEventHandlers(DiscordReadyCallback ready) {
        this.ready = ready;
    }

    public DiscordEventHandlers() {
    }
}
