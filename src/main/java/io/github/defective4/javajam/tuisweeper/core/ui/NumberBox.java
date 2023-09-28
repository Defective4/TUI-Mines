package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.gui2.Interactable;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXEngine;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXTextBox;

import java.util.regex.Pattern;

public class NumberBox extends SFXTextBox {

    private int min = 1;
    private int max = Integer.MAX_VALUE;

    public NumberBox(int initial, SFXEngine sfx) {
        super(Integer.toString(initial), sfx);
        setValidationPattern(Pattern.compile("[0-9]+"));
    }

    @Override
    protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
        super.afterLeaveFocus(direction, nextInFocus);
        int val = getValue();
        if (val < min) setValue(min);
        else setValue(Math.min(val, max));
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getValue() {
        try {
            return Integer.parseInt(getText());
        } catch (Exception e) {
            return 0;
        }
    }

    public void setValue(int value) {
        setText(Integer.toString(value));
    }
}
