package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.FileDialog;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXEngine;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXMessageDialogBuilder;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class CustomFileDialogBuilder extends FileDialogBuilder {

    private final boolean save;
    private final SFXEngine sfx;

    private String forcedExtension;

    public CustomFileDialogBuilder(boolean save, SFXEngine sfx) {
        this.save = save;
        this.sfx = sfx;
    }


    public CustomFileDialogBuilder(SFXEngine sfx) {
        this(false, sfx);
    }

    public String getForcedExtension() {
        return forcedExtension;
    }

    public CustomFileDialogBuilder setForcedExtension(String forcedExtension) {
        this.forcedExtension = forcedExtension;
        return this;
    }

    @Override
    protected FileDialog buildDialog() {
        FileDialog dial = super.buildDialog();
        dial.setHints(Arrays.asList(Window.Hint.NO_POST_RENDERING, Window.Hint.CENTERED));
        File file = getSelectedFile();
        try {
            Field field;
            if (file != null && !file.isFile()) {

                field = FileDialog.class.getDeclaredField("fileBox");
                field.setAccessible(true);
                ((TextBox) field.get(dial)).setText(file.getName());

                field = FileDialog.class.getDeclaredField("okButton");
                field.setAccessible(true);
                ((Button) field.get(dial)).takeFocus();

                if (forcedExtension != null && !save) {
                    field = FileDialog.class.getDeclaredField("fileListBox");
                    field.setAccessible(true);
                    Field fin = Field.class.getDeclaredField("modifiers");
                    fin.setAccessible(true);
                    fin.set(field, field.getModifiers() & ~Modifier.FINAL);

                    ActionListBox fileListBox = new FilteredActionListBox(new TerminalSize(30, 10), forcedExtension);
                    field.set(dial, fileListBox);

                    Panel panel = (Panel) dial.getComponent();
                    field = Panel.class.getDeclaredField("components");
                    field.setAccessible(true);
                    List<Component> cpt = (List<Component>) field.get(panel);
                    fileListBox.withBorder(Borders.singleLine())
                               .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING,
                                                                          GridLayout.Alignment.CENTER,
                                                                          false,
                                                                          false))
                               .addTo(panel);

                    cpt.set(2, cpt.get(cpt.size() - 1));
                    cpt.remove(cpt.size() - 1);

                    field = FileDialog.class.getDeclaredField("directory");
                    field.setAccessible(true);

                    Method meth = FileDialog.class.getDeclaredMethod("reloadViews", File.class);
                    meth.setAccessible(true);
                    meth.invoke(dial, field.get(dial));

                }
            }
        } catch (Exception ignored) {
        }
        return dial;
    }

    public File buildAndShow(WindowBasedTextGUI gui) {
        FileDialog dial = buildDialog();
        File file = dial.showDialog(gui);
        if (save) {
            if (file != null && forcedExtension != null && !file.getName()
                                                                .toLowerCase()
                                                                .endsWith("." + forcedExtension)) {
                file = new File(file.getParentFile(), file.getName() + "." + forcedExtension);
            }
            if (file != null && file.isFile()) {
                MessageDialog md = new SFXMessageDialogBuilder(sfx).setText("A file with this name already exists\n" + "in this location. \n" + "Do you want to overwrite it?")
                                                                   .setTitle("Overwriting file")
                                                                   .addButton(MessageDialogButton.No)
                                                                   .addButton(MessageDialogButton.Yes)
                                                                   .build();

                if (md.showDialog(gui) != MessageDialogButton.Yes) return null;
            }
        }
        return file;
    }
}
