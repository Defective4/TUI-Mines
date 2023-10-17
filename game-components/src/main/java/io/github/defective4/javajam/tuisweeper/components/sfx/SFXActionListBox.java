package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;

/**
 * @author Defective
 */
public class SFXActionListBox extends ActionListBox {
    private final SoundEngine sfx;

    public SFXActionListBox(SoundEngine sfx) {
        this.sfx = sfx;
    }

    @Override
    protected synchronized void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        sfx.play("focus");
    }

    @Override
    public Result handleKeyStroke(KeyStroke keyStroke) {
        Result res = super.handleKeyStroke(keyStroke);
        if (res == Result.HANDLED)
            sfx.play("focus");
        return res;
    }
}
