package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;

import static com.googlecode.lanterna.TextColor.ANSI;

public class ColorChooserButton extends Button {

    private ANSI color;

    public ColorChooserButton(WindowBasedTextGUI gui, ANSI color) {
        super("");
        setColor(color);
        addListener(button -> {
            Window win = new SimpleWindow("Color chooser");
            Panel colorPanel = new Panel(new GridLayout(5));

            for (ANSI c : ANSI.values()) {
                Button btn = new Button(TUISweeper.capitalize(c), () -> {
                    win.close();
                    setColor(c);
                });
                btn.setTheme(new SimpleTheme(c.isBright() ? ANSI.BLACK : ANSI.WHITE_BRIGHT, c));
                colorPanel.addComponent(btn);
            }

            win.setComponent(colorPanel);
            gui.addWindow(win);
        });
    }

    public ANSI getColor() {
        return color;
    }

    public void setColor(ANSI color) {
        this.color = color;
        setLabel(TUISweeper.capitalize(color));
        setTheme(new SimpleTheme(color.isBright() ? ANSI.BLACK : ANSI.WHITE_BRIGHT, color));
    }
}
