package io.github.defective4.javajam.tuisweeper.core;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import io.github.defective4.javajam.tuisweeper.core.sfx.*;
import io.github.defective4.javajam.tuisweeper.core.storage.Leaderboards;
import io.github.defective4.javajam.tuisweeper.core.storage.Preferences;
import io.github.defective4.javajam.tuisweeper.core.ui.*;

import javax.swing.JFrame;
import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TUISweeper {

    private static final DecimalFormat doubleFormat = new DecimalFormat("#.##");
    private static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final Screen screen;
    private final WindowBasedTextGUI gui;
    private final Terminal term;
    private final SFXEngine sfx;
    private final Window mainWindow = new SimpleWindow(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS);
    private final TextBox boardBox = new TextBox();
    private final MineBoard board = new MineBoard();

    private final Timer boardUpdater = new Timer(true);
    private final Preferences prefs;
    private final Leaderboards leaders = new Leaderboards();
    private final Label infoLabel;
    private long startTime = -1;
    private long endTime = -1;
    private boolean placed = false;
    private byte gameOver = 0;
    private Difficulty localDifficulty = Difficulty.EASY;

    public TUISweeper(Screen screen, WindowBasedTextGUI gui, Terminal term, SFXEngine sfx, Preferences prefs) {
        this.screen = screen;
        this.gui = gui;
        this.term = term;
        this.sfx = sfx;
        this.prefs = prefs;
        this.sfx.setEnabled(this.prefs.getOptions().isSounds());
        this.infoLabel = new Label("");
        updateTheme(this.prefs.getTheme());

        boardBox.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill,
                                                             LinearLayout.GrowPolicy.CanGrow));
        mainWindow.setComponent(Panels.vertical(infoLabel, boardBox));

        boardBox.setInputFilter((interactable, keyStroke) -> {
            boolean allowed = false;
            TerminalPosition pos = boardBox.getCaretPosition();

            int cx = pos.getColumn();
            int cy = pos.getRow();

            int absX = cx - board.getXOffset();
            int absY = cy - MineBoard.Y_OFFSET;

            switch (keyStroke.getKeyType()) {
                case Character: {
                    if (gameOver == 0 && absY >= 0 && absX >= 0 && absX < board.getSizeX() && absY < board.getSizeY())
                        switch (keyStroke.getCharacter()) {
                            case 'f': {
                                flag(absX, absY);
                                updateBoard();
                                break;
                            }
                            case ' ': {
                                int cnt = reveal(absX, absY);
                                updateBoard();
                                if (cnt > 0 && gameOver == 0) sfx.play(cnt > 10 ? "mass_reveal" : "reveal");
                                break;
                            }
                            default:
                                break;
                        }

                    switch (keyStroke.getCharacter()) {
                        case 'g': {
                            Window win = new SimpleWindow("Go to field...");

                            NumberBox x = new NumberBox(1, sfx);
                            NumberBox y = new NumberBox(1, sfx);
                            x.setMin(1);
                            y.setMin(1);
                            x.setMax(board.getSizeX());
                            y.setMax(board.getSizeY());

                            win.setComponent(Panels.grid(2,
                                                         new Label("Enter coordinates"),
                                                         new EmptySpace(),
                                                         new Label("Column (X)"),
                                                         new Label("Row (Y)"),
                                                         x,
                                                         y,
                                                         new EmptySpace(),
                                                         new EmptySpace(),
                                                         new SFXButton("Go", sfx, false, () -> {
                                                             boardBox.setCaretPosition(2 + y.getValue(),
                                                                                       board.getXOffset() + x.getValue() - 1);
                                                             win.close();
                                                         }),
                                                         new SFXButton("Cancel", sfx, true, win::close)));
                            gui.addWindow(win);
                            break;
                        }
                        case 'n': {
                            if (new SFXMessageDialogBuilder(sfx).setText(
                                                                        "Are you sure you want to start a new game?\nYour current session will get discarded.")
                                                                .setTitle("New Game")
                                                                .addButton(MessageDialogButton.No)
                                                                .addButton(MessageDialogButton.Yes)
                                                                .build()
                                                                .showDialog(gui) == MessageDialogButton.Yes) {
                                start();
                            }
                            break;
                        }
                        case 'm': {
                            Window win = new SimpleWindow("Game Menu");
                            Label text = new Label("");
                            text.setPreferredSize(new TerminalSize(32, 5));

                            Button game = new SFXButton("Game", sfx, () -> {
                                Window win2 = new SimpleWindow("Game settings");

                                Difficulty[] diffs = Difficulty.values();
                                SFXRadioBoxList<Difficulty> radio = new SFXRadioBoxList<>(sfx);
                                for (Difficulty dif : diffs)
                                    radio.addItem(dif);

                                NumberBox wBox = new NumberBox(board.getSizeX(), sfx);
                                NumberBox hBox = new NumberBox(board.getSizeY(), sfx);
                                NumberBox bBox = new NumberBox(board.getBombs(), sfx);

                                wBox.setMin(2);
                                hBox.setMin(2);
                                wBox.setMax(99);
                                hBox.setMax(99);

                                TextBox.TextChangeListener listener = (s, b) -> {
                                    if (b && radio.getCheckedItem() != Difficulty.CUSTOM)
                                        radio.setCheckedItem(Difficulty.CUSTOM);
                                };

                                wBox.setTextChangeListener(listener);
                                hBox.setTextChangeListener(listener);
                                bBox.setTextChangeListener(listener);

                                Button confirm = new SFXButton("Confirm", sfx, () -> {
                                    TUISweeper.this.prefs.setDifficulty(radio.getCheckedItem());
                                    TUISweeper.this.prefs.setWidth(wBox.getValue());
                                    TUISweeper.this.prefs.setHeight(hBox.getValue());
                                    TUISweeper.this.prefs.setBombs(bBox.getValue());
                                    prefs.save();

                                    win.close();
                                    win2.close();
                                    if (new SFXMessageDialogBuilder(sfx).setTitle("Start new game?")
                                                                        .addButton(MessageDialogButton.No)
                                                                        .addButton(MessageDialogButton.Yes)
                                                                        .setText(
                                                                                "The changes will take effect after starting a new game.\nDo you want to start a new game now?\n ")
                                                                        .build()
                                                                        .showDialog(gui) == MessageDialogButton.Yes) {
                                        start();
                                    }
                                });

                                radio.addListener((i, i1) -> {
                                    Difficulty sel = radio.getItemAt(i);
                                    if (sel != Difficulty.CUSTOM) {
                                        wBox.setValue(sel.getWidth());
                                        hBox.setValue(sel.getHeight());
                                        bBox.setValue(sel.getBombs());
                                        if (radio.isFocused()) confirm.takeFocus();
                                    } else if (radio.isFocused()) wBox.takeFocus();
                                });
                                radio.setCheckedItem(TUISweeper.this.prefs.getDifficulty());
                                win2.setComponent(Panels.grid(2,
                                                              Panels.vertical(radio),
                                                              Panels.vertical(new Label("Width"),
                                                                              wBox,
                                                                              new Label("\nHeight"),
                                                                              hBox,
                                                                              new Label("\nBombs"),
                                                                              bBox),
                                                              Panels.horizontal(confirm,
                                                                                new SFXButton("Cancel",
                                                                                              sfx,
                                                                                              true,
                                                                                              win2::close))));
                                gui.addWindow(win2);
                            }) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" |  Adjust game's difficulty\n" + " | \n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button theme = new SFXButton("Theme", sfx, () -> {
                                Window win2 = new SimpleWindow("Theming");

                                Preferences.UserTheme ut = TUISweeper.this.prefs.getTheme();
                                ColorChooserButton bfColor = new ColorChooserButton(TUISweeper.this.gui,
                                                                                    ut.getBaseForeground(),
                                                                                    sfx);
                                ColorChooserButton bbColor = new ColorChooserButton(TUISweeper.this.gui,
                                                                                    ut.getBaseBackground(),
                                                                                    sfx);
                                ColorChooserButton efColor = new ColorChooserButton(TUISweeper.this.gui,
                                                                                    ut.getEditableForeground(),
                                                                                    sfx);
                                ColorChooserButton ebColor = new ColorChooserButton(TUISweeper.this.gui,
                                                                                    ut.getEditableBackground(),
                                                                                    sfx);
                                ColorChooserButton sfColor = new ColorChooserButton(TUISweeper.this.gui,
                                                                                    ut.getSelectedForeground(),
                                                                                    sfx);
                                ColorChooserButton sbColor = new ColorChooserButton(TUISweeper.this.gui,
                                                                                    ut.getSelectedBackground(),
                                                                                    sfx);

                                Runnable save = () -> {
                                    ut.setBaseBackground(bbColor.getColor());
                                    ut.setBaseForeground(bfColor.getColor());
                                    ut.setSelectedBackground(sbColor.getColor());
                                    ut.setSelectedForeground(sfColor.getColor());
                                    ut.setEditableBackground(ebColor.getColor());
                                    ut.setEditableForeground(efColor.getColor());
                                    updateTheme(ut);
                                };

                                Button apply = new SFXButton("Apply", sfx, () -> {
                                    save.run();
                                    win2.close();
                                });

                                ComboBox<ThemePreset> presets = new SFXComboBox<ThemePreset>(sfx,
                                                                                             ThemePreset.values()) {
                                    @Override
                                    protected synchronized void afterLeaveFocus(FocusChangeDirection direction, Interactable nextInFocus) {
                                        super.afterLeaveFocus(direction, nextInFocus);
                                        setSelectedIndex(0);
                                    }
                                };
                                presets.addListener((i, i1, b) -> {
                                    ThemePreset preset = presets.getItem(i);
                                    if (preset != ThemePreset.NONE) {
                                        bfColor.setColor(preset.getBf());
                                        bbColor.setColor(preset.getBb());
                                        efColor.setColor(preset.getEf());
                                        ebColor.setColor(preset.getEb());
                                        sfColor.setColor(preset.getSf());
                                        sbColor.setColor(preset.getSb());
                                        apply.takeFocus();
                                    }
                                });

                                Button share = new SFXButton("Share theme", sfx, false, () -> {
                                    CustomFileDialogBuilder bd = (CustomFileDialogBuilder) new CustomFileDialogBuilder(
                                            true,
                                            sfx).setActionLabel("Save")
                                                .setDescription("Choose location to save your theme")
                                                .setTitle("Exporting theme")
                                                .setSelectedFile(new File("."));
                                    bd.setForcedExtension("json");
                                    File file = bd.buildAndShow(gui);
                                    if (file != null) {
                                        MessageDialogBuilder builder = new SFXMessageDialogBuilder(sfx);
                                        builder.setTitle("Exporting theme");
                                        builder.addButton(MessageDialogButton.OK);
                                        try (OutputStream os = Files.newOutputStream(file.toPath())) {
                                            save.run();
                                            os.write(prefs.getTheme().toJSON().getBytes(StandardCharsets.UTF_8));
                                            builder.setText("Theme exported to " + file);
                                        } catch (Exception e) {
                                            builder.setText("An error occured while exporting theme");
                                            e.printStackTrace();
                                        }
                                        builder.build().showDialog(gui);
                                    }
                                });

                                Button imp = new SFXButton("Import theme", sfx, false, () -> {
                                    CustomFileDialogBuilder builder = (CustomFileDialogBuilder) new CustomFileDialogBuilder(
                                            sfx).setSelectedFile(new File("theme.json"))
                                                .setTitle("Importing a theme")
                                                .setDescription("Select a theme file to import")
                                                .setActionLabel("Import");
                                    builder.setForcedExtension("json");

                                    File file = builder.buildAndShow(gui);
                                    if (file != null) {
                                        MessageDialogBuilder md = new SFXMessageDialogBuilder(sfx);
                                        md.addButton(MessageDialogButton.OK);
                                        md.setText("Importing theme");
                                        if (file.isFile()) {
                                            try (FileReader rdr = new FileReader(file)) {
                                                Preferences.UserTheme imported = Preferences.UserTheme.fromJSON(rdr);
                                                if (imported == null || !imported.isValid())
                                                    throw new IOException("Invalid theme");
                                                updateTheme(imported);
                                                prefs.getTheme().fromTheme(imported);
                                                md.setText("Theme imported!");
                                                win2.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                md.setText("There was an error while importing the theme");
                                            }
                                        } else {
                                            md.setText("This file does not exist!");
                                        }
                                        md.build().showDialog(gui);
                                    }
                                });

                                win2.setComponent(Panels.grid(2,
                                                              new Label("Choose a preset"),
                                                              new EmptySpace(),
                                                              presets,
                                                              new EmptySpace(),
                                                              new Label("\nBase color"),
                                                              new Label("\nBase background"),
                                                              bfColor,
                                                              bbColor,
                                                              new Label("\nField color"),
                                                              new Label("\nField background"),
                                                              efColor,
                                                              ebColor,
                                                              new Label("\nSelected color    "),
                                                              new Label("\nSelected background"),
                                                              sfColor,
                                                              sbColor,
                                                              new Label("\n "),
                                                              new Label("\n "),
                                                              Panels.horizontal(share, imp),
                                                              new EmptySpace(),
                                                              new EmptySpace(),
                                                              new EmptySpace(),
                                                              apply,
                                                              new SFXButton("Cancel", sfx, true, win2::close)));
                                gui.addWindow(win2);
                            }) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " |  Customize game's appearance\n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button leaderboards = new SFXButton("Leaderboards", sfx, this::displayLeaderboards) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " | \n" + " |  Show top times by difficulty\n" + " |");
                                }
                            };
                            Button done = new SFXButton("Done", sfx, true, win::close) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " | \n" + " | \n" + " |  Close this menu");
                                }
                            };
                            Button options = new SFXButton("Options", sfx, () -> {
                                Window win2 = new SimpleWindow("Options");
                                Preferences.Options ops = TUISweeper.this.prefs.getOptions();
                                boolean sndAvailable = sfx.isAvailable();

                                CheckBox shaking = new SFXCheckBox("Enable screen shaking", ops.isScreenShaking(), sfx);
                                CheckBox sounds = new SFXCheckBox("Enable sounds", ops.isSounds() && sndAvailable, sfx);
                                sounds.setEnabled(sndAvailable);

                                win2.setComponent(Panels.vertical(shaking,
                                                                  sounds,
                                                                  new EmptySpace(),
                                                                  Panels.horizontal(new SFXButton("Confirm",
                                                                                                  sfx,
                                                                                                  () -> {
                                                                                                      ops.setScreenShaking(
                                                                                                              shaking.isChecked());
                                                                                                      ops.setSounds(
                                                                                                              sounds.isChecked());
                                                                                                      sfx.setEnabled(ops.isSounds());
                                                                                                      prefs.save();
                                                                                                      win2.close();
                                                                                                  }),
                                                                                    new SFXButton("Cancel",
                                                                                                  sfx,
                                                                                                  true,
                                                                                                  win2::close))));
                                gui.addWindow(win2);
                            }) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " |  Adjust game settings\n" + " | \n" + " |");
                                }
                            };

                            win.setComponent(Panels.grid(2,
                                                         Panels.vertical(game, theme, options, leaderboards, done),
                                                         text));
                            this.gui.addWindow(win);
                            break;
                        }
                        case 'q': {
                            if (new SFXMessageDialogBuilder(sfx).setText("Are you sure you want to discard\n" + "current game and close the application?\n ")
                                                                .setTitle("Quiting the game")
                                                                .addButton(MessageDialogButton.No)
                                                                .addButton(MessageDialogButton.Yes)
                                                                .build()
                                                                .showDialog(gui) == MessageDialogButton.Yes) {
                                System.exit(0);
                            }
                            break;
                        }
                        default:
                            break;
                    }

                    break;
                }
                case ArrowLeft: {
                    cx--;
                    allowed = true;
                    break;
                }
                case ArrowRight: {
                    cx++;
                    allowed = true;
                    break;
                }
                case ArrowUp: {
                    cy--;
                    allowed = true;
                    break;
                }
                case ArrowDown: {
                    cy++;
                    allowed = true;
                    break;
                }
                default:
                    break;
            }

            if (allowed) updateBoard(cx, cy);
            return allowed;
        });

        boardUpdater.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateBoard();
            }
        }, 250, 250);
    }

    public static String capitalize(Enum<?> en) {
        String[] split = en.name().split("_");
        for (int x = 0; x < split.length; x++)
            split[x] = split[x].substring(0, 1).toUpperCase() + split[x].substring(1).toLowerCase();
        return String.join(" ", split);
    }

    private void updateTheme(Preferences.UserTheme theme) {
        gui.setTheme(theme.toTUITheme());
        prefs.save();
        infoLabel.setTheme(new SimpleTheme(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE_BRIGHT));
    }

    public void shake() {
        if (!prefs.getOptions().isScreenShaking()) return;
        if (term instanceof JFrame) {
            JFrame frame = (JFrame) term;
            Point original = frame.getLocation();
            boardUpdater.scheduleAtFixedRate(new TimerTask() {
                int x = 50;

                @Override
                public void run() {
                    frame.setLocation((int) (original.getX() + (x % 2 == 0 ? 2 : -2)), (int) original.getY());
                    x--;
                    if (x <= 0) {
                        cancel();
                        frame.setLocation(original);
                    }
                }
            }, 10, 10);
        }
    }

    public String getCurrentPlayingTime() {
        long diff = startTime == -1 ? 0 : endTime == -1 ? System.currentTimeMillis() - startTime : endTime - startTime;
        return new SimpleDateFormat("mm:ss").format(new Date(diff));
    }

    public void flag(int x, int y) {
        int[] fields = board.countAllFields(11, 12);
        byte current = board.getFieldAt(x, y);
        byte c = -1;
        switch (current) {
            case 0: {
                if (fields[0] - fields[1] <= 0) break;
                sfx.play("flag");
                c = 12;
                break;
            }
            case 11: {
                if (fields[0] - fields[1] <= 0) break;
                sfx.play("flag");
                c = 13;
                break;
            }
            case 12: {
                sfx.play("unflag");
                c = 0;
                break;
            }
            case 13: {
                sfx.play("unflag");
                c = 11;
                break;
            }
            default: {
                break;
            }
        }
        if (c > -1) {
            startTimer();
            board.setFieldAt(x, y, c);
        }
    }

    public int reveal(int x, int y) {
        return reveal(x, y, false);
    }

    public int reveal(int x, int y, boolean revealFlags) {
        startTimer();
        int count = 0;
        byte current = board.getFieldAt(x, y);
        if (!placed) {
            placed = true;
            if (current == 11) {
                int i, j;
                byte c;
                int maxTries = 100;
                Random rand = board.getRand();
                do {
                    i = rand.nextInt(board.getSizeX());
                    j = rand.nextInt(board.getSizeY());
                    c = board.getFieldAt(i, j);
                    maxTries--;
                } while (c != 0 && maxTries > 0);
                if (c != 0) {
                    for (int l = 0; l < board.getSizeX(); l++)
                        for (int m = 0; m < board.getSizeY(); m++) {
                            if (board.getFieldAt(l, m) == 0) {
                                c = 0;
                                break;
                            }
                        }
                }
                if (c == 0) {
                    board.setFieldAt(i, j, 11);
                    board.setFieldAt(x, y, 0);
                    current = 0;
                }
            }
        }
        if (current == 0 || (revealFlags && current == 12)) {
            int bombs = board.countBombs(x, y);
            if (bombs == 0) {
                board.setFieldAt(x, y, 10);
                for (int i = x - 1; i <= x + 1; i++)
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i >= 0 && j >= 0 && i < board.getSizeX() && j < board.getSizeY()) {
                            byte c = board.getFieldAt(i, j);
                            if (c == 0 || c == 12) count += reveal(i, j, true);
                        }
                    }
            } else {
                board.setFieldAt(x, y, bombs);
            }
            count++;
        } else if (current > 0 && current < 10) {
            int flags = board.countFlags(x, y);
            if (flags == current) {
                for (int i = x - 1; i <= x + 1; i++)
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i >= 0 && j >= 0 && i < board.getSizeX() && j < board.getSizeY()) {
                            byte c = board.getFieldAt(i, j);
                            if (c == 0 || c == 11) count += reveal(i, j, true);
                        }
                    }
            }
        } else if (current == 11) {
            board.revealMines();
            gameOver = 1;
            endTime = System.currentTimeMillis();
            sfx.play("bomb");
            shake();
        }
        return count;
    }

    public void show() {
        gui.addWindow(mainWindow);
    }

    public void displayLeaderboards() {
        Window win = new SimpleWindow("Leaderboards");

        Table<String> table = new Table<>("#", "Time", "Date (yy-mm-dd hh:mm)");
        table.setPreferredSize(new TerminalSize(35, 11));

        ComboBox<Difficulty> diff = new SFXComboBox<>(sfx, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
        Difficulty current = prefs.getDifficulty();
        diff.addListener((i, i1, b) -> {
            Difficulty sel = diff.getItem(i);
            int index = 0;
            table.getTableModel().clear();
            Leaderboards.Entry[] entries = leaders.getEntries(sel, 10);
            if (entries.length == 0) {
                table.getTableModel().addRow("", "No entries", "");
            } else for (Leaderboards.Entry et : entries) {
                index++;
                table.getTableModel()
                     .addRow(Integer.toString(index),
                             new SimpleDateFormat("mm:ss").format(new Date(et.getTime())),
                             new SimpleDateFormat("(yy-MM-dd kk:mm)").format(new Date(et.getDate())));
            }
        });
        diff.setSelectedItem(current == Difficulty.CUSTOM ? Difficulty.EASY : current);

        win.setComponent(Panels.vertical(new Label("Difficulty"),
                                         diff,
                                         new EmptySpace(),
                                         table,
                                         new EmptySpace(),
                                         new SFXButton("Close", sfx, true, win::close)));
        gui.addWindow(win);
    }

    public void start() {
        board.initialize(prefs.getWidth(), prefs.getHeight(), prefs.getBombs());
        resetVariables();
        updateBoard();
        boardBox.setCaretPosition(MineBoard.Y_OFFSET, board.getXOffset());
    }

    private void startTimer() {
        if (startTime == -1) startTime = System.currentTimeMillis();
    }

    private void endTimer() {
        if (endTime == -1 && startTime == -1) endTime = System.currentTimeMillis();
    }

    private void resetVariables() {
        startTime = -1;
        endTime = -1;
        placed = false;
        gameOver = 0;
        localDifficulty = prefs.getDifficulty();
    }

    private void updateBoard() {
        TerminalPosition caret = boardBox.getCaretPosition();
        updateBoard(caret.getColumn(), caret.getRow());
    }

    private void updateBoard(int cx, int cy) {
        StringBuilder builder = new StringBuilder();

        int numberLen = board.getMaxSizeLen();
        int offsetX = MineBoard.X_OFFSET + numberLen;

        String bombs;
        double percent;
        if (gameOver > 0) {
            bombs = "0";
            percent = gameOver == 2 ? 1 : 0;
        } else {
            int[] fields = board.countAllFields(11, 12, 0, 13);
            if (fields[0] == 0 && fields[1] == 0 && fields[2] == 0) {
                gameOver = 2;
                sfx.play("win");
                endTime = System.currentTimeMillis();
                Difficulty diff = prefs.getDifficulty();
                if (diff != Difficulty.CUSTOM) {
                    leaders.addEntry(diff, endTime - startTime);
                }
                Window win = new SimpleWindow("Game won");
                Panel ctl = Panels.horizontal(new Button("Close", win::close));
                if (diff != Difficulty.CUSTOM)
                    ctl.addComponent(new SFXButton("Leaderboards", sfx, this::displayLeaderboards));

                win.setComponent(Panels.vertical(new Label("You won!\n" + "Your time is " + getCurrentPlayingTime()),
                                                 new EmptySpace(),
                                                 ctl

                ));
                gui.addWindow(win);
            }
            percent = (double) (fields[2] + fields[0]) / (board.getSizeX() * board.getSizeY());
            percent = 1 - percent;
            bombs = Integer.toString(fields[0] - fields[1]);
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < board.getSizeX() + offsetX; j++) {
                builder.append(j < offsetX ?
                               ' ' :
                               i == 0 ?
                               j == cx ? 'v' : ' ' :
                               i == 1 ? CHARS[(j - offsetX) % CHARS.length] : '=');
            }
            switch (i) {
                case 0: {
                    builder.append("      ");
                    int stripes = (int) ((double) 23 * percent);
                    for (int x = 0; x < 23; x++)
                        builder.append(stripes > x ? "▰" : "▱");
                    break;
                }
                case 1: {
                    builder.append("    Timer   Bombs   Cleared");
                    break;
                }
                case 2: {
                    String time = getCurrentPlayingTime();

                    StringBuilder space = new StringBuilder();
                    for (int x = 0; x < 8 - bombs.length(); x++)
                        space.append(" ");

                    builder.append("    ")
                           .append(time)
                           .append("   ")
                           .append(bombs)
                           .append(space)
                           .append(doubleFormat.format(percent * 100))
                           .append('%');
                    break;
                }
                default: {
                    break;
                }
            }
            builder.append("\n");
        }

        for (int i = 0; i < board.getSizeY(); i++) {
            String line = Integer.toString(i + 1);
            builder.append(i + 3 == cy ? '>' : ' ');
            for (int j = 1; j <= numberLen - line.length(); j++)
                builder.append(' ');
            builder.append(line).append("|");

            for (int j = 0; j < board.getSizeX(); j++) {
                builder.append(board.getFieldCharAt(j, i));
            }

            builder.append("\n");
        }

        builder.append("\n    ")
               .append(gameOver == 0 ? "" : gameOver == 2 ? "You won!" : "GAME OVER")
               .append("\n")
               .append("    ")
               .append("\n")
               .append("    n - New Game     g - Go to field...\n")
               .append("    m - Game Menu\n")
               .append("    q - Quit");

        boardBox.setText(builder.toString());

        StringBuilder labelText = new StringBuilder();

        String emote = gameOver == 0 ? ":)" : gameOver == 1 ? "X(" : "B)";
        labelText.append("  ").append(emote).append("   TUI-Sweeper    Difficulty: ").append(localDifficulty);
        int wh = screen.getTerminalSize().getColumns();
        for (int x = labelText.length(); x < wh; x++)
            labelText.append(" ");
        infoLabel.setText(labelText.toString());
    }
}
