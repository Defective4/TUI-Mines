package io.github.defective4.javajam.tuisweeper.components.sfx;


import com.googlecode.lanterna.gui2.CheckBox;

/**
 * @author Defective
 */
public class SFXCheckBox extends CheckBox {

    private final SoundEngine sfx;

    public SFXCheckBox(String label, boolean checked, SoundEngine sfx) {
        super(label);
        this.sfx = sfx;
        setChecked(checked);
    }
}
