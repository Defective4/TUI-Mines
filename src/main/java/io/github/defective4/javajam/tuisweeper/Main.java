package io.github.defective4.javajam.tuisweeper;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal;
import io.github.defective4.javajam.tuisweeper.core.TUISweeper;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFX;
import io.github.defective4.javajam.tuisweeper.core.ui.SimpleWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import static com.googlecode.lanterna.TextColor.ANSI;

public final class Main {
    public static void main(String[] args) {
        try {
            SFX sfx = new SFX();

            DefaultTerminalFactory factory = new DefaultTerminalFactory();

            factory.setPreferTerminalEmulator(System.getProperty("os.name")
                                                    .equalsIgnoreCase("windows") || (args.length > 0 && args[0].equalsIgnoreCase(
                    "-gui")));
            Terminal term = factory.createTerminal();

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
                    box.setText(brand + "\n" + "\n                     " + ((System.currentTimeMillis() / 500 % 2 == 0) ?
                            "PRESS ANY KEY TO START" :
                            ""));

                }
            }, 0, 125);

            box.setInputFilter((interactable, keyStroke) -> {
                if (keyStroke.getKeyType() == KeyType.Character || keyStroke.getKeyType() == KeyType.Enter) {
                    win.setVisible(false);
                    flashTimer.cancel();
                    TUISweeper game = new TUISweeper(screen, gui, term, sfx);
                    game.start();
                    game.show();
                }
                return true;
            });

            win.setComponent(box);

            if (term instanceof JFrame) {
                JFrame frame = (JFrame) term;
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setSize((int) (dim.getWidth() - 4), (int) dim.getHeight());
                frame.setLocation(2, 0);
                frame.setTitle("TUI-Sweeper");
                try {
                    frame.setIconImage(ImageIO.read(Main.class.getResourceAsStream("/logo.png")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            Timer tm = new Timer(true);
            if (term instanceof UnixLikeTerminal) {
                tm.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Window warn = new BasicWindow("Native warning");
                        warn.setHints(Collections.singleton(Window.Hint.CENTERED));
                        Panel panel = new Panel(new LinearLayout());

                        panel.addComponent(new Label("It looks like you are running TUI-Sweeper\n" + "in a native terminal.\n" + "If you run into any issues try launching\n" + "this app with argument \"-gui\",\n" + "for example:\n" + "java -jar tui-sweeper.jar -gui"));

                        panel.addComponent(new Button("Continue", warn::close));
                        warn.setComponent(panel);
                        gui.addWindowAndWait(warn);
                    }
                }, 1);
            }

            tm.schedule(new TimerTask() {
                @Override
                public void run() {
                    TerminalSize size = screen.getTerminalSize();
                    if (size.getColumns() < 61 || size.getRows() < 34) {
                        Window warn = new SimpleWindow("Warning");
                        warn.setTheme(win.getTheme());
                        Panel panel = new Panel(new LinearLayout());
                        panel.addComponent(new Label("Your terminal has really small size. \n" + "Not all elements may be visible at once,\nso the playing experience may be affected\n" + "If you are using a terminal emulator/terminal screen \nplease resize/maximize the window.\n" + "Thank you!\n "));

                        panel.addComponent(new Button("Ok", warn::close));
                        warn.setComponent(panel);
                        gui.addWindowAndWait(warn);
                    }
                }
            }, 1000);
            gui.addWindowAndWait(win);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
