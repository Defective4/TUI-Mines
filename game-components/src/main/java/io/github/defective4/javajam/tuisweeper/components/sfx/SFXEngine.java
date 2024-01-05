package io.github.defective4.javajam.tuisweeper.components.sfx;

/**
 * SFX engine loads, stores and plays WAVE sound files
 * associated with certaing events in the application.
 * <p>
 * It's mainly used in separate SFX components.
 *
 * @author Defective
 */
public final class SFXEngine implements SoundEngine {

    public SFXEngine() {
    }

    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
