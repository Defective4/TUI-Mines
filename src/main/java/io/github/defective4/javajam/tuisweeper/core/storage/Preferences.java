package io.github.defective4.javajam.tuisweeper.core.storage;

import com.google.gson.Gson;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import io.github.defective4.javajam.tuisweeper.core.Difficulty;
import io.github.defective4.javajam.tuisweeper.core.ui.ThemePreset;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

    public static class Options {
        private boolean screenShaking = true;
        private boolean sounds = true;

        public boolean isSounds() {
            return sounds;
        }

        public void setSounds(boolean sounds) {
            this.sounds = sounds;
        }

        public boolean isScreenShaking() {
            return screenShaking;
        }

        public void setScreenShaking(boolean screenShaking) {
            this.screenShaking = screenShaking;
        }
    }

    private UserTheme theme = ThemePreset.PITCH_BLACK.toTheme();
    private Options options = new Options();

    private boolean firstBoot = true;
    private Difficulty difficulty = Difficulty.EASY;
    private int width = difficulty.getWidth();
    private int height = difficulty.getHeight();
    private int bombs = difficulty.getBombs();

    public Preferences() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    public static File getConfigDirectory() {
        String subdir = System.getProperty("os.name").toLowerCase().contains("windows") ?
                "AppData/Roaming/TUISweeper" :
                ".config/tuisweeper";
        return new File(System.getProperty("user.home") + "/" + subdir);
    }

    public static File getConfigFile() {
        return new File(getConfigDirectory(), "config.json");
    }

    public static Preferences load() {
        File in = getConfigFile();
        if (in.isFile()) try (InputStreamReader reader = new FileReader(in)) {
            return new Gson().fromJson(reader, Preferences.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Preferences clean = new Preferences();
        clean.save();
        return clean;
    }

    public void save() {
        File out = getConfigFile();
        out.getParentFile().mkdirs();
        try (OutputStream os = Files.newOutputStream(out.toPath())) {
            os.write(new Gson().toJson(this).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isFirstBoot() {
        return firstBoot;
    }

    public void setFirstBoot(boolean firstBoot) {
        this.firstBoot = firstBoot;
    }

    public Options getOptions() {
        if (options == null) options = new Options();
        return options;
    }

    public int getWidth() {
        return Math.max(width, 1);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return Math.max(height, 1);
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBombs() {
        return Math.max(bombs, 1);
    }

    public void setBombs(int bombs) {
        this.bombs = bombs;
    }

    public UserTheme getTheme() {
        if (theme == null) theme = ThemePreset.PITCH_BLACK.toTheme();
        return theme;
    }

    public Difficulty getDifficulty() {
        if (difficulty == null) difficulty = Difficulty.EASY;
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}
