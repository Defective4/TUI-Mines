package io.github.defective4.javajam.tuisweeper.components.sfx;

public class DummySoundEngine implements SoundEngine {
    @Override
    public void play(String sound) {

    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
