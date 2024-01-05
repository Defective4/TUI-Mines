package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.RadioBoxList;

/**
 * @param <T>
 * @author Defective
 */
public class SFXRadioBoxList<T> extends RadioBoxList<T> {
    private final SoundEngine sfx;

    public SFXRadioBoxList(SoundEngine sfx) {
        this.sfx = sfx;
    }
}
