package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.TextBox;

/**
 * @author Defective
 */
public class SFXTextBox extends TextBox {

    private final SoundEngine sfx;

    public SFXTextBox(String initialContent, SoundEngine sfx) {
        super(initialContent);
        this.sfx = sfx;
    }
}
