package io.github.defective4.javajam.tuisweeper;

import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import io.github.defective4.javajam.tuisweeper.core.ui.SimpleWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import static com.googlecode.lanterna.TextColor.ANSI;

public final class Main {
    public static void main(String[] args) {
        try {
            Terminal term = new DefaultTerminalFactory().createTerminal();
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
            TextBox box = new TextBox();
            box.setReadOnly(true);

            StringBuilder brandBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(
                    "/logo.txt")))) {
                String line;
                while ((line = reader.readLine()) != null) brandBuilder.append(line).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            String brand = brandBuilder.toString();

            Timer flashTimer = new Timer(true);
            flashTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    box.setText(brand + "\n" + "\n                         " + ((System.currentTimeMillis() / 500 % 2 == 0) ?
                            "PRESS ANY KEY TO START" :
                            ""));

                }
            }, 0, 125);

            win.setComponent(box);

            if (term instanceof SwingTerminalFrame) {
                SwingTerminalFrame frame = (SwingTerminalFrame) term;
                frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
                try {
                    frame.setIconImage(ImageIO.read(Main.class.getResourceAsStream("/logo.png")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            gui.addWindowAndWait(win);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
