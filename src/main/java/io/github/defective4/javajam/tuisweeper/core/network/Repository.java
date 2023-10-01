package io.github.defective4.javajam.tuisweeper.core.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.defective4.javajam.tuisweeper.core.Difficulty;
import io.github.defective4.javajam.tuisweeper.core.replay.Replay;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Repository {
    private static final String REPO = "http://10.0.0.1:3000/Defective4/TUI-Sweeper-Repo/raw/branch/master/repo.json";

    private final List<RemoteTheme> themes = new ArrayList<>();
    private final List<RemoteReplay> replays = new ArrayList<>();


    public RemoteTheme[] getThemes() {
        return themes.toArray(new RemoteTheme[0]);
    }

    public RemoteReplay[] getReplays() {
        return replays.toArray(new RemoteReplay[0]);
    }

    public void fetch() { // TODO add erro reporting
        themes.clear();
        replays.clear();
        try (InputStreamReader reader = new InputStreamReader(new URL(REPO).openStream())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (JsonElement el : json.getAsJsonArray("themes")) {
                if (el.isJsonObject()) {
                    JsonObject themeO = el.getAsJsonObject();
                    long time;
                    try {
                        time = RemoteTheme.DATE_FORMAT.parse(themeO.get("addedDate").getAsString()).getTime();
                    } catch (Exception e) {
                        time = 0;
                    }

                    themes.add(new RemoteTheme(
                            themeO.get("url").getAsString(),
                            themeO.get("name").getAsString(),
                            themeO.get("description").getAsString(),
                            themeO.get("version").getAsString(),
                            themeO.get("author").getAsString(),
                            time
                    ));
                }
            }
            for (JsonElement el : json.getAsJsonArray("replays")) {
                if (el.isJsonObject()) {
                    JsonObject replayO = el.getAsJsonObject();
                    long addedTime, createdTime, playTime;
                    try {
                        addedTime = Replay.DATE_FORMAT.parse(replayO.get("addedTime").getAsString()).getTime();
                        createdTime = Replay.DATE_FORMAT.parse(replayO.get("createdTime").getAsString()).getTime();
                        playTime = Long.parseLong(replayO.get("playTime").getAsString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        addedTime = 0;
                        createdTime = 0;
                        playTime = 0;
                    }

                    replays.add(new RemoteReplay(replayO.get("width").getAsInt(),
                                                 replayO.get("height").getAsInt(),
                                                 replayO.get("bombs").getAsInt(),
                                                 addedTime,
                                                 createdTime,
                                                 playTime,
                                                 Difficulty.getByID(replayO.get("difficulty").getAsInt()),
                                                 replayO.get("url").getAsString(),
                                                 replayO.get("identifier").getAsString(),
                                                 replayO.get("author").getAsString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
