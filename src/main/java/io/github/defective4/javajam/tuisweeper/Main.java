package io.github.defective4.javajam.tuisweeper;

import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;

import java.io.IOException;

public final class Main {
    public static void main(String[] args) {
        try {
            Screen screen = new DefaultTerminalFactory().createScreen();
            screen.startScreen();
            TUISweeper game = new TUISweeper(screen);
            game.start();
            game.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
