package io.github.defective4.javajam.tuisweeper.core.util;

import com.googlecode.lanterna.TextColor;

public final class ColorConverter {
    public static TextColor.RGB toRGB(TextColor color) {
        if (color instanceof TextColor.RGB) return (TextColor.RGB) color;
        else return new TextColor.RGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static TextColor optimize(TextColor color) {
        if (color instanceof TextColor.ANSI) return color;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        for (TextColor.ANSI ans : TextColor.ANSI.values()) {
            if (ans.getBlue() == b && ans.getGreen() == g && ans.getRed() == r) return ans;
        }
        return color;
    }

    public static boolean isDark(TextColor color) {
        double luma = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.144 * color.getBlue();
        return luma < 0.5;
    }
}
