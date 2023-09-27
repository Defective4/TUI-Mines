package io.github.defective4.javajam.tuisweeper.core.sfx;

import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

public class SFXComboBox<T> extends ComboBox<T> {

    private final SFXEngine sfx;

    @SafeVarargs
    public SFXComboBox(SFXEngine sfx, T... items) {
        super(items);
        this.sfx = sfx;
    }

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        sfx.play("focus");
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        Result res = super.handleKeyStroke(keyStroke);
        if (res == Result.HANDLED) sfx.play("focus");
        else if (keyStroke.getKeyType() == KeyType.Enter || (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == ' '))
            sfx.play("confirm");
        return res;
    }
}
