package io.github.defective4.javajam.tuisweeper;

import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import io.github.defective4.javajam.tuisweeper.core.ui.SimpleWindow;

import java.awt.*;
import java.io.IOException;

import static com.googlecode.lanterna.TextColor.ANSI;

public final class Main {
    public static void main(String[] args) {
        try {
            Terminal term = new DefaultTerminalFactory().createTerminal();
            if (term instanceof SwingTerminalFrame) {
                SwingTerminalFrame frame = (SwingTerminalFrame) term;
                frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
            }
            Screen screen = new TerminalScreen(term);
            screen.startScreen();
            WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
            Window win = new SimpleWindow(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS);
            win.setTheme(SimpleTheme.makeTheme(true,
                                               ANSI.WHITE_BRIGHT,
                                               ANSI.BLACK,
                                               ANSI.WHITE_BRIGHT,
                                               ANSI.BLACK,
                                               ANSI.WHITE_BRIGHT,
                                               ANSI.BLACK,
                                               ANSI.BLACK));

            gui.addWindowAndWait(win);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
