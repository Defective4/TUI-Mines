package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.Button;

/**
 * @author Defective
 */
public class SFXButton extends Button {

    private final SoundEngine sfx;

    public SFXButton(String label, SoundEngine sfx, boolean back, Runnable action) {
        super(label, action);
        this.sfx = sfx;
    }

    public SFXButton(String label, SoundEngine sfx, Runnable action) {
        this(label, sfx, false, action);
    }
}
