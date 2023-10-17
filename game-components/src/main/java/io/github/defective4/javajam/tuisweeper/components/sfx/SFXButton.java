package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Interactable;

/**
 * @author Defective
 */
public class SFXButton extends Button {

    private final SFXEngine sfx;

    public SFXButton(String label, SFXEngine sfx, boolean back, Runnable action) {
        super(label, () -> sfx.play(back ? "back" : "click"));
        this.sfx = sfx;
        addListener(b -> action.run());
    }

    public SFXButton(String label, SFXEngine sfx, Runnable action) {
        this(label, sfx, false, action);
    }

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        sfx.play("focus");
    }
}
