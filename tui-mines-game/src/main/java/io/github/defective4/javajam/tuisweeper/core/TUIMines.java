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
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import io.github.defective4.javajam.tuisweeper.Main;
import io.github.defective4.javajam.tuisweeper.components.Difficulty;
import io.github.defective4.javajam.tuisweeper.components.Strings;
import io.github.defective4.javajam.tuisweeper.components.sfx.*;
import io.github.defective4.javajam.tuisweeper.components.ui.ColorChooserButton;
import io.github.defective4.javajam.tuisweeper.components.ui.NumberBox;
import io.github.defective4.javajam.tuisweeper.components.ui.SimpleWindow;
import io.github.defective4.javajam.tuisweeper.core.network.RemoteTheme;
import io.github.defective4.javajam.tuisweeper.core.network.Repository;
import io.github.defective4.javajam.tuisweeper.core.storage.Preferences;
import io.github.defective4.javajam.tuisweeper.core.ui.ThemePreset;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import static io.github.defective4.javajam.tuisweeper.components.ui.Dialogs.showDownloadingWindow;
import static io.github.defective4.javajam.tuisweeper.components.ui.Dialogs.showErrorDialog;
import static io.github.defective4.javajam.tuisweeper.components.ui.util.ColorConverter.applyBackground;

/**
 * The main game class.
 * Most of the game and application logic happens here.
 *
 * @author Defective
 */
public class TUIMines {

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
    private final Repository remoteRepo = new Repository();
    private final Label infoLabel;
    private long startTime = -1;
    private long endTime = -1;
    private boolean placed;
    private byte gameOver;
    private Difficulty localDifficulty = Difficulty.EASY;


    private final Runnable notAvailable;

