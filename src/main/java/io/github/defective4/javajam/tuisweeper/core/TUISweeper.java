package io.github.defective4.javajam.tuisweeper.core;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import io.github.defective4.javajam.tuisweeper.core.network.RemoteReplay;
import io.github.defective4.javajam.tuisweeper.core.network.RemoteTheme;
import io.github.defective4.javajam.tuisweeper.core.network.Repository;
import io.github.defective4.javajam.tuisweeper.core.replay.Replay;
import io.github.defective4.javajam.tuisweeper.core.replay.ReplayIO;
import io.github.defective4.javajam.tuisweeper.core.replay.ReplayPlayer;
import io.github.defective4.javajam.tuisweeper.core.replay.ReplayRecorder;
import io.github.defective4.javajam.tuisweeper.core.sfx.*;
import io.github.defective4.javajam.tuisweeper.core.storage.Leaderboards;
import io.github.defective4.javajam.tuisweeper.core.storage.Preferences;
import io.github.defective4.javajam.tuisweeper.core.ui.*;

import java.awt.Point;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.defective4.javajam.tuisweeper.core.replay.Replay.*;
import static io.github.defective4.javajam.tuisweeper.core.ui.ErrorDialog.showErrorDialog;
import static io.github.defective4.javajam.tuisweeper.core.util.ColorConverter.applyBackground;

