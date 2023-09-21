package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;

public class ColorChooserButton extends Button {

    private TextColor.ANSI color;

    public ColorChooserButton(WindowBasedTextGUI gui, TextColor.ANSI color) {
        super("");
        setColor(color);
        addListener(button -> {
            Window win = new SimpleWindow("Color chooser");
            Panel colorPanel = new Panel(new GridLayout(5));

            for (TextColor.ANSI c : TextColor.ANSI.values()) {
                Button btn = new Button(TUISweeper.capitalize(c), () -> {
                    win.close();
                    setColor(c);
                });
                btn.setTheme(new SimpleTheme(c.isBright() ? TextColor.ANSI.BLACK : TextColor.ANSI.WHITE_BRIGHT, c));
                colorPanel.addComponent(btn);
            }

            win.setComponent(colorPanel);
            gui.addWindow(win);
        });
    }

    public TextColor.ANSI getColor() {
        return color;
    }

    public void setColor(TextColor.ANSI color) {
        this.color = color;
        setLabel(TUISweeper.capitalize(color));
        setTheme(new SimpleTheme(color.isBright() ? TextColor.ANSI.BLACK : TextColor.ANSI.WHITE_BRIGHT, color));
    }
}
