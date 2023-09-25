package io.github.defective4.javajam.tuisweeper.core.sfx;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;

public class SFXButton extends Button {

    private final SFX sfx;

    public SFXButton(String label, SFX sfx, boolean back, Runnable action) {
        super(label, action);
        this.sfx = sfx;
        addListener(e -> {
            sfx.play(back ? "back" : "click");
        });
    }

    public SFXButton(String label, SFX sfx, Runnable action) {
        this(label, sfx, false, action);
    }

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        sfx.play("focus");
    }
}
