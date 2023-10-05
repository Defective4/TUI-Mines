package io.github.defective4.javajam.tuisweeper.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import io.github.defective4.javajam.tuisweeper.core.Difficulty;
import io.github.defective4.javajam.tuisweeper.core.ui.ThemePreset;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.googlecode.lanterna.TextColor.ANSI;
import static com.googlecode.lanterna.TextColor.RGB;
import static io.github.defective4.javajam.tuisweeper.core.util.ColorConverter.optimize;
import static io.github.defective4.javajam.tuisweeper.core.util.ColorConverter.toRGB;

/**
 * Stores user's game preferences, theme and options
 *
 * @author Defective
 */
public class Preferences {

    /**
     * A user-defined theme entry
     */
    public static class UserTheme {

        private RGB baseForeground, baseBackground, editableForeground, editableBackground, selectedForeground, selectedBackground;

        public UserTheme(TextColor baseForeground, TextColor baseBackground, TextColor editableForeground, TextColor editableBackground, TextColor selectedForeground, TextColor selectedBackground) {
            setBaseForeground(baseForeground);
            setBaseBackground(baseBackground);
            setEditableBackground(editableBackground);
            setEditableForeground(editableForeground);
            setSelectedBackground(selectedBackground);
            setSelectedForeground(selectedForeground);
        }

        public static UserTheme fromJSON(Reader reader) {
            try {
                return new Gson().fromJson(reader, UserTheme.class);
            } catch (Exception e) {
                return null;
            }
        }

        public void fromTheme(UserTheme target) {
            setBaseBackground(target.getBaseBackground());
            setBaseForeground(target.getBaseForeground());
            setSelectedForeground(target.getSelectedForeground());
            setSelectedBackground(target.getSelectedBackground());
            setEditableForeground(target.getEditableForeground());
            setEditableBackground(target.getEditableBackground());
        }

        public boolean isValid() {
            return baseForeground != null && baseBackground != null && editableBackground != null && editableForeground != null && selectedBackground != null && selectedForeground != null;
        }

        public TextColor getBaseForeground() {
            return optimize(baseForeground);
        }

        public void setBaseForeground(TextColor baseForeground) {
            this.baseForeground = toRGB(baseForeground);
        }

        public TextColor getBaseBackground() {
            return optimize(baseBackground);
        }

        public void setBaseBackground(TextColor baseBackground) {
            this.baseBackground = toRGB(baseBackground);
        }

        public TextColor getEditableForeground() {
            return optimize(editableForeground);
        }

        public void setEditableForeground(TextColor editableForeground) {
            this.editableForeground = toRGB(editableForeground);
        }

        public TextColor getEditableBackground() {
            return optimize(editableBackground);
        }

        public void setEditableBackground(TextColor editableBackground) {
            this.editableBackground = toRGB(editableBackground);
        }

        public TextColor getSelectedForeground() {
            return optimize(selectedForeground);
        }

        public void setSelectedForeground(TextColor selectedForeground) {
            this.selectedForeground = toRGB(selectedForeground);
        }

        public TextColor getSelectedBackground() {
            return optimize(selectedBackground);
        }

        public void setSelectedBackground(TextColor selectedBackground) {
            this.selectedBackground = toRGB(selectedBackground);
        }

        public Theme toTUITheme() {
            return SimpleTheme.makeTheme(true,
                                         getBaseForeground(),
                                         getBaseBackground(),
                                         getEditableForeground(),
                                         getEditableBackground(),
                                         getSelectedForeground(),
                                         getSelectedBackground(),
                                         ANSI.BLACK);
        }

        public String toJSON() {
            JsonObject obj = new Gson().toJsonTree(this).getAsJsonObject();
            obj.add("themeVersion", new JsonPrimitive(1));
            return new GsonBuilder().setPrettyPrinting().create().toJson(obj);
        }
    }

    /**
     * Stores user's options
     */
    public static class Options {
        private boolean screenShaking = true;
        private boolean sounds = true;
        private boolean discordIntegration = true;
        private boolean isFlagOnly;

        public boolean isFlagOnly() {
            return isFlagOnly;
        }

        public void setFlagOnly(boolean flagOnly) {
            isFlagOnly = flagOnly;
        }

        public boolean isDiscordIntegrationEnabled() {
            return discordIntegration;
        }

        public void setDiscordIntegration(boolean discordIntegration) {
            this.discordIntegration = discordIntegration;
        }

        public boolean areSoundsEnabled() {
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

    /**
     * Keeps track on what dialogs were seen by the user
     */
    public static class OneTimeDialogs {
        private boolean seenPlayStyleDialog;


        public boolean seenPlayStyleDialog() {
            boolean old = seenPlayStyleDialog;
            seenPlayStyleDialog = true;
            return old;
        }
    }

    private UserTheme theme = ThemePreset.PITCH_BLACK.toTheme();
    private Options options = new Options();
    private OneTimeDialogs otd = new OneTimeDialogs();

    private boolean firstBoot = true;
    private Difficulty difficulty = Difficulty.EASY;
    private int width = difficulty.getWidth();
    private int height = difficulty.getHeight();
    private int bombs = difficulty.getBombs();

    public Preferences() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public OneTimeDialogs getOneTimeDialogs() {
        if (otd == null) otd = new OneTimeDialogs();
        return otd;
    }

    public static File getConfigDirectory() {
        String subdir = System.getProperty("os.name").toLowerCase().contains("windows") ?
                        "AppData/Roaming/TUIMines" :
                        ".config/tuimines";
        return new File(System.getProperty("user.home") + "/" + subdir);
    }

    public static File getConfigFile() {
        return new File(getConfigDirectory(), "config.json");
    }

    public static File getDatabaseFile() {
        return new File(getConfigDirectory(), "storage.db");
    }

    public static File getReplaysDir() {
        return new File(getConfigDirectory(), "replays");
    }

    public static Preferences load() throws Exception {
        File in = getConfigFile();
        if (in.isFile()) try (InputStreamReader reader = new FileReader(in)) {
            Preferences prefs = new Gson().fromJson(reader, Preferences.class);
            if (!prefs.getTheme().isValid()) prefs.getTheme().fromTheme(ThemePreset.PITCH_BLACK.toTheme());
            return prefs;
        }
        Preferences clean = new Preferences();
        clean.save();
        return clean;
    }

    public void save() throws IOException {
        File out = getConfigFile();
        out.getParentFile().mkdirs();
        try (OutputStream os = Files.newOutputStream(out.toPath())) {
            os.write(new Gson().toJson(this).getBytes(StandardCharsets.UTF_8));
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
