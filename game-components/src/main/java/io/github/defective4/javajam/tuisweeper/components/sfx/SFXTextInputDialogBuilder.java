package io.github.defective4.javajam.tuisweeper.components.sfx;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Defective
 */
public class SFXTextInputDialogBuilder extends TextInputDialogBuilder {

    private final SFXEngine sfx;

    public SFXTextInputDialogBuilder(SFXEngine sfx) {
        this.sfx = sfx;
    }

    @Override
    protected TextInputDialog buildDialog() {
        TextInputDialog dial = super.buildDialog();
        Panel panel = (Panel) dial.getComponent();
        try {
            Field field;
            field = Panel.class.getDeclaredField("components");
            field.setAccessible(true);
            List<Component> cpts = (List<Component>) field.get(panel);
            TextBox old = (TextBox) cpts.get(2);
            TextBox box = new SFXTextBox(old.getText(), sfx);
            box.setMask(old.getMask());
            box.setLayoutData(old.getLayoutData());

            field = TextInputDialog.class.getDeclaredField("textBox");
            field.setAccessible(true);
            field.set(dial, box);

            box.addTo(panel);
            cpts.set(2, cpts.get(cpts.size() - 1));
            cpts.remove(cpts.size() - 1);

            box.takeFocus();

        } catch (Exception ignored) {
        }

        return dial;
    }
}
