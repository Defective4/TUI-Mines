package io.github.defective4.javajam.tuisweeper;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;

import java.awt.*;
import java.io.IOException;

public final class Main {
    public static void main(String[] args) {
        try {
            Terminal term = new DefaultTerminalFactory().createTerminal();
            if(term instanceof SwingTerminalFrame) {
                SwingTerminalFrame frame = (SwingTerminalFrame) term;
                frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
            }

            Screen screen = new TerminalScreen(term);
            screen.startScreen();
            TUISweeper game = new TUISweeper(screen);
            game.start();
            game.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
