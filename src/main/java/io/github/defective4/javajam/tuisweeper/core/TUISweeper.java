package io.github.defective4.javajam.tuisweeper.core;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import io.github.defective4.javajam.tuisweeper.core.ui.SimpleWindow;

public class TUISweeper {

    private static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final Screen screen;
    private final WindowBasedTextGUI gui;
    private final Window mainWindow = new SimpleWindow(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS);
    private final TextBox boardBox = new TextBox();
    private final MineBoard board = new MineBoard();

    public TUISweeper(Screen screen) {
        this.screen = screen;
        gui = new MultiWindowTextGUI(screen);
        mainWindow.setComponent(boardBox);

        boardBox.setInputFilter((interactable, keyStroke) -> {
            boolean allowed = false;
            TerminalPosition pos = boardBox.getCaretPosition();

            int cx = pos.getColumn();
            int cy = pos.getRow();

            int absX = cx - (2 + Integer.toString(board.getSizeY() + 1).length());
            int absY = cy - 3;

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
    }

    public void flag(int x, int y) {
        byte current = board.getFieldAt(x, y);
        byte c;
        switch (current) {
            case 0: {
                c = 12;
                break;
            }
            case 11: {
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
                c = -1;
                break;
            }
        }
        if (c > -1) board.setFieldAt(x, y, c);
    }

    public void reveal(int x, int y) {
        reveal(x, y, false);
    }

    public void reveal(int x, int y, boolean revealFlags) {
        byte current = board.getFieldAt(x, y);
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
        }
    }

    public void show() {
        gui.addWindowAndWait(mainWindow);
    }

    public void start() {
        board.initialize(10, 10, 10);
        updateBoard();
    }

    private void updateBoard() {
        TerminalPosition caret = boardBox.getCaretPosition();
        updateBoard(caret.getColumn(), caret.getRow());
    }

    private void updateBoard(int cx, int cy) {
        StringBuilder builder = new StringBuilder();

        int numberLen = Integer.toString(board.getSizeY() + 1).length();
        int offsetX = 2 + numberLen;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < board.getSizeX() + offsetX; j++) {
                builder.append(j < offsetX ?
                                       ' ' :
                                       i == 0 ? j == cx ? 'v' : ' ' : i == 1 ? CHARS[j - offsetX % CHARS.length] : '=');
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

        boardBox.setText(builder.toString());
    }
}
