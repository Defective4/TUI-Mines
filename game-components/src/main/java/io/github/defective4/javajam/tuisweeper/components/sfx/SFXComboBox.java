package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.ComboBox;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

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

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        sfx.play("focus");
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        Result res = super.handleKeyStroke(keyStroke);
        if (res == Result.HANDLED) sfx.play("focus");
        else if (keyStroke.getKeyType() == KeyType.Enter || keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == ' ')
            sfx.play("confirm");
        return res;
    }
}
