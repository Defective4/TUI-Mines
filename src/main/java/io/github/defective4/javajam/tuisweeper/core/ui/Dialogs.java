package io.github.defective4.javajam.tuisweeper.core.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXButton;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXEngine;
import io.github.defective4.javajam.tuisweeper.core.sfx.SFXMessageDialogBuilder;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class used to show exception dialogs to user.
 *
 * @author Defective
 */
public final class Dialogs {
    private Dialogs() {
    }

    public static Window showDownloadingWindow(WindowBasedTextGUI gui) {
        Window win = new SimpleWindow("Please wait");

        win.setComponent(Panels.vertical(
                new Label("Downloading...\n" +
                          "Please wait"),
                new EmptySpace()
        ));
        gui.addWindow(win);
        return win;
    }

    public static void showErrorDialog(WindowBasedTextGUI gui, Exception e, SFXEngine sfx, String... text) {
        Window win = new SimpleWindow("Error");
        String msg = e.getLocalizedMessage();
        String cName = e.getClass().getSimpleName();

        win.setComponent(Panels.vertical(
                new Label(String.join("\n", text)),
                new EmptySpace(), new Label("Exception:"),
                new Label(msg == null ? cName : cName + ": " + msg).setPreferredSize(new TerminalSize(40,
                                                                                                      1))
                                                                   .withBorder(Borders.singleLine()),
                new EmptySpace(),
                Panels.horizontal(
                        new SFXButton("Ok", sfx, true, win::close),
                        new SFXButton("Show stack trace", sfx, false, () -> {
                            Window trace = new SimpleWindow("Exception stack trace");
                            List<String> stackTrace = new ArrayList<>();
                            stackTrace.add(e.toString());
                            stackTrace.addAll(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(
                                    Collectors.toList()));

                            TextBox box = new TextBox(String.join("\n", stackTrace.toArray(new String[0])));
                            box.setPreferredSize(new TerminalSize(50, 20));
                            box.setInputFilter((i, key) -> {
                                switch (key.getKeyType()) {
                                    case ArrowDown:
                                    case ArrowLeft:
                                    case ArrowRight:
                                    case ArrowUp:
                                    case Tab:
                                    case ReverseTab:
                                    case PageDown:
                                    case PageUp:
                                    case Home:
                                        return true;
                                    default:
                                        return false;
                                }
                            });
                            box.setCaretPosition(0, 0);

                            Button ok = new SFXButton("Ok", sfx, true, trace::close);

                            trace.setComponent(Panels.vertical(
                                    box.withBorder(Borders.singleLine()),
                                    Panels.horizontal(
                                            ok,
                                            new SFXButton("Copy to clipboard", sfx, false, () -> {
                                                try {
                                                    Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
                                                    StringSelection sel = new StringSelection(box.getText());
                                                    cp.setContents(sel, sel);
                                                    new SFXMessageDialogBuilder(sfx)
                                                            .setTitle("Copied!")
                                                            .setText("Stack trace copied to clipboard")
                                                            .addButton(MessageDialogButton.OK)
                                                            .build().showDialog(gui);
                                                } catch (Exception ignored) {
                                                }
                                            }),
                                            new SFXButton("Save to file", sfx, false, () -> {
                                                File f = new File(
                                                        "Exception_" + new SimpleDateFormat("yyyy_MM_dd kk_mm_ss").format(
                                                                new Date()) + ".txt");
                                                try (OutputStream os = Files.newOutputStream(f.toPath()
                                                )) {
                                                    os.write(box.getText().getBytes(StandardCharsets.UTF_8));
                                                    os.close();
                                                    String path = f.getAbsolutePath();
                                                    if (path.length() > 50)
                                                        path = "..." + path.substring(path.length() - 47);
                                                    new SFXMessageDialogBuilder(sfx)
                                                            .setTitle("Saved!")
                                                            .setText("Stack trace saved to:\n" + path)
                                                            .addButton(MessageDialogButton.OK)
                                                            .build().showDialog(gui);
                                                } catch (Exception ignored) {
                                                }
                                            })
                                    )
                            ));
                            ok.takeFocus();
                            gui.addWindowAndWait(trace);
                        })
                )
        ));
        sfx.play("error");
        gui.addWindowAndWait(win);
    }
}
