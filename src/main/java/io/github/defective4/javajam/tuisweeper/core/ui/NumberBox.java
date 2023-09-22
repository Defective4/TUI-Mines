package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.TextBox;

import java.util.regex.Pattern;

public class NumberBox extends TextBox {

    private int min = 1;
    private int max = Integer.MAX_VALUE;

    public NumberBox(int initial) {
        super(Integer.toString(initial));
        setValidationPattern(Pattern.compile("[0-9]+"));
    }

    @Override
    protected void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
        super.afterLeaveFocus(direction, nextInFocus);
        int val = getValue();
        if (val < min) setValue(min);
        else if (val > max) setValue(max);
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