/**
 * The main game class.
 * Most of the game and application logic happens here.
 */
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
    private final ReplayRecorder recorder;
    private final ReplayPlayer player;
    private final Leaderboards leaders = new Leaderboards();
    private final Repository remoteRepo = new Repository();
    private final Label infoLabel;
    private long startTime = -1;
    private long endTime = -1;
    private boolean placed;
    private byte gameOver;
    private Difficulty localDifficulty = Difficulty.EASY;
    private boolean isReplay;
    private Replay currentReplay;

    public TUISweeper(Screen screen, WindowBasedTextGUI gui, Terminal term, SFXEngine sfx, Preferences prefs) {
        this.screen = screen;
        this.gui = gui;
        this.term = term;
        this.sfx = sfx;
        this.prefs = prefs;
        this.recorder = new ReplayRecorder(board, this);
        this.sfx.setEnabled(this.prefs.getOptions().isSounds());
        this.infoLabel = new Label("");
        this.player = new ReplayPlayer(this, boardBox, sfx);
        updateTheme(this.prefs.getTheme());

        boardBox.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill,
                                                             LinearLayout.GrowPolicy.CanGrow));
        mainWindow.setComponent(Panels.vertical(infoLabel, boardBox));

        boardBox.setInputFilter((interactable, keyStroke) -> {
            if (isReplay) {
                if (keyStroke.getKeyType() == KeyType.Character && keyStroke.getCharacter() == 'n') {
                    start();
                }
                if (gameOver == 0)
                    return false;
            }
            boolean allowed = false;
            TerminalPosition pos = boardBox.getCaretPosition();

            int cx = pos.getColumn();
            int cy = pos.getRow();

            int absX = cx - board.getXOffset();
            int absY = cy - MineBoard.Y_OFFSET;

            boolean arrow = false;

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
                        case 'u': {
                            byte[][] matrix = board.getMatrix();
                            int ox = board.getXOffset();
                            int oy = MineBoard.Y_OFFSET;
                            for (int x = 0; x < matrix.length; x++)
                                for (int y = 0; y < matrix[x].length; y++) {
                                    byte type = matrix[x][y];
                                    if (type == 0 || type == 11) {
                                        boardBox.setCaretPosition(y + oy, x + ox);
                                        recorder.action(Replay.ActionType.CARET, y + oy, x + ox);
                                        break;
                                    }
                                }
                            break;
                        }
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
                            text.setPreferredSize(new TerminalSize(32, 6));

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
                                    try {
                                        prefs.save();
                                    } catch (IOException e) {
                                        ErrorDialog.showErrorDialog(gui, e, sfx, "Couldn't save preferences!");
                                    }

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
                                                              Panels.vertical(radio.withBorder(Borders.singleLine())),
                                                              Panels.vertical(new Label("Width"),
                                                                              wBox,
                                                                              new Label("\nHeight"),
                                                                              hBox,
                                                                              new Label("\nBombs"),
                                                                              bBox).withBorder(Borders.singleLine()),
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
                                    text.setText(" |  Adjust game's difficulty\n" + " | \n" + " | \n" + " | \n" + " | \n" + " |");
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
                                    if (preset == ThemePreset.ONLINE) {
                                        presets.setSelectedIndex(0);
                                        Exception ex = remoteRepo.fetch();
                                        if (ex != null) {
                                            showErrorDialog(gui, ex, sfx, "Couldn't download from remote repository!");
                                            return;
                                        }

                                        Window themes = new SimpleWindow("Theme repository");

                                        Table<Object> table = new Table<>("Name", "Version", "Author");
                                        TableModel<Object> model = table.getTableModel();
                                        RemoteTheme[] ths = remoteRepo.getThemes();
                                        int width = 40;
                                        if (ths.length > 0) {
                                            int index = 0;
                                            for (RemoteTheme th : ths) {
                                                model.addRow(th.getName(), th.getVersion(), th.getAuthor());
                                                width = Math.max(width,
                                                                 th.getName().length() + th.getVersion()
                                                                                           .length() + th.getAuthor()
                                                                                                         .length() + 6);
                                                index++;
                                            }
                                        } else {
                                            model.addRow("", "<Empty>", "");
                                        }
                                        table.setPreferredSize(new TerminalSize(width, 10));
                                        table.setSelectAction(() -> {
                                            int index = table.getSelectedRow();
                                            if (index < ths.length) {
                                                RemoteTheme sel = ths[index];
                                                showThemeDetails(sel, win2, themes);
                                            }
                                        });

                                        themes.setComponent(Panels.vertical(
                                                new Label("Browse themes made by others!"),
                                                new EmptySpace(),
                                                table.withBorder(Borders.singleLine()),
                                                new EmptySpace(),
                                                new SFXButton("Close", sfx, true, themes::close)
                                        ));
                                        gui.addWindowAndWait(themes);

                                    } else if (preset != ThemePreset.NONE) {
                                        if (preset == ThemePreset.SEPARATOR) {
                                            presets.setSelectedIndex(0);
                                        } else {
                                            bfColor.setColor(preset.getBf());
                                            bbColor.setColor(preset.getBb());
                                            efColor.setColor(preset.getEf());
                                            ebColor.setColor(preset.getEb());
                                            sfColor.setColor(preset.getSf());
                                            sbColor.setColor(preset.getSb());
                                            apply.takeFocus();
                                        }
                                    }
                                });

                                Button share = new SFXButton("Share theme", sfx, false, () -> {
                                    CustomFileDialogBuilder bd = (CustomFileDialogBuilder) new CustomFileDialogBuilder(
                                            true,
                                            sfx).setActionLabel("Save")
                                                .setDescription("Choose location to save your theme")
                                                .setTitle("Exporting theme")
                                                .setSelectedFile(new File("theme.json"));
                                    bd.setForcedExtension("json");
                                    File file = bd.buildAndShow(gui);
                                    if (file != null) {
                                        try (OutputStream os = Files.newOutputStream(file.toPath())) {
                                            MessageDialogBuilder builder = new SFXMessageDialogBuilder(sfx);
                                            builder.setTitle("Exporting theme");
                                            builder.addButton(MessageDialogButton.OK);
                                            save.run();
                                            os.write(prefs.getTheme().toJSON().getBytes(StandardCharsets.UTF_8));
                                            builder.setText("Theme exported to " + file);
                                            builder.build().showDialog(gui);
                                        } catch (Exception e) {
                                            showErrorDialog(gui,
                                                            e,
                                                            sfx,
                                                            "An exception was catched",
                                                            "when trying to save your theme!");
                                        }
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
                                                showErrorDialog(gui,
                                                                e,
                                                                sfx,
                                                                "An exception was catched",
                                                                "when trying to import your theme!");
                                                return;
                                            }
                                        } else {
                                            md.setText("This file does not exist!");
                                        }
                                        md.build().showDialog(gui);
                                    }
                                });

                                win2.setComponent(Panels.grid(2,
                                                              Panels.vertical(new Label("Choose a preset"),
                                                                              presets),
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
                                                              Panels.horizontal(share, imp)
                                                                    .withBorder(Borders.singleLine()),
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
                                    text.setText(" | \n" + " | \n" + " |  Customize game's appearance\n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button leaderboards = new SFXButton("Leaderboards", sfx, this::displayLeaderboards) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " | \n" + " | \n" + " |  Show top times by difficulty\n" + " |");
                                }
                            };
                            Button done = new SFXButton("Done", sfx, true, win::close) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " | \n" + " | \n" + " | \n" + " |  Close this menu");
                                }
                            };
                            Button options = new SFXButton("Options", sfx, () -> {
                                Window win2 = new SimpleWindow("Options");
                                Preferences.Options ops = TUISweeper.this.prefs.getOptions();
                                boolean sndAvailable = sfx.isAvailable();
                                boolean guiAvailable = term
                                        instanceof SwingTerminalFrame;

                                CheckBox shaking = new SFXCheckBox("Enable screen shaking",
                                                                   ops.isScreenShaking() && guiAvailable,
                                                                   sfx);
                                CheckBox sounds = new SFXCheckBox("Enable sounds", ops.isSounds() && sndAvailable, sfx);
                                CheckBox discord = new SFXCheckBox("Discord integration",
                                                                   ops.isDiscordIntegrationEnabled(),
                                                                   sfx);
                                sounds.setEnabled(sndAvailable);
                                shaking.setEnabled(guiAvailable);

                                win2.setComponent(Panels.vertical(
                                        Panels.vertical(shaking,
                                                        sounds, discord).withBorder(Borders.singleLine())
                                        ,
                                        new EmptySpace(),
                                        Panels.horizontal(new SFXButton("Confirm",
                                                                        sfx,
                                                                        () -> {
                                                                            ops.setScreenShaking(
                                                                                    shaking.isChecked());
                                                                            ops.setSounds(
                                                                                    sounds.isChecked());
                                                                            ops.setDiscordIntegration(discord.isChecked());
                                                                            sfx.setEnabled(ops.isSounds());
                                                                            DiscordIntegr.setEnabled(discord.isChecked(),
                                                                                                     this);
                                                                            try {
                                                                                prefs.save();
                                                                            } catch (IOException e) {
                                                                                ErrorDialog.showErrorDialog(gui,
                                                                                                            e,
                                                                                                            sfx,
                                                                                                            "Couldn't save preferences!");
                                                                            }
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
                                    text.setText(" | \n" + " | \n" + " | \n" + " |  Adjust game settings\n" + " | \n" + " |");
                                }
                            };

                            Button replays = new SFXButton("Replays", sfx, () -> {
                                Window win2 = new SimpleWindow("Replays");

                                Label text2 = new Label("");
                                text2.setPreferredSize(new TerminalSize(38, 3));

                                Button local = new SFXButton("Local", sfx, () -> {
                                    win2.close();
                                    win.close();
                                    openLocalReplayBrowser();
                                }) {
                                    @Override
                                    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                        super.afterEnterFocus(direction, previouslyInFocus);
                                        text2.setText(" |  Browse replays saved locally\n |\n |");
                                    }
                                };

                                Button online = new SFXButton("Online", sfx, () -> {
                                    win2.close();
                                    win.close();
                                    openRemoteReplayBrowser();
                                }) {
                                    @Override
                                    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                        super.afterEnterFocus(direction, previouslyInFocus);
                                        text2.setText(" | \n |  Browse replays shared by others\n |");
                                    }
                                };
                                Button back = new SFXButton("Back", sfx, true, win2::close) {
                                    @Override
                                    protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                        super.afterEnterFocus(direction, previouslyInFocus);
                                        text2.setText(" | \n |\n |  Go back");
                                    }
                                };


                                win2.setComponent(Panels.grid(2, Panels.vertical(local, online, back), text2));
                                gui.addWindowAndWait(win2);
                            }) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " |  Browse and play replays \n" + " |  \n" + " | \n" + " | \n" + " |");
                                }
                            };

                            win.setComponent(Panels.grid(2,
                                                         Panels.vertical(game,
                                                                         replays,
                                                                         theme,
                                                                         options,
                                                                         leaderboards,
                                                                         done),
                                                         text));
                            this.gui.addWindowAndWait(win);
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
                        case 'h': {
                            showCtls(gui, sfx);
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
                    arrow = true;
                    break;
                }
                case ArrowRight: {
                    cx++;
                    allowed = true;
                    arrow = true;
                    break;
                }
                case ArrowUp: {
                    cy--;
                    allowed = true;
                    arrow = true;
                    break;
                }
                case ArrowDown: {
                    cy++;
                    allowed = true;
                    arrow = true;
                    break;
                }
                default:
                    break;
            }

            if (arrow) {
                recorder.action(Replay.ActionType.CARET, cx, cy);
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

    public static void showCtls(WindowBasedTextGUI gui, SFXEngine sfx) {
        Window win = new SimpleWindow("Game controls");

        win.setComponent(Panels.vertical(
                new Label("Arrow keys - Navigate on the board\n" +
                          "<Space> - Reveal a field\n" +
                          "F - Place/Remove a flag").withBorder(Borders.singleLine()),
                new EmptySpace(),
                new Label("If a revealed field has equal number of\n" +
                          "surrounding flags and bombs, you can\n" +
                          "use <Space> on it to chord"),
                new EmptySpace(),
                new SFXButton("OK", sfx, true, win::close)
        ));
        gui.addWindowAndWait(win);
    }

    private void openRemoteReplayBrowser() {
        Exception ex = remoteRepo.fetch();
        if (ex != null) {
            showErrorDialog(gui, ex, sfx, "Couldn't download from remote repository!");
            return;
        }
        Window win = new SimpleWindow("Replay browser");
        List<RemoteReplay> replays = new ArrayList<>();
        Collections.addAll(replays, remoteRepo.getReplays());


        List<String> dl = new ArrayList<>();
        dl.add("All");
        dl.addAll(Arrays.stream(Difficulty.values()).map(TUISweeper::capitalize).collect(Collectors.toList()));
        ComboBox<String> diffs = new SFXComboBox<>(sfx, dl.toArray(new String[0]));
        ComboBox<RemoteReplay.Sorting> sort = new SFXComboBox<>(sfx, RemoteReplay.Sorting.values());

        Table<Object> table = new Table<>("#", "ID", "Author", "Time", "Difficulty");
        table.setPreferredSize(new TerminalSize(50, 10));

        table.setSelectAction(() -> {
            Object selected = table.getTableModel().getRow(table.getSelectedRow()).get(0);
            if (selected instanceof Integer) {
                int index = (int) selected - 1;
                if (index < replays.size()) {
                    RemoteReplay replay = replays.get(index);
                    Window win2 = new SimpleWindow("Replay info");

                    String id = replay.getIdentifier();

                    win2.setComponent(Panels.vertical(
                            Panels.vertical(new Label("Identifier: " + (id.isEmpty() ? "<None>" : id)),
                                            new Label("Author: " + replay.getAuthor()),
                                            new Label("Play time: " + TIME_FORMAT.format(new Date(replay.getPlayTime()))))
                                  .withBorder(Borders.singleLine("Info")),
                            Panels.vertical(new Label("Difficulty: " + capitalize(replay.getDifficulty())),
                                            new Label(String.format("Size: %sx%s (%s bombs)",
                                                                    replay.getWidth(),
                                                                    replay.getHeight(),
                                                                    replay.getBombs())))
                                  .withBorder(Borders.singleLine("Difficulty")),
                            Panels.vertical(new Label("Added: " + DATE_FORMAT.format(new Date(replay.getAddedTime()))),
                                            new Label("Created: " + DATE_FORMAT.format(new Date(replay.getCreatedTime()))))
                                  .withBorder(Borders.singleLine("Times")),
                            new EmptySpace(),
                            Panels.horizontal(new SFXButton("Back", sfx, true, win2::close),
                                              new SFXButton("Play", sfx, false, () -> {
                                                  Replay rpl = replay.fetch();
                                                  if (rpl == null) {
                                                      new SFXMessageDialogBuilder(sfx).setText(
                                                                                              "Couldn't download this replay!")
                                                                                      .setTitle("Error")
                                                                                      .addButton(MessageDialogButton.OK)
                                                                                      .build().showDialog(gui);
                                                  } else if (new SFXMessageDialogBuilder(sfx)
                                                                     .addButton(MessageDialogButton.No)
                                                                     .addButton(MessageDialogButton.Yes)
                                                                     .setText(
                                                                             "Playing a replay will discard your current game.\n" +
                                                                             "Do you want to continue?")
                                                                     .setTitle("Warning")
                                                                     .build()
                                                                     .showDialog(gui) == MessageDialogButton.Yes) {
                                                      win2.close();
                                                      win.close();
                                                      startReplay(rpl);
                                                  }
                                              }),
                                              new SFXButton("Download", sfx, false, () -> {
                                                  Replay rpl = replay.fetch();
                                                  if (rpl == null) {
                                                      new SFXMessageDialogBuilder(sfx).setText(
                                                                                              "Couldn't download this replay!")
                                                                                      .setTitle("Error")
                                                                                      .addButton(MessageDialogButton.OK)
                                                                                      .build().showDialog(gui);
                                                  } else {
                                                      try {
                                                          File dir = Preferences.getReplaysDir();
                                                          dir.mkdirs();
                                                          File out = new File(dir,
                                                                              FILE_FORMAT.format(new Date()) + ".jbcfrt");
                                                          ReplayIO.write(rpl, out);

                                                          new SFXMessageDialogBuilder(sfx).setText(
                                                                                                  "Replay saved!\n" +
                                                                                                  "You can now access it in your local replays.")
                                                                                          .setTitle("Success")
                                                                                          .addButton(MessageDialogButton.OK)
                                                                                          .build().showDialog(gui);
                                                          win2.close();
                                                      } catch (Exception e) {
                                                          showErrorDialog(gui, e, sfx, "Couldn't save the replay!");
                                                      }
                                                  }
                                              }))
                    ));
                    gui.addWindowAndWait(win2);
                }
            }
        });

        diffs.addListener((i, i1, b) -> {
            table.getTableModel().clear();
            String dif = diffs.getItem(i);
            int index = 0;
            int added = 0;
            for (RemoteReplay replay : replays) {
                if ("all".equalsIgnoreCase(dif) || replay.getDifficulty().name().equalsIgnoreCase(dif)) {
                    table.getTableModel().addRow(index + 1, replay.getIdentifier(),
                                                 replay.getAuthor(),
                                                 TIME_FORMAT.format(new Date(replay.getPlayTime())),
                                                 capitalize(replay.getDifficulty()));
                    added++;
                }
                index++;
            }
            if (added == 0) {
                table.getTableModel().addRow("", "<No replays>", "", "", "");
            }
        });

        sort.addListener((i, i1, b) -> {
            RemoteReplay.Sorting s = sort.getItem(i);
            if (s != null) {
                Comparator<RemoteReplay> comparator;
                switch (s) {
                    default:
                    case DATE: {
                        comparator = (o1, o2) -> (int) (o1.getAddedTime() / 1000 - o2.getAddedTime() / 1000);
                        break;
                    }
                    case TIME: {
                        comparator = (o1, o2) -> (int) (o1.getPlayTime() - o2.getPlayTime());
                        break;
                    }
                    case CREATED: {
                        comparator = (o1, o2) -> (int) (o1.getCreatedTime() / 1000 - o2.getCreatedTime() / 1000);
                        break;
                    }
                }
                replays.sort(comparator);
            }
            diffs.setSelectedIndex(diffs.getSelectedIndex());
        });

        sort.setSelectedIndex(0);


        win.setComponent(Panels.vertical(
                Panels.grid(2, new Label("Filter by    "),
                            new Label("Sort by"),
                            diffs,
                            sort),
                new EmptySpace(),
                table.withBorder(Borders.singleLine()),
                new SFXButton("Close", sfx, true, win::close)
        ));
        table.takeFocus();
        gui.addWindowAndWait(win);
    }

    private void openLocalReplayBrowser() {
        Window win = new SimpleWindow("Replay viewer");
        List<File> replayFiles = new ArrayList<>();
        List<Replay> replays = new ArrayList<>();
        if (Preferences.getReplaysDir().isDirectory())
            try {
                File[] list = Preferences.getReplaysDir().listFiles();

                for (File f : list)
                    if (f.getName().endsWith(".jbcfrt"))
                        replayFiles.add(f);
                replayFiles.sort((o1, o2) -> {
                    int l1 = (int) (o1.lastModified() / 1000);
                    int l2 = (int) (o2.lastModified() / 1000);
                    return l2 - l1;
                });

                for (File f : replayFiles) {
                    try {
                        replays.add(ReplayIO.read(f));
                    } catch (Exception e) {
                        showErrorDialog(gui, e, sfx, "Corrupted replay:", f.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                showErrorDialog(gui, e, sfx, "Couldn't list files in replay dir");
            }

        List<String> difList = new ArrayList<>();
        difList.add("All");
        difList.addAll(Arrays.stream(Difficulty.values())
                             .map(TUISweeper::capitalize)
                             .collect(Collectors.toList()));
        ComboBox<String> difs = new SFXComboBox<>(sfx, difList.toArray(new String[0]));


        Table<Object> table = new Table<>("#", "ID", "Created", "Time", "Difficulty");
        table.setSelectAction(() -> {
            Object index = table.getTableModel().getRow(table.getSelectedRow()).get(0);
            if (index instanceof Integer) {
                int sel = (int) index - 1;
                if (sel < replays.size()) {
                    Replay repl = replays.get(sel);
                    Window win2 = new SimpleWindow("Replay");

                    win2.setComponent(Panels.vertical(
                            new Label("Identifier: " + repl.getMetadata().getIdentifier()),
                            new Label("Date created: " + DATE_FORMAT.format(new Date(repl.getMetadata()
                                                                                         .getCreatedDate()))),
                            new EmptySpace(),
                            new Label("Time: " + TIME_FORMAT.format(new Date(repl.getTime()))),
                            new Label("Difficulty: " + repl.getMetadata().getDifficulty()),
                            new Label(String.format("Size: %sx%s (%s bombs)",
                                                    repl.getWidth(),
                                                    repl.getHeight(),
                                                    repl.getBombs().size())),
                            new EmptySpace(),
                            Panels.horizontal(new SFXButton("Back", sfx, true, win2::close),
                                              new SFXButton("Play", sfx, () -> {
                                                  if (new SFXMessageDialogBuilder(sfx)
                                                              .addButton(MessageDialogButton.No)
                                                              .addButton(MessageDialogButton.Yes)
                                                              .setText(
                                                                      "Playing a replay will discard your current game.\n" +
                                                                      "Do you want to continue?")
                                                              .setTitle("Warning")
                                                              .build()
                                                              .showDialog(gui) == MessageDialogButton.Yes) {
                                                      win2.close();
                                                      win.close();
                                                      startReplay(repl);
                                                  }
                                              }),
                                              new SFXButton("Delete", sfx, () -> {
                                                  if (new SFXMessageDialogBuilder(sfx)
                                                              .setTitle("Deleting a replay")
                                                              .setText("Are you sure you want to delete\n" +
                                                                       "this replay?\n" +
                                                                       "This action is permanent")
                                                              .addButton(MessageDialogButton.No)
                                                              .addButton(MessageDialogButton.Yes)
                                                              .build()
                                                              .showDialog(gui) == MessageDialogButton.Yes) {
                                                      repl.getMetadata().getOrigin().delete();
                                                      win2.close();
                                                      replays.remove(repl);
                                                      difs.setSelectedIndex(difs.getSelectedIndex());
                                                  }
                                              }))
                    ));
                    gui.addWindowAndWait(win2);
                }
            }
        });
        table.setPreferredSize(new TerminalSize(50, 10));


        difs.addListener((i, i1, b) -> {
            table.getTableModel().clear();

            String dif = difs.getItem(i);
            int index = 0;
            Difficulty difE = dif.equalsIgnoreCase("all") ? null : Difficulty.valueOf(dif.toUpperCase());
            int addedN = 0;
            for (Replay rpl : replays) {
                if (difE == null || rpl.getMetadata().getDifficulty() == difE) {
                    table.getTableModel().addRow(index + 1,
                                                 rpl.getMetadata().getIdentifier(),
                                                 DATE_FORMAT.format(new Date(rpl.getMetadata().getCreatedDate())),
                                                 TIME_FORMAT.format(new Date(rpl.getTime())),
                                                 capitalize(rpl.getMetadata().getDifficulty())
                    );
                    addedN++;
                }
                index++;
            }
            if (addedN == 0) {
                table.getTableModel().addRow("", "<No replays>", "", "", "");
            }
        });

        difs.setSelectedIndex(0);


        win.setComponent(Panels.vertical(
                new Label("Filter by difficulty"),
                difs,
                new EmptySpace(),
                table.withBorder(Borders.singleLine()),
                new SFXButton("Back", sfx, true, win::close)
        ));
        table.takeFocus();
        gui.addWindowAndWait(win);
    }

    private void showThemeDetails(RemoteTheme theme, Window themes, Window themes2) {
        Preferences.UserTheme local = theme.fetch();
        if (local == null || !local.isValid()) {
            new SFXMessageDialogBuilder(sfx)
                    .setTitle("Error")
                    .setText("Couldn't download selected theme...")
                    .build().showDialog(gui);
            return;
        }

        Window win = new SimpleWindow("Theme details");


        Label b1 = new Label("Base color");
        Label b2 = new Label("Base background");
        Label b3 = new Label("Field color");
        Label b4 = new Label("Field background");
        Label b5 = new Label("Selected color");
        Label b6 = new Label("Selected background");

        applyBackground(local.getBaseForeground(), b1);
        applyBackground(local.getBaseBackground(), b2);
        applyBackground(local.getEditableForeground(), b3);
        applyBackground(local.getEditableBackground(), b4);
        applyBackground(local.getSelectedForeground(), b5);
        applyBackground(local.getSelectedBackground(), b6);


        win.setComponent(
                Panels.vertical(
                        new Label("Name: " + theme.getName()),
                        new Label("Author: " + theme.getAuthor()),
                        new Label("Version: " + theme.getVersion()),
                        new Label("Added: " + RemoteTheme.DATE_FORMAT.format(new Date(theme.getAddedDate()))),
                        new EmptySpace(),
                        new Label(theme.getDescription()).withBorder(Borders.singleLine()),
                        Panels.grid(2, b1, b2, b3, b4, b5, b6).withBorder(Borders.singleLine()),
                        Panels.horizontal(
                                new SFXButton("Cancel", sfx, true, win::close),
                                new SFXButton("Apply", sfx, false, () -> {
                                    win.close();
                                    updateTheme(local);
                                    prefs.getTheme().fromTheme(local);
                                    themes.close();
                                    themes2.close();
                                    new SFXMessageDialogBuilder(sfx)
                                            .setTitle("Success!")
                                            .setText("Theme applied!")
                                            .build().showDialog(gui);
                                })
                        )
                )
        );
        gui.addWindowAndWait(win);
    }

    public boolean isReplay() {
        return isReplay;
    }

    public byte getGameOver() {
        return gameOver;
    }

    private void updateTheme(Preferences.UserTheme theme) {
        gui.setTheme(theme.toTUITheme());
        try {
            prefs.save();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(gui, e, sfx, "Couldn't save preferences!");
        }
        infoLabel.setTheme(new SimpleTheme(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE_BRIGHT));
    }

    public void shake() {
        if (!prefs.getOptions().isScreenShaking()) return;
        if (term instanceof SwingTerminalFrame) {
            SwingTerminalFrame frame = (SwingTerminalFrame) term;
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

    public Difficulty getLocalDifficulty() {
        return localDifficulty;
    }

    public String getCurrentPlayingTime() {
        long diff = startTime == -1 ? 0 : endTime == -1 ? System.currentTimeMillis() - startTime : endTime - startTime;
        return new SimpleDateFormat("mm:ss").format(new Date(diff));
    }

    public int getWidth() {
        return board.getSizeX();
    }

    public int getHeight() {
        return board.getSizeY();
    }

    public int getBombs() {
        return board.getBombs();
    }

    public void flag(int x, int y) {
        if (gameOver > 0)
            return;
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
            recorder.start();
            recorder.action(Replay.ActionType.FLAG, x, y);
            board.setFieldAt(x, y, c);
        }
    }

    public int reveal(int x, int y) {
        return reveal(x, y, false, true);
    }

    public int reveal(int x, int y, boolean revealFlags, boolean user) {
        if (gameOver > 0)
            return 0;
        startTimer();
        recorder.start();
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
        if (current == 0 || revealFlags && current == 12) {
            int bombs = board.countBombs(x, y);
            if (bombs == 0) {
                board.setFieldAt(x, y, 10);
                for (int i = x - 1; i <= x + 1; i++)
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i >= 0 && j >= 0 && i < board.getSizeX() && j < board.getSizeY()) {
                            byte c = board.getFieldAt(i, j);
                            if (c == 0 || c == 12) count += reveal(i, j, true, false);
                        }
                    }
            } else {
                board.setFieldAt(x, y, bombs);
            }
            if (user)
                recorder.action(Replay.ActionType.REVEAL, x, y);
            count++;
        } else if (current > 0 && current < 10) {
            int flags = board.countFlags(x, y);
            if (flags == current) {
                for (int i = x - 1; i <= x + 1; i++)
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i >= 0 && j >= 0 && i < board.getSizeX() && j < board.getSizeY()) {
                            byte c = board.getFieldAt(i, j);
                            if (c == 0 || c == 11) count += reveal(i, j, true, false);
                        }
                    }
            }
            if (user)
                recorder.action(Replay.ActionType.REVEAL, x, y);
        } else if (current == 11) {
            board.revealMines();
            gameOver = 1;
            recorder.stop();
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
                                         table.withBorder(Borders.singleLine()),
                                         new EmptySpace(),
                                         new SFXButton("Close", sfx, true, win::close)));
        gui.addWindow(win);
    }

    public void start() {
        try {
            currentReplay = null;
            isReplay = false;
            player.stop();
            recorder.setEnabled(true);
            board.initialize(prefs.getWidth(), prefs.getHeight(), prefs.getBombs());
            resetVariables();
            updateBoard();
            boardBox.setCaretPosition(MineBoard.Y_OFFSET, board.getXOffset());
            DiscordIntegr.updateStartDate();
        } catch (Exception e) {
            showErrorDialog(gui, e, sfx, "Error initializing the field!");
        }
    }

    public void startReplay(Replay replay) {
        try {
            isReplay = true;
            player.stop();
            recorder.setEnabled(false);
            board.initializeReplay(replay.getWidth(), replay.getHeight(), replay.getBombs().size(), replay.getSeed());
            for (Replay.CoordPair bomb : replay.getBombs())
                board.setFieldAt(bomb.getX(), bomb.getY(), 11);
            resetVariables();
            updateBoard();
            boardBox.setCaretPosition(0, 0);
            player.play(replay);
            currentReplay = replay;
            DiscordIntegr.updateStartDate();
        } catch (Exception e) {
            showErrorDialog(gui, e, sfx, "Error initializing replay field!", "The replay might be corrupted.");
        }
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

    public String getPercentDone() {
        if (gameOver > 0) {
            return gameOver == 2 ? "100" : "0";
        } else {
            int[] fields = board.countAllFields(11, 12, 0, 13);
            double percent = (double) (fields[2] + fields[0]) / (board.getSizeX() * board.getSizeY());
            percent = 1 - percent;
            return doubleFormat.format(percent * 100);
        }
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
                Replay replay = recorder.getReplay();
                recorder.stop();
                sfx.play("win");
                endTime = System.currentTimeMillis();
                Difficulty diff = prefs.getDifficulty();
                if (diff != Difficulty.CUSTOM && !isReplay) {
                    leaders.addEntry(diff, endTime - startTime);
                }
                Window win = new SimpleWindow(isReplay ? "Replay ended" : "Game won");
                Panel ctl = Panels.horizontal(new Button("Close", win::close));
                if (diff != Difficulty.CUSTOM && !isReplay)
                    ctl.addComponent(new SFXButton("Leaderboards", sfx, this::displayLeaderboards));

                if (!isReplay) {
                    ctl.addComponent(new SFXButton("Save replay", sfx, () -> {
                        String name = new SFXTextInputDialogBuilder(sfx)
                                .setValidator(s -> s.length() <= 10 ? null : "The name must be less than 10 characters long!")
                                .setTextBoxSize(new TerminalSize(10, 1))
                                .setTitle("Enter replay name")
                                .setDescription("Enter replay name in the field below.\n" +
                                                "You can leave the field empty.")
                                .build().showDialog(gui);
                        if (name != null) {
                            replay.getMetadata().setIdentifier(name);
                            File dir = Preferences.getReplaysDir();
                            dir.mkdirs();
                            String rpn = FILE_FORMAT.format(new Date()) + ".jbcfrt";
                            try {
                                ReplayIO.write(replay, new File(dir, rpn));
                                new SFXMessageDialogBuilder(sfx).addButton(MessageDialogButton.OK)
                                                                .setText("Replay saved!")
                                                                .setTitle("Success")
                                                                .build()
                                                                .showDialog(gui);
                            } catch (Exception e) {
                                showErrorDialog(gui, e, sfx, "An error occured while saving", "the replay!");
                            }
                        }
                    }));
                }

                win.setComponent(Panels.vertical(new Label(isReplay ? "Replay ended" : "You won!\nYour time is " + getCurrentPlayingTime()),
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

        if (isReplay && gameOver == 0) {
            builder.append("\n")
                   .append("\n")
                   .append("    ")
                   .append("\n")
                   .append("    n - End replay");
        } else {
            builder.append("\n    ")
                   .append(gameOver == 0 ? "" : gameOver == 2 ? "You won!" : "GAME OVER")
                   .append("\n")
                   .append("    ")
                   .append("\n")
                   .append("    n - New Game     g - Go to field...\n")
                   .append("    m - Game Menu    u - Find next empty field\n")
                   .append("    q - Quit         h - Show controls");
        }

        boardBox.setText(builder.toString());

        StringBuilder labelText = new StringBuilder();

        String emote = gameOver == 0 ? ":)" : gameOver == 1 ? "X(" : "B)";
        labelText.append("  ")
                 .append(emote)
                 .append("   TUI-Sweeper    Difficulty: ")
                 .append(currentReplay != null && isReplay ? currentReplay.getMetadata()
                                                                          .getDifficulty() : localDifficulty);
        int wh = screen.getTerminalSize().getColumns();
        for (int x = labelText.length(); x < wh; x++)
            labelText.append(" ");
        infoLabel.setText(labelText.toString());
        DiscordIntegr.update(this);
    }
}
