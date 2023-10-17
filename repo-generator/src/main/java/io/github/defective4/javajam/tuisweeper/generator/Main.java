package io.github.defective4.javajam.tuisweeper.generator;

import com.google.gson.*;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import io.github.defective4.javajam.tuisweeper.components.replay.Replay;
import io.github.defective4.javajam.tuisweeper.components.replay.ReplayIO;
import io.github.defective4.javajam.tuisweeper.components.sfx.DummySoundEngine;
import io.github.defective4.javajam.tuisweeper.components.sfx.SoundEngine;
import io.github.defective4.javajam.tuisweeper.components.ui.CustomFileDialogBuilder;
import io.github.defective4.javajam.tuisweeper.components.ui.Dialogs;
import io.github.defective4.javajam.tuisweeper.components.ui.SimpleWindow;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.googlecode.lanterna.TextColor.ANSI.BLACK;
import static com.googlecode.lanterna.TextColor.ANSI.WHITE_BRIGHT;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            SoundEngine sfx = new DummySoundEngine();
            Screen scr = new TerminalScreen(new DefaultTerminalFactory().createTerminal());
            scr.startScreen();
            MultiWindowTextGUI gui = new MultiWindowTextGUI(scr);
            gui.setTheme(
                    SimpleTheme.makeTheme(true,
                                          WHITE_BRIGHT,
                                          BLACK,
                                          WHITE_BRIGHT,
                                          BLACK,
                                          BLACK,
                                          WHITE_BRIGHT,
                                          BLACK)
            );

            CustomFileDialogBuilder bd = (CustomFileDialogBuilder)
                    new CustomFileDialogBuilder(sfx)
                            .setForcedExtension("json")
                            .setTitle("Choose repo location")
                            .setDescription("Choose the \"repo.json\" file")
                            .setActionLabel("Select");
            File file = bd.buildAndShow(gui);
            if (file == null) System.exit(0);
            else {
                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()))) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray themes = obj.getAsJsonArray("themes");
                    JsonArray replays = obj.getAsJsonArray("replays");
                    if (themes == null || replays == null)
                        throw new IOException();

                    Window win = new SimpleWindow();
                    win.setComponent(Panels.horizontal(
                            new Button("Add theme", () -> {
                                win.close();
                                Window win2 = new SimpleWindow();

                                TextBox url = new TextBox();
                                TextBox name = new TextBox();
                                TextBox description = new TextBox(
                                        new TerminalSize(20, 2)
                                );
                                TextBox version = new TextBox("1.0");
                                TextBox author = new TextBox();

                                win2.setComponent(Panels.vertical(
                                        new Label("URL"),
                                        url,
                                        new EmptySpace(),
                                        new Label("Name"),
                                        name,
                                        new EmptySpace(),
                                        new Label("Description"),
                                        description,
                                        new EmptySpace(),
                                        new Label("Version"),
                                        version,
                                        new EmptySpace(),
                                        new Label("Author"),
                                        author,
                                        new EmptySpace(),
                                        Panels.horizontal(
                                                new Button("Add", () -> {
                                                    JsonObject o = new JsonObject();
                                                    o.add("url", new JsonPrimitive(url.getText()));
                                                    o.add("name", new JsonPrimitive(name.getText()));
                                                    o.add("description", new JsonPrimitive(description.getText()));
                                                    o.add("version", new JsonPrimitive(version.getText()));
                                                    o.add("author", new JsonPrimitive(author.getText()));
                                                    o.add("addedDate", new JsonPrimitive(new SimpleDateFormat(
                                                            "yyyy-MM-dd kk:mm"
                                                    ).format(new Date())));
                                                    themes.add(
                                                            o
                                                    );
                                                    String json = new GsonBuilder().setPrettyPrinting().create()
                                                                                   .toJson(obj);
                                                    try (OutputStream os = Files.newOutputStream(file.toPath())) {
                                                        os.write(json.getBytes(StandardCharsets.UTF_8));
                                                        System.exit(0);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                        Dialogs.showErrorDialog(gui, e, sfx, "Error");
                                                        System.exit(0);
                                                    }
                                                }),
                                                new Button("Cancel", () -> System.exit(0))
                                        )
                                ));

                                gui.addWindowAndWait(win2);
                            }),

                            new Button("Add replay", () -> {
                                CustomFileDialogBuilder bd2 = (CustomFileDialogBuilder)
                                        new CustomFileDialogBuilder(sfx)
                                                .setForcedExtension("jbcfrt")
                                                .setTitle("Choose a replay")
                                                .setDescription("Choose a replay file")
                                                .setActionLabel("Select");
                                try {
                                    File rpl = bd2.buildAndShow(gui);
                                    Replay replay = ReplayIO.read(rpl);
                                    Window win2 = new SimpleWindow();

                                    TextBox url = new TextBox();
                                    TextBox author = new TextBox();

                                    win2.setComponent(Panels.vertical(
                                            new Label("URL"),
                                            url,
                                            new EmptySpace(),
                                            new Label("Author"),
                                            author,
                                            new EmptySpace(),
                                            Panels.horizontal(
                                                    new Button("Ok", () -> {
                                                        JsonObject o = new JsonObject();

                                                        o.add("width", new JsonPrimitive(replay.getWidth()));
                                                        o.add("height", new JsonPrimitive(replay.getHeight()));
                                                        o.add("bombs", new JsonPrimitive(replay.getBombs().size()));
                                                        o.add("addedTime", new JsonPrimitive(
                                                                Replay.DATE_FORMAT.format(new Date())
                                                        ));
                                                        o.add("createdTime", new JsonPrimitive(
                                                                Replay.DATE_FORMAT.format(new Date(replay.getMetadata()
                                                                                                         .getCreatedDate()))
                                                        ));
                                                        o.add("playTime", new JsonPrimitive(
                                                                replay.getTime()
                                                        ));
                                                        o.add("difficulty", new JsonPrimitive(
                                                                replay.getMetadata().getDifficulty().getId()
                                                        ));
                                                        o.add("url", new JsonPrimitive(
                                                                url.getText()
                                                        ));
                                                        o.add("identifier", new JsonPrimitive(
                                                                replay.getMetadata().getIdentifier()
                                                        ));
                                                        o.add("author", new JsonPrimitive(
                                                                author.getText()
                                                        ));

                                                        replays.add(o);

                                                        try (OutputStream os = Files.newOutputStream(file.toPath())) {
                                                            os.write(new GsonBuilder().setPrettyPrinting()
                                                                                      .create()
                                                                                      .toJson(obj)
                                                                                      .getBytes(
                                                                                              StandardCharsets.UTF_8));
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                            Dialogs.showErrorDialog(gui, e, sfx, "Error");
                                                        } finally {
                                                            win2.close();
                                                        }
                                                    }),
                                                    new Button("Cancel", win2::close)
                                            )
                                    ));

                                    gui.addWindowAndWait(win2);
                                    System.exit(0);
                                } catch (Exception e) {
                                    Dialogs.showErrorDialog(gui, e, sfx, "Error");
                                    System.exit(0);
                                }
                            })
                    ));
                    gui.addWindowAndWait(win);
                } catch (Exception e) {
                    Dialogs.showErrorDialog(gui, e, sfx, "Couldn't parse repo file");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
