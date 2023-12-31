package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyType;

/**
 * @author Defective
 */
public class SFXTextBox extends TextBox {

    private final SoundEngine sfx;

    public SFXTextBox(String initialContent, SoundEngine sfx) {
        super(initialContent);
        this.sfx = sfx;
        setInputFilter((interactable, keyStroke) -> {
            if (keyStroke.getKeyType() == KeyType.Character || keyStroke.getKeyType() == KeyType.Backspace)
                sfx.play("key");
            return true;
        });
    }

    @Override
    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
        super.afterEnterFocus(direction, previouslyInFocus);
        sfx.play("focus");
    }
}
