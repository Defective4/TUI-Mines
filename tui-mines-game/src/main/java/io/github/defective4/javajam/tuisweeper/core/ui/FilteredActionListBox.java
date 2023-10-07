package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.ActionListBox;

/**
 * @author Defective
 */
public class FilteredActionListBox extends ActionListBox {

    private final String filterExtension;

    public FilteredActionListBox(TerminalSize preferredSize, String filterExtension) {
        super(preferredSize);
        this.filterExtension = filterExtension;
    }

    @Override
    public ActionListBox addItem(String label, Runnable action) {
        return label.equals("<empty>") ?
               super.addItem("<No applicable items>", action) :
               label.toLowerCase().endsWith(filterExtension) ? super.addItem(label, action) : this;
    }

}
