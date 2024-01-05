package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.ActionListBox;

/**
 * @author Defective
 */
public class SFXActionListBox extends ActionListBox {
    private final SoundEngine sfx;

    public SFXActionListBox(SoundEngine sfx) {
        this.sfx = sfx;
    }
}
