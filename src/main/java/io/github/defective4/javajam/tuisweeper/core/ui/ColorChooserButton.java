package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXButton;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXEngine;

import static com.googlecode.lanterna.TextColor.ANSI;

public class ColorChooserButton extends SFXButton {

    private TextColor color;

    public ColorChooserButton(WindowBasedTextGUI gui, TextColor color, SFXEngine sfx) {
        super("", sfx, false, () -> {});
        addListener(button -> {
            Window win = new SimpleWindow("Color chooser");
            Panel colorPanel = new Panel(new GridLayout(5));

            for (ANSI c : ANSI.values()) {
                Button btn = new SFXButton(TUISweeper.capitalize(c), sfx, false, () -> {
                    win.close();
                    setColor(c);
                });
                btn.setTheme(new SimpleTheme(c.isBright() ? ANSI.BLACK : ANSI.WHITE_BRIGHT, c));
                colorPanel.addComponent(btn);
            }

            win.setComponent(colorPanel);
            gui.addWindow(win);
        });
        setColor(color);
    }

    public TextColor getColor() {
        return color;
    }

    public void setColor(TextColor color) {
        this.color = color;
        ANSI ansi = color instanceof ANSI ? (ANSI) color : null;
        setLabel(ansi == null ? "Custom" : TUISweeper.capitalize(ansi));
        setTheme(new SimpleTheme((ansi == null || ansi.isBright()) ? ANSI.BLACK : ANSI.WHITE_BRIGHT, color));
    }
}
