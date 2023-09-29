package io.github.defective4.javajam.tuisweeper.core.util;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.Component;

public final class ColorConverter {
    private ColorConverter() {
    }

    public static void applyBackground(TextColor color, Component cpt) {
        cpt.setTheme(new SimpleTheme(isDark(color) ? ANSI.WHITE_BRIGHT : ANSI.BLACK, color));
    }

    public static TextColor.RGB toRGB(TextColor color) {
        if (color instanceof TextColor.RGB) return (TextColor.RGB) color;
        else return new TextColor.RGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static TextColor optimize(TextColor color) {
        if (color instanceof ANSI) return color;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        for (ANSI ans : ANSI.values()) {
            if (ans.getBlue() == b && ans.getGreen() == g && ans.getRed() == r) return ans;
        }
        return color;
    }

    public static boolean isDark(TextColor color) {
        double luma = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.144 * color.getBlue()) / 255;
        return luma < 0.5;
    }
}
