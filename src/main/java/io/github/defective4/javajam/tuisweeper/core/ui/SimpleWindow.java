package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.gui2.BasicWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleWindow extends BasicWindow {
    public SimpleWindow(String title, Hint... hints) {
        super(title);
        List<Hint> list = new ArrayList<>(Arrays.asList(hints));
        list.add(Hint.NO_POST_RENDERING);
        list.add(Hint.MODAL);
        list.add(Hint.CENTERED);
        setHints(list);
    }

    public SimpleWindow(Hint... hints) {
        this("", hints);
    }
}
