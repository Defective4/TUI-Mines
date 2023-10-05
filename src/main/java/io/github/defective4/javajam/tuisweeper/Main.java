package io.github.defective4.javajam.tuisweeper;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import io.github.defective4.javajam.tuisweeper.core.DiscordIntegr;
import io.github.defective4.javajam.tuisweeper.core.TUIMines;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXButton;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXEngine;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXMessageDialogBuilder;
import io.github.defective4.javajam.tuisweeper.core.storage.Preferences;
import io.github.defective4.javajam.tuisweeper.core.ui.Dialogs;
import io.github.defective4.javajam.tuisweeper.core.ui.SimpleWindow;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import static io.github.defective4.javajam.tuisweeper.core.ui.Dialogs.showErrorDialog;

/**
 * Main entry point of the game
 *
 * @author Defective
 */
public final class Main {

    public static final String VERSION = "1.0";

    private Main() {
    }

    public static void openLink(String url, SFXEngine sfx, WindowBasedTextGUI gui) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        try {
            if (desktop == null)
                throw new IllegalStateException();
            desktop.browse(new URI(url));
            new SFXMessageDialogBuilder(sfx)
                    .setTitle("")
                    .setText("The link should now open in your default browser.")
                    .addButton(MessageDialogButton.OK)
                    .build().showDialog(gui);
        } catch (Exception ignored) {
            try {
                StringSelection content = new StringSelection(url);
                Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
                cp.setContents(content, content);
            } catch (Exception ex) {
                Dialogs.showErrorDialog(gui, ex, sfx, "Couldn't copy link to the clipbaord...\n" + url);
            }
        }
    }

    public static void main(String[] args) {
        try {
            DefaultTerminalFactory factory = new DefaultTerminalFactory();
            Timer timer = new Timer(true);
            Preferences prefs = Preferences.load();
            DiscordIntegr.init();
            DiscordIntegr.setEnabled(prefs.getOptions().isDiscordIntegrationEnabled(), null);
            DiscordIntegr.title();
            SFXEngine sfx = new SFXEngine();
            TextBox box = new TextBox();
            box.setReadOnly(true);

            factory.setPreferTerminalEmulator(System.getProperty("os.name")
                                                    .toLowerCase()
                                                    .contains("windows") || args.length > 0 && args[0].equalsIgnoreCase(
                    "-gui"));

            Terminal term = factory.createTerminal();
            Screen screen = new TerminalScreen(term);
            screen.startScreen();

            WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
            gui.setTheme(prefs.getTheme().toTUITheme());
            Window win = new SimpleWindow(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS);

            StringBuilder brandBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(
                    "/logo.txt")))) {
                String line;
                while ((line = reader.readLine()) != null) brandBuilder.append(line).append("\n");
            } catch (Exception e) {
                showErrorDialog(gui, e, sfx, "Couldn't load title screen ASCII art!");
            }
            String brand = brandBuilder.toString();

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!win.isVisible()) cancel();
                    box.setText(brand + "\n" + "\n                     " + (System.currentTimeMillis() / 500 % 2 == 0 ?
                                                                            "PRESS ANY KEY TO START" :
                                                                            ""));

                }
            }, 0, 125);

            box.setInputFilter((interactable, keyStroke) -> {
                if (keyStroke.getKeyType() == KeyType.Character || keyStroke.getKeyType() == KeyType.Enter) {
                    if (prefs.isFirstBoot()) {
                        prefs
                                .getOptions().setSounds(new MessageDialogBuilder()
                                                                .setText(
                                                                        "Do you want to enable sounds?\nYou can always change this setting in\ngame menu!")
                                                                .setTitle("Enable sounds?")
                                                                .addButton(MessageDialogButton.Yes)
                                                                .addButton(MessageDialogButton.No)
                                                                .build().showDialog(gui) == MessageDialogButton.Yes);
                        sfx.setEnabled(prefs.getOptions().areSoundsEnabled());
                        Window twin = new SimpleWindow("First time");
                        twin.setComponent(Panels.vertical(
                                new Label("It looks like it's your first time playing TUI Mines!"),
                                new EmptySpace(),
                                Panels.horizontal(
                                        new SFXButton("View controls", sfx, false, () -> TUIMines.showCtls(gui, sfx, prefs)),
                                        new SFXButton("Continue", sfx, true, twin::close)
                                )
                        ));
                        gui.addWindowAndWait(twin);
                    }
                    win.setVisible(false);
                    timer.cancel();
                    prefs.setFirstBoot(false);
                    TUIMines game = new TUIMines(screen, gui, term, sfx, prefs);
                    game.start();
                    game.show();
                }
                return true;
            });

            win.setComponent(box);


            if (term instanceof SwingTerminalFrame) {
                SwingTerminalFrame frame = (SwingTerminalFrame) term;
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize((int) (dim.getWidth() - 4), (int) dim.getHeight());
                frame.setLocation(2, 0);
                frame.setTitle("TUI-Mines");
                try {
                    frame.setIconImage(ImageIO.read(Main.class.getResourceAsStream("/logo.png")));
                } catch (Exception e) {
                    showErrorDialog(gui, e, sfx, "Couldn't load logo image!");
                }
            }

            if (term instanceof UnixLikeTerminal) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        new MessageDialogBuilder()
                                .setTitle("Native warning")
                                .addButton(MessageDialogButton.Continue)
                                .setText("It looks like you are running TUI-Mines\n" + "in a native terminal.\n" + "If you run into any issues try launching\n" + "this app with argument \"-gui\",\n" + "for example:\n" + "java -jar tui-mines.jar -gui")
                                .build().showDialog(gui);
                    }
                }, 1);
            }

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TerminalSize size = screen.getTerminalSize();
                    if (size.getColumns() < 61 || size.getRows() < 34) {
                        new MessageDialogBuilder()
                                .setTitle("Warning")
                                .addButton(MessageDialogButton.OK)
                                .setText("Your terminal has really small size. \n" + "Not all elements may be visible at once,\nso the playing experience may be affected\n" + "If you are using a terminal emulator/terminal screen \nplease resize/maximize the window.\n" + "Thank you!")
                                .build().showDialog(gui);
                    }
                }
            }, 1000);
            gui.addWindowAndWait(win);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
