package io.github.defective4.javajam.tuisweeper.core;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import io.github.defective4.javajam.tuisweeper.core.storage.Leaderboards;
import io.github.defective4.javajam.tuisweeper.core.storage.Preferences;
import io.github.defective4.javajam.tuisweeper.core.ui.ColorChooserButton;
import io.github.defective4.javajam.tuisweeper.core.ui.NumberBox;
import io.github.defective4.javajam.tuisweeper.core.ui.SimpleWindow;
import io.github.defective4.javajam.tuisweeper.core.ui.ThemePreset;

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
    private final Window mainWindow = new SimpleWindow(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS);
    private final TextBox boardBox = new TextBox();
    private final MineBoard board = new MineBoard();

    private final Timer boardUpdater = new Timer(true);
    private final Preferences prefs = new Preferences();
    private final Leaderboards leaders = new Leaderboards();
    private long startTime = -1;
    private long endTime = -1;
    private boolean placed = false;

    private byte gameOver = 0;

    public TUISweeper(Screen screen) {
        this.screen = screen;
        gui = new MultiWindowTextGUI(screen);
        gui.setTheme(prefs.getTheme().toTUITheme());
        mainWindow.setComponent(boardBox);

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
                                reveal(absX, absY);
                                updateBoard();
                                break;
                            }
                            default:
                                break;
                        }

                    switch (keyStroke.getCharacter()) {
                        case 'n': {
                            Window win = new SimpleWindow("New game");
                            Panel panel = new Panel(new LinearLayout());
                            Panel ctl = new Panel(new LinearLayout(Direction.HORIZONTAL));

                            panel.addComponent(new Label("Are you sure you want to start a new game?\n" + "Your current session will get discarded.\n "));

                            ctl.addComponent(new Button("No", win::close));
                            ctl.addComponent(new Button("Yes", () -> {
                                win.close();
                                start();
                            }));

                            panel.addComponent(ctl);

                            win.setComponent(panel);
                            gui.addWindow(win);
                            break;
                        }
                        case 'm': {
                            Window win = new SimpleWindow("Game Menu");
                            Panel panel = new Panel(new GridLayout(2));
                            Panel ctl = new Panel(new LinearLayout());
                            Label text = new Label("");
                            text.setPreferredSize(new TerminalSize(32, 4));

                            Button game = new Button("Game", () -> {
                                Panel panel2 = new Panel(new GridLayout(2));
                                Panel left = new Panel(new LinearLayout());
                                Panel right = new Panel(new LinearLayout());
                                Panel ctl2 = new Panel(new LinearLayout(Direction.HORIZONTAL));

                                Difficulty[] diffs = Difficulty.values();
                                RadioBoxList<Difficulty> radio = new RadioBoxList<>();
                                for (Difficulty dif : diffs)
                                    radio.addItem(dif);

                                NumberBox wBox = new NumberBox(board.getSizeX());
                                NumberBox hBox = new NumberBox(board.getSizeY());
                                NumberBox bBox = new NumberBox(board.getBombs());

                                TextBox.TextChangeListener listener = (s, b) -> {
                                    if (b && radio.getCheckedItem() != Difficulty.CUSTOM)
                                        radio.setCheckedItem(Difficulty.CUSTOM);
                                };

                                wBox.setTextChangeListener(listener);
                                hBox.setTextChangeListener(listener);
                                bBox.setTextChangeListener(listener);

                                radio.addListener((i, i1) -> {
                                    Difficulty sel = radio.getItemAt(i);
                                    if (sel != Difficulty.CUSTOM) {
                                        wBox.setValue(sel.getWidth());
                                        hBox.setValue(sel.getHeight());
                                        bBox.setValue(sel.getBombs());
                                    } else if (radio.isFocused()) wBox.takeFocus();
                                });
                                radio.setCheckedItem(prefs.getDifficulty());

                                left.addComponent(radio);
                                right.addComponent(new Label("Width"));
                                right.addComponent(wBox);
                                right.addComponent(new Label("\nHeight"));
                                right.addComponent(hBox);
                                right.addComponent(new Label("\nBombs"));
                                right.addComponent(bBox);
                                ctl2.addComponent(new Button("Cancel", () -> win.setComponent(panel)));
                                ctl2.addComponent(new Button("Confirm", () -> {
                                    prefs.setDifficulty(radio.getCheckedItem());
                                    prefs.setWidth(wBox.getValue());
                                    prefs.setHeight(hBox.getValue());
                                    prefs.setBombs(bBox.getValue());
                                    Panel panel3 = new Panel(new LinearLayout());
                                    Panel ctl3 = new Panel(new LinearLayout(Direction.HORIZONTAL));

                                    ctl3.addComponent(new Button("No", win::close));
                                    ctl3.addComponent(new Button("Yes", () -> {
                                        start();
                                        win.close();
                                    }));
                                    panel3.addComponent(new Label(
                                            "The changes will take effect after starting a new game.\n" + "Do you want to start a new gamew now?\n "));
                                    panel3.addComponent(ctl3);
                                    win.setComponent(panel3);
                                }));
                                panel2.addComponent(left);
                                panel2.addComponent(right);
                                panel2.addComponent(ctl2);
                                win.setComponent(panel2);
                            }) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" |  Adjust game's difficulty\n" + " | \n" + " | \n" + " |");
                                }
                            };
                            Button theme = new Button("Theme", () -> {
                                Panel panel2 = new Panel(new GridLayout(2));

                                Preferences.UserTheme ut = prefs.getTheme();
                                ColorChooserButton bfColor = new ColorChooserButton(gui, ut.getBaseForeground());
                                ColorChooserButton bbColor = new ColorChooserButton(gui, ut.getBaseBackground());
                                ColorChooserButton efColor = new ColorChooserButton(gui, ut.getEditableForeground());
                                ColorChooserButton ebColor = new ColorChooserButton(gui, ut.getEditableBackground());
                                ColorChooserButton sfColor = new ColorChooserButton(gui, ut.getSelectedForeground());
                                ColorChooserButton sbColor = new ColorChooserButton(gui, ut.getSelectedBackground());


                                ComboBox<ThemePreset> presets = new ComboBox<ThemePreset>(ThemePreset.values()) {
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
                                    }
                                });

                                panel2.addComponent(new Label("Choose a preset"));
                                panel2.addComponent(new EmptySpace());
                                panel2.addComponent(presets);
                                panel2.addComponent(new EmptySpace());
                                panel2.addComponent(new Label("\nBase color"));
                                panel2.addComponent(new Label("\nBase background"));
                                panel2.addComponent(bfColor);
                                panel2.addComponent(bbColor);
                                panel2.addComponent(new Label("\nField color"));
                                panel2.addComponent(new Label("\nField background"));
                                panel2.addComponent(efColor);
                                panel2.addComponent(ebColor);
                                panel2.addComponent(new Label("\nSelected color    "));
                                panel2.addComponent(new Label("\nSelected background"));
                                panel2.addComponent(sfColor);
                                panel2.addComponent(sbColor);
                                panel2.addComponent(new Label("\n "));
                                panel2.addComponent(new Label("\n "));
                                panel2.addComponent(new Button("Cancel", () -> win.setComponent(panel)));
                                panel2.addComponent(new Button("Apply", () -> {
                                    ut.setBaseBackground(bbColor.getColor());
                                    ut.setBaseForeground(bfColor.getColor());
                                    ut.setSelectedBackground(sbColor.getColor());
                                    ut.setSelectedForeground(sfColor.getColor());
                                    ut.setEditableBackground(ebColor.getColor());
                                    ut.setEditableForeground(efColor.getColor());
                                    gui.setTheme(ut.toTUITheme());
                                    win.setComponent(panel);
                                }));


                                win.setComponent(panel2);
                            }) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " |  Customize game's appearance\n" + " | \n" + " |");
                                }
                            };
                            Button leaderboards = new Button("Leaderboards", this::displayLeaderboards) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " |  Show top times by difficulty\n" + " |");
                                }
                            };

                            Button done = new Button("Done", win::close) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    super.afterEnterFocus(direction, previouslyInFocus);
                                    text.setText(" | \n" + " | \n" + " | \n" + " |  Close this menu");
                                }
                            };

                            ctl.addComponent(game);
                            ctl.addComponent(theme);
                            ctl.addComponent(leaderboards);
                            ctl.addComponent(done);

                            panel.addComponent(ctl);
                            panel.addComponent(text);
                            win.setComponent(panel);
                            gui.addWindow(win);
                            break;
                        }
                        case 'q': {
                            Window win = new SimpleWindow("Quiting the game");
                            Panel panel = new Panel(new LinearLayout());
                            Panel ctl = new Panel(new LinearLayout(Direction.HORIZONTAL));

                            Button no = new Button("No", win::close);
                            Button yes = new Button("Yes", () -> System.exit(0));

                            ctl.addComponent(no);
                            ctl.addComponent(yes);

                            panel.addComponent(new Label("Are you sure you want to discard\n" + "current game and close the application?\n "));
                            panel.addComponent(ctl);

                            win.setComponent(panel);
                            gui.addWindow(win);
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

    public void reveal(int x, int y) {
        reveal(x, y, false);
    }

    public void reveal(int x, int y, boolean revealFlags) {
        startTimer();
        byte current = board.getFieldAt(x, y);
        if (!placed) {
            placed = true;
            if (current == 11) {
                int i, j;
                byte c;
                Random rand = board.getRand();
                do {
                    i = rand.nextInt(board.getSizeX());
                    j = rand.nextInt(board.getSizeY());
                    c = board.getFieldAt(i, j);
                } while (c != 0);
                board.setFieldAt(i, j, 11);
                board.setFieldAt(x, y, 0);
                current = 0;
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
                            if (c == 0 || c == 12) reveal(i, j, true);
                        }
                    }
            } else {
                board.setFieldAt(x, y, bombs);
            }
        } else if (current > 0 && current < 10) {
            int flags = board.countFlags(x, y);
            if (flags == current) {
                for (int i = x - 1; i <= x + 1; i++)
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i >= 0 && j >= 0 && i < board.getSizeX() && j < board.getSizeY()) {
                            byte c = board.getFieldAt(i, j);
                            if (c == 0 || c == 11) reveal(i, j, true);
                        }
                    }
            }
        } else if (current == 11) {
            board.revealMines();
            gameOver = 1;
            endTime = System.currentTimeMillis();
        }
    }

    public void show() {
        gui.addWindowAndWait(mainWindow);
    }

    public void displayLeaderboards() {
        Window win = new SimpleWindow("Leaderboards");
        Panel panel = new Panel(new LinearLayout());

        Table<String> table = new Table<>("#", "Time", "Date (yy-mm-dd hh:mm)");
        table.setPreferredSize(new TerminalSize(35, 11));

        ComboBox<Difficulty> diff = new ComboBox<>(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
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

        panel.addComponent(new Label("Difficulty"));
        panel.addComponent(diff);
        panel.addComponent(new EmptySpace());
        panel.addComponent(table);
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Button("Close", win::close));
        win.setComponent(panel);
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
    }

    private void updateBoard() {
        TerminalPosition caret = boardBox.getCaretPosition();
        updateBoard(caret.getColumn(), caret.getRow());
    }

    private void updateBoard(int cx, int cy) {
        StringBuilder builder = new StringBuilder();

        int numberLen = board.getMaxSizeLen();
        int offsetX = MineBoard.X_OFFSET + numberLen;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < board.getSizeX() + offsetX; j++) {
                builder.append(j < offsetX ?
                                       ' ' :
                                       i == 0 ?
                                               j == cx ? 'v' : ' ' :
                                               i == 1 ? CHARS[(j - offsetX) % CHARS.length] : '=');
            }
            if (i == 1) builder.append("    Timer   Bombs   Cleared");
            else if (i == 2) {
                String time = getCurrentPlayingTime();

                String bombs;
                double percent;
                if (gameOver > 0) {
                    bombs = "0";
                    percent = gameOver == 2 ? 100 : 0;
                } else {
                    int[] fields = board.countAllFields(11, 12, 0, 13);
                    if (fields[0] == 0 && fields[1] == 0 && fields[2] == 0) {
                        gameOver = 2;
                        endTime = System.currentTimeMillis();
                        Difficulty diff = prefs.getDifficulty();
                        if (diff != Difficulty.CUSTOM) {
                            leaders.addEntry(diff, endTime - startTime);
                        }
                        Window win = new SimpleWindow("Game won");
                        Panel panel = new Panel(new LinearLayout());
                        Panel ctl = new Panel(new LinearLayout(Direction.HORIZONTAL));

                        ctl.addComponent(new Button("Close", win::close));
                        if (diff != Difficulty.CUSTOM)
                            ctl.addComponent(new Button("Leaderboards", this::displayLeaderboards));

                        panel.addComponent(new Label("You won!\n" + "Your time is " + getCurrentPlayingTime() + "\n "));
                        panel.addComponent(ctl);
                        win.setComponent(panel);
                        gui.addWindow(win);
                    }
                    percent = (double) (fields[2] + fields[0]) / (board.getSizeX() * board.getSizeY());
                    percent = 100 - (percent * 100);
                    bombs = Integer.toString(fields[0] - fields[1]);
                }

                StringBuilder space = new StringBuilder();
                for (int x = 0; x < 8 - bombs.length(); x++)
                    space.append(" ");

                builder.append("    ")
                       .append(time)
                       .append("   ")
                       .append(bombs)
                       .append(space)
                       .append(doubleFormat.format(percent))
                       .append('%');
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
               .append("\n\n")
               .append("    n - New Game\n")
               .append("    m - Game Menu\n")
               .append("    q - Quit");

        boardBox.setText(builder.toString());
    }
}
