package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialog;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXEngine;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXMessageDialogBuilder;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

public class CustomFileDialogBuilder extends FileDialogBuilder {

    private final boolean save;
    private final SFXEngine sfx;

    private String forcedExtension = null;

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
        if (file != null && !file.isFile()) {
            Field field;

            try {

                field = FileDialog.class.getDeclaredField("fileBox");
                field.setAccessible(true);
                ((TextBox) field.get(dial)).setText(file.getName());

                field = FileDialog.class.getDeclaredField("okButton");
                field.setAccessible(true);
                ((Button) field.get(dial)).takeFocus();


            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return dial;
    }

    public File buildAndShow(WindowBasedTextGUI gui) {
        FileDialog dial = buildDialog();
        File file = dial.showDialog(gui);
        if (file != null && forcedExtension != null && !file.getName().endsWith("." + forcedExtension)) {
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
        return file;
    }
}
