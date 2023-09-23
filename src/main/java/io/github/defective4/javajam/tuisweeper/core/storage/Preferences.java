package io.github.defective4.javajam.tuisweeper.core.storage;

import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import io.github.defective4.javajam.tuisweeper.core.Difficulty;

import static com.googlecode.lanterna.TextColor.ANSI;

public class Preferences {

    public static class UserTheme {

        private ANSI baseForeground, baseBackground, editableForeground, editableBackground, selectedForeground, selectedBackground;

        public UserTheme(ANSI baseForeground, ANSI baseBackground, ANSI editableForeground, ANSI editableBackground, ANSI selectedForeground, ANSI selectedBackground) {
            this.baseForeground = baseForeground;
            this.baseBackground = baseBackground;
            this.editableForeground = editableForeground;
            this.editableBackground = editableBackground;
            this.selectedForeground = selectedForeground;
            this.selectedBackground = selectedBackground;
        }

        public ANSI getBaseForeground() {
            return baseForeground;
        }

        public void setBaseForeground(ANSI baseForeground) {
            this.baseForeground = baseForeground;
        }

        public ANSI getBaseBackground() {
            return baseBackground;
        }

        public void setBaseBackground(ANSI baseBackground) {
            this.baseBackground = baseBackground;
        }

        public ANSI getEditableForeground() {
            return editableForeground;
        }

        public void setEditableForeground(ANSI editableForeground) {
            this.editableForeground = editableForeground;
        }

        public ANSI getEditableBackground() {
            return editableBackground;
        }

        public void setEditableBackground(ANSI editableBackground) {
            this.editableBackground = editableBackground;
        }

        public ANSI getSelectedForeground() {
            return selectedForeground;
        }

        public void setSelectedForeground(ANSI selectedForeground) {
            this.selectedForeground = selectedForeground;
        }

        public ANSI getSelectedBackground() {
            return selectedBackground;
        }

        public void setSelectedBackground(ANSI selectedBackground) {
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
                                         ANSI.BLACK);
        }
    }

    private final UserTheme theme = new UserTheme(ANSI.WHITE_BRIGHT,
                                                  ANSI.BLACK,
                                                  ANSI.WHITE_BRIGHT,
                                                  ANSI.BLACK,
                                                  ANSI.BLACK,
                                                  ANSI.WHITE_BRIGHT);
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
