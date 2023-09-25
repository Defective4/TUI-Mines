package io.github.defective4.javajam.tuisweeper.core.sfx;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.RadioBoxList;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

public class SFXRadioBoxList<T> extends RadioBoxList<T> {
    private final SFX sfx;

    public SFXRadioBoxList(SFX sfx) {
        this.sfx = sfx;
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        Result res = super.handleKeyStroke(keyStroke);
        if (res == Result.HANDLED) sfx.play("focus");
        else if (keyStroke.getKeyType() == KeyType.Enter || (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == ' '))
            sfx.play("confirm");
        return res;
    }

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        sfx.play("focus");
    }
}
