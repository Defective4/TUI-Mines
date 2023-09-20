package io.github.defective4.javajam.tuisweeper.core;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import io.github.defective4.javajam.tuisweeper.core.storage.Preferences;
import io.github.defective4.javajam.tuisweeper.core.ui.SimpleWindow;

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
    private long startTime = -1;
    private long endTime = -1;
    private boolean placed = false;

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
                    if (absY >= 0 && absX >= 0 && absX < board.getSizeX() && absY < board.getSizeY())
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
                        case 'c': {
                            Window win = new SimpleWindow("Customization");
                            Panel panel = new Panel(new GridLayout(2));
                            Panel ctl = new Panel(new LinearLayout());
                            Label text = new Label("");

                            Button game = new Button("Game") {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    text.setText(" |  Adjust game's difficulty   \n" + " | \n" + " |");
                                }
                            };
                            Button theme = new Button("Theme") {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    text.setText(" | \n" + " |  Customize game's appearance\n" + " |");
                                }
                            };
                            Button done = new Button("Done", win::close) {
                                @Override
                                protected void afterEnterFocus(FocusChangeDirection direction, Interactable previouslyInFocus) {
                                    text.setText(" | \n" + " | \n" + " |  Close this menu            ");
                                }
                            };

                            ctl.addComponent(game);
                            ctl.addComponent(theme);
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
            // TODO
        }
    }

    public void show() {
        gui.addWindowAndWait(mainWindow);
    }

    public void start() {
        board.initialize(10, 10, 10);
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

                int[] fields = board.countAllFields(11, 12, 0);
                String bombs = Integer.toString(fields[0] - fields[1]);
                double percent = (double) (fields[2] + fields[0]) / (board.getSizeX() * board.getSizeY());

                StringBuilder space = new StringBuilder();
                for (int x = 0; x < 8 - bombs.length(); x++)
                    space.append(" ");

                builder.append("    ")
                       .append(time)
                       .append("   ")
                       .append(bombs)
                       .append(space)
                       .append(doubleFormat.format(100 - (percent * 100)))
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

        builder.append("\n\n\n")
               .append("    n - New Game\n")
               .append("    c - Customize\n")
               .append("    l - Leaderboards\n")
               .append("    q - Quit");

        boardBox.setText(builder.toString());
    }
}
