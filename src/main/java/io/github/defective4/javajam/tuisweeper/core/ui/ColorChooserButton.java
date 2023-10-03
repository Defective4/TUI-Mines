package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXButton;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXEngine;
import io.github.defective4.javajam.tuisweeper.core.util.ColorConverter;

import static com.googlecode.lanterna.TextColor.ANSI;

/**
 * A special button used to choose colors.
 * It can be used to choose predefined {@link ANSI} colors
 * and custom {@link com.googlecode.lanterna.TextColor.RGB}
 *
 * @author Defective
 */
public final class ColorChooserButton extends SFXButton {

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
                btn.setTheme(new SimpleTheme(ColorConverter.isDark(c) ? ANSI.WHITE_BRIGHT : ANSI.BLACK, c));
                colorPanel.addComponent(btn);
            }

            win.setComponent(Panels.vertical(colorPanel,
                                             new EmptySpace(),
                                             Panels.horizontal(new SFXButton("Custom color", sfx, false, () -> {
                                                 Window cw = new SimpleWindow("Make a custom color");

                                                 TextColor cc = getColor();
                                                 NumberBox r = new NumberBox(cc.getRed(), sfx);
                                                 NumberBox g = new NumberBox(cc.getGreen(), sfx);
                                                 NumberBox b = new NumberBox(cc.getBlue(), sfx);

                                                 Button cApply = new SFXButton("Confirm", sfx, false, () -> {
                                                     setColor(ColorConverter.optimize(new TextColor.RGB(r.getValue(),
                                                                                                        g.getValue(),
                                                                                                        b.getValue())));
                                                     cw.close();
                                                     win.close();
                                                 });

                                                 r.setMin(0);
                                                 r.setMax(255);
                                                 g.setMin(0);
                                                 g.setMax(255);
                                                 b.setMin(0);
                                                 b.setMax(255);

                                                 cw.setComponent(Panels.vertical(new Label("Enter color values below:"),
                                                                                 new EmptySpace(),
                                                                                 Panels.grid(3,
                                                                                             new Label("Red"),
                                                                                             new Label("Green"),
                                                                                             new Label("Blue"),
                                                                                             r,
                                                                                             g,
                                                                                             b),
                                                                                 new EmptySpace(),
                                                                                 Panels.horizontal(cApply,
                                                                                                   new SFXButton(
                                                                                                           "Cancel",
                                                                                                           sfx,
                                                                                                           true,
                                                                                                           cw::close))));
                                                 gui.addWindow(cw);
                                             }), new SFXButton("Cancel", sfx, true, win::close))));
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
        setTheme(new SimpleTheme(ColorConverter.isDark(color) ? ANSI.WHITE_BRIGHT : ANSI.BLACK, color));
    }
}
