package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialog;
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @param <T>
 * @author Defective
 */
public class SFXListSelectDialogBuilder<T> extends ListSelectDialogBuilder<T> {

    private final SFXEngine sfx;

    public SFXListSelectDialogBuilder(SFXEngine sfx) {
        this.sfx = sfx;
    }

    @Override
    protected ListSelectDialog<T> buildDialog() {
        ListSelectDialog<T> dial = super.buildDialog();
        dial.setHints(Arrays.asList(Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        Panel panel = (Panel) dial.getComponent();
        try {
            Field field = Panel.class.getDeclaredField("components");
            field.setAccessible(true);
            List<Component> cpts = (List<Component>) field.get(panel);
            ActionListBox original = (ActionListBox) cpts.get(2);
            ActionListBox replacement = new SFXActionListBox(sfx);
            replacement.setPreferredSize(original.getPreferredSize());
            replacement.setLayoutData(original.getLayoutData());
            for (Runnable run : original.getItems())
                replacement.addItem(run.toString(), run);

            replacement.withBorder(Borders.singleLine()).addTo(panel);
            cpts.set(2, cpts.get(cpts.size() - 1));
            cpts.remove(cpts.size() - 1);
            replacement.takeFocus();

        } catch (Exception ignored) {
        }

        return dial;
    }
}
