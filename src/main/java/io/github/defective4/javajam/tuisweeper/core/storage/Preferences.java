package io.github.defective4.javajam.tuisweeper.core.storage;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import io.github.defective4.javajam.tuisweeper.core.Difficulty;

public class Preferences {

    public static class UserTheme {

        private TextColor.ANSI baseForeground, baseBackground, editableForeground, editableBackground, selectedForeground, selectedBackground;

        public UserTheme(TextColor.ANSI baseForeground, TextColor.ANSI baseBackground, TextColor.ANSI editableForeground, TextColor.ANSI editableBackground, TextColor.ANSI selectedForeground, TextColor.ANSI selectedBackground) {
            this.baseForeground = baseForeground;
            this.baseBackground = baseBackground;
            this.editableForeground = editableForeground;
            this.editableBackground = editableBackground;
            this.selectedForeground = selectedForeground;
            this.selectedBackground = selectedBackground;
        }

        public TextColor.ANSI getBaseForeground() {
            return baseForeground;
        }

        public void setBaseForeground(TextColor.ANSI baseForeground) {
            this.baseForeground = baseForeground;
        }

        public TextColor.ANSI getBaseBackground() {
            return baseBackground;
        }

        public void setBaseBackground(TextColor.ANSI baseBackground) {
            this.baseBackground = baseBackground;
        }

        public TextColor.ANSI getEditableForeground() {
            return editableForeground;
        }

        public void setEditableForeground(TextColor.ANSI editableForeground) {
            this.editableForeground = editableForeground;
        }

        public TextColor.ANSI getEditableBackground() {
            return editableBackground;
        }

        public void setEditableBackground(TextColor.ANSI editableBackground) {
            this.editableBackground = editableBackground;
        }

        public TextColor.ANSI getSelectedForeground() {
            return selectedForeground;
        }

        public void setSelectedForeground(TextColor.ANSI selectedForeground) {
            this.selectedForeground = selectedForeground;
        }

        public TextColor.ANSI getSelectedBackground() {
            return selectedBackground;
        }

        public void setSelectedBackground(TextColor.ANSI selectedBackground) {
            this.selectedBackground = selectedBackground;
        }

        public Theme toTUITheme() {
            return SimpleTheme.makeTheme(true,
                                         baseForeground,
                                         baseBackground,
                                         editableForeground,
                                         editableBackground,
                                         selectedForeground,
                                         selectedBackground,
                                         TextColor.ANSI.BLACK);
        }
    }

    private final UserTheme theme = new UserTheme(TextColor.ANSI.WHITE_BRIGHT,
                                                  TextColor.ANSI.BLACK,
                                                  TextColor.ANSI.WHITE_BRIGHT,
                                                  TextColor.ANSI.BLACK,
                                                  TextColor.ANSI.BLACK,
                                                  TextColor.ANSI.WHITE_BRIGHT);
    private Difficulty difficulty = Difficulty.EASY;
    private int width = difficulty.getWidth();
    private int height = difficulty.getHeight();
    private int bombs = difficulty.getBombs();

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBombs() {
        return bombs;
    }

    public void setBombs(int bombs) {
        this.bombs = bombs;
    }

    public UserTheme getTheme() {
        return theme;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}
