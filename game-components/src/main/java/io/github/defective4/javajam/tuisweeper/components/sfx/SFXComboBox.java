package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.ComboBox;

/**
 * @param <T>
 * @author Defective
 */
public class SFXComboBox<T> extends ComboBox<T> {

    private final SoundEngine sfx;

    @SafeVarargs
    public SFXComboBox(SoundEngine sfx, T... items) {
        super(items);
        this.sfx = sfx;
    }
}
