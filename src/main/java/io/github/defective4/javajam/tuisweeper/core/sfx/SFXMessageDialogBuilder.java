package io.github.defective4.javajam.tuisweeper.core.sfx;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SFXMessageDialogBuilder extends MessageDialogBuilder {
    private final SFXEngine sfx;

    public SFXMessageDialogBuilder(SFXEngine sfx) {
        this.sfx = sfx;
        setExtraWindowHints(Arrays.asList(Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
    }

    @Override
    public MessageDialog build() {
        MessageDialog dial = super.build();

        Panel buttonPanel = (Panel) ((Panel) dial.getComponent()).getChildrenList().get(2);
        List<Component> cpts = buttonPanel.getChildrenList();
        buttonPanel.removeAllComponents();
        for (Component cpt : cpts) {
            if (cpt instanceof Button) {
                Button old = (Button) cpt;
                Button nw = new SFXButton(old.getLabel(), sfx, false, () -> {});
                try {
                    Field field = Button.class.getDeclaredField("listeners");
                    field.setAccessible(true);
                    for (Button.Listener ls : (List<Button.Listener>) field.get(old))
                        nw.addListener(ls);
                    buttonPanel.addComponent(nw);
                } catch (Exception ignored) {
                }

            }
        }
        cpts = buttonPanel.getChildrenList();
        if (!cpts.isEmpty() && cpts.get(0) instanceof Button) {
            ((Button) cpts.get(0)).takeFocus();
        }

        return dial;
    }
}