    public TUIMines(Screen screen, WindowBasedTextGUI gui, Terminal term, SFXEngine sfx, Preferences prefs) {
        this.screen = screen;
        this.gui = gui;
        this.term = term;
        this.sfx = sfx;
        this.prefs = prefs;
        this.infoLabel = new Label("");
        updateTheme(this.prefs.getTheme());

        notAvailable = () -> new MessageDialogBuilder()
                .setTitle("Error")
                .setText("This feature is not available in\n" +
                         "this version of TUI-Mines\n" +
                         "\n " +
                         "Please download the full version at\n" +
                         "https://github.com/Defective4/TUI-Mines")
                .addButton(MessageDialogButton.OK)
                .build().showDialog(gui);

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
                                byte current = board.getFieldAt(absX, absY);
                                if (prefs.getOptions().isFlagOnly() && placed && (current <= 0 || current >= 10)) {
                                    flag(absX, absY);
                                } else {
                                    int cnt = reveal(absX, absY);
                                    updateBoard();
                                }
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
                            text.setPreferredSize(new TerminalSize(32, 7));

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
                                    TUIMines.this.prefs.setDifficulty(radio.getCheckedItem());
                                    TUIMines.this.prefs.setWidth(wBox.getValue());
                                    TUIMines.this.prefs.setHeight(hBox.getValue());
                                    TUIMines.this.prefs.setBombs(bBox.getValue());

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
                                radio.setCheckedItem(TUIMines.this.prefs.getDifficulty());
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
                                    text.setText(" |  Adjust game's difficulty\n" + " | \n" + " | \n" + " | \n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button theme = new SFXButton("Theme", sfx, () -> {
                                Window win2 = new SimpleWindow("Theming");

                                Preferences.UserTheme ut = TUIMines.this.prefs.getTheme();
                                ColorChooserButton bfColor = new ColorChooserButton(TUIMines.this.gui,
                                                                                    ut.getBaseForeground(),
                                                                                    sfx);
                                ColorChooserButton bbColor = new ColorChooserButton(TUIMines.this.gui,
                                                                                    ut.getBaseBackground(),
                                                                                    sfx);
                                ColorChooserButton efColor = new ColorChooserButton(TUIMines.this.gui,
                                                                                    ut.getEditableForeground(),
                                                                                    sfx);
                                ColorChooserButton ebColor = new ColorChooserButton(TUIMines.this.gui,
                                                                                    ut.getEditableBackground(),
                                                                                    sfx);
                                ColorChooserButton sfColor = new ColorChooserButton(TUIMines.this.gui,
                                                                                    ut.getSelectedForeground(),
                                                                                    sfx);
                                ColorChooserButton sbColor = new ColorChooserButton(TUIMines.this.gui,
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
                                        win2.close();
                                        win.close();
                                        Window downloading = showDownloadingWindow(gui);
                                        Future<?> future = remoteRepo.fetch(ex -> {
                                            downloading.close();
                                            if (ex != null) {
                                                showErrorDialog(gui,
                                                                ex,
                                                                sfx,
                                                                "Couldn't download from remote repository!");
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
                                        });

                                        Button cancel = new SFXButton("Cancel",
                                                                      sfx,
                                                                      true,
                                                                      () -> {
                                                                          downloading.close();
                                                                          remoteRepo.cancel(
                                                                                  future);
                                                                      });
                                        ((Panel) downloading.getComponent()).addComponent(cancel);
                                        cancel.takeFocus();

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

                                Button share = new SFXButton("Share theme", sfx, false, notAvailable);

                                Button imp = new SFXButton("Import theme", sfx, false, notAvailable);


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
                                    text.setText(" | \n" + " | \n" + " |  Customize game's appearance\n" + " | \n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button leaderboards = new SFXButton("Leaderboards", sfx, notAvailable) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " | \n" + " | \n" + " |  Not available\n" + " | \n" + " |");
                                }
                            };
                            Button done = new SFXButton("Done", sfx, true, win::close) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " | \n" + " | \n" + " | \n" + " | \n" + " |  Close this menu");
                                }
                            };
                            Button options = new SFXButton("Options", sfx, () -> {
                                Window win2 = new SimpleWindow("Options");
                                Preferences.Options ops = TUIMines.this.prefs.getOptions();
                                boolean sndAvailable = sfx.isAvailable();
                                boolean guiAvailable = term
                                        instanceof SwingTerminalFrame;

                                CheckBox shaking = new SFXCheckBox("Enable screen shaking",
                                                                   false,
                                                                   sfx);
                                CheckBox sounds = new SFXCheckBox("Enable sounds",
                                                                  false,
                                                                  sfx);
                                CheckBox discord = new SFXCheckBox("Discord integration",
                                                                   false,
                                                                   sfx);
                                CheckBox updates = new SFXCheckBox("Update checker", false, sfx);

                                ComboBox<String> playStyle = new SFXComboBox<>(sfx, "Classic", "Flag only");
                                playStyle.addListener((i, i1, b) -> {
                                    if (i == 1 && !prefs.getOneTimeDialogs().seenPlayStyleDialog()) {
                                        new SFXMessageDialogBuilder(sfx)
                                                .setTitle("Play style notice")
                                                .setText("Welcome to Flag only mode!\n" +
                                                         "While Flag only play style is active\n" +
                                                         "<space> key action is replaced with Flag,\n" +
                                                         "meaning you have to use chording to work your way\n" +
                                                         "through the board!\n" +
                                                         "This option is recommended for players who use chording\n" +
                                                         "as their main technique, as it can be quicker and it helps avoid\n" +
                                                         "mistakes!\n" +
                                                         "\n" +
                                                         "Also make sure to check game controls help after this change!")
                                                .addButton(MessageDialogButton.OK)
                                                .build().showDialog(gui);
                                    }
                                });
                                playStyle.setSelectedIndex(ops.isFlagOnly() ? 1 : 0);

                                sounds.setEnabled(false);
                                shaking.setEnabled(false);
                                discord.setEnabled(false);
                                updates.setEnabled(false);

                                win2.setComponent(Panels.vertical(
                                        Panels.vertical(shaking,
                                                        sounds,
                                                        discord,
                                                        updates,
                                                        new EmptySpace(),
                                                        new Label("Play style"),
                                                        playStyle)
                                              .withBorder(Borders.singleLine())
                                        ,
                                        new EmptySpace(),
                                        Panels.horizontal(new SFXButton("Confirm",
                                                                        sfx,
                                                                        () -> {
                                                                            if (shaking.isEnabled())
                                                                                ops.setScreenShaking(
                                                                                        shaking.isChecked());
                                                                            if (sounds.isEnabled())
                                                                                ops.setSounds(
                                                                                        sounds.isChecked());
                                                                            if (discord.isEnabled())
                                                                                ops.setDiscordIntegration(discord.isChecked());
                                                                            ops.setUpdatesEnabled(updates.isChecked());
                                                                            ops.setFlagOnly(playStyle.getSelectedIndex() == 1);
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
                                    text.setText(" | \n" + " | \n" + " | \n" + " |  Adjust game settings\n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button replays = new SFXButton("Replays", sfx, notAvailable) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " |  Not available \n" + " |  \n" + " | \n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button about = new SFXButton("About", sfx, () -> {
                                Window abt = new SimpleWindow("");

                                abt.setComponent(Panels.vertical(
                                        new Label("TUI - Mines\n" +
                                                  "Terminal-based Minesweeper clone"),
                                        new EmptySpace(),
                                        new Label("Created for Java Jam 2023 hosted at\n" +
                                                  "Java Community Discord"),
                                        new EmptySpace(),
                                        new Label("Author: Defective\n" +
                                                  "Version: " + Main.VERSION + "\n" +
                                                  "Github: https://github.com/Defective4/TUI-Mines"),
                                        new EmptySpace(),
                                        Panels.horizontal(
                                                new SFXButton("Back", sfx, true, abt::close),
                                                new SFXButton("GitHub",
                                                              sfx,
                                                              () -> Main.openLink(
                                                                      "https://github.com/Defective4/TUI-Mines",
                                                                      sfx,
                                                                      gui))

                                        )
                                ));
                                gui.addWindowAndWait(abt);
                            }) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " |  \n" + " |  \n" + " | \n" + " | \n" + " |  Information about the game\n" + " |");
                                }
                            };

                            Label discord = new Label("Discord integration disabled"
                            );

                            win.setComponent(Panels.vertical(discord, new EmptySpace(), Panels.grid(2,
                                                                                                    Panels.vertical(game,
                                                                                                                    replays,
                                                                                                                    theme,
                                                                                                                    options,
                                                                                                                    leaderboards,
                                                                                                                    about,
                                                                                                                    done),
                                                                                                    text)));
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
                            showCtls(gui, sfx, prefs);
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
        return Strings.capitalize(en);
    }

    public static void showCtls(WindowBasedTextGUI gui, SFXEngine sfx, Preferences prefs) {
        Window win = new SimpleWindow("Game controls");

        boolean onlyFlags = prefs.getOptions().isFlagOnly();

        win.setComponent(Panels.vertical(
                new Label("Controls for " + (onlyFlags ? "Flag only" : "Classic") + " mode"),
                new EmptySpace(),
                new Label(onlyFlags ? "Arrow keys - Navigate on the board\n" +
                                      "<Space> - Flag/Chord" : "Arrow keys - Navigate on the board\n" +
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

    public byte getGameOver() {
        return gameOver;
    }

    private void updateTheme(Preferences.UserTheme theme) {
        gui.setTheme(theme.toTUITheme());
        infoLabel.setTheme(new SimpleTheme(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE_BRIGHT));
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
                c = 12;
                break;
            }
            case 11: {
                if (fields[0] - fields[1] <= 0) break;
                c = 13;
                break;
            }
            case 12: {
                c = 0;
                break;
            }
            case 13: {
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
        return reveal(x, y, false, true);
    }

    public int reveal(int x, int y, boolean revealFlags, boolean user) {
        if (gameOver > 0)
            return 0;
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
        } else if (current == 11) {
            board.revealMines();
            gameOver = 1;
            endTime = System.currentTimeMillis();
        }
        return count;
    }

    public void show() {
        gui.addWindow(mainWindow);
    }

    public void start() {
        try {
            board.initialize(prefs.getWidth(), prefs.getHeight(), prefs.getBombs());
            resetVariables();
            updateBoard();
            boardBox.setCaretPosition(MineBoard.Y_OFFSET, board.getXOffset());
        } catch (Exception e) {
            showErrorDialog(gui, e, sfx, "Error initializing the field!");
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
                endTime = System.currentTimeMillis();
                Difficulty diff = prefs.getDifficulty();
                Window win = new SimpleWindow("Game won");
                Panel ctl = Panels.horizontal();

                ctl.addComponent(new SFXButton("Close", sfx, true, win::close));

                win.setComponent(Panels.vertical(new Label("You won!\nYour time is " + getCurrentPlayingTime()),
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
               .append("    m - Game Menu    u - Find next empty field\n");

        boardBox.setText(builder.toString());

        StringBuilder labelText = new StringBuilder();

        String emote = gameOver == 0 ? ":)" : gameOver == 1 ? "X(" : "B)";
        labelText.append("  ")
                 .append(emote)
                 .append("   TUI-Mines    Difficulty: ")
                 .append(localDifficulty);
        int wh = screen.getTerminalSize().getColumns();
        for (int x = labelText.length(); x < wh; x++)
            labelText.append(" ");
        infoLabel.setText(labelText.toString());
    }
}
