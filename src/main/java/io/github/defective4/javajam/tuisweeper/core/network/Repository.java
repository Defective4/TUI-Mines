package io.github.defective4.javajam.tuisweeper.core.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Repository {
    private static final String REPO = "http://10.0.0.1:3000/Defective4/TUI-Sweeper-Repo/raw/branch/master/repo.json";

    private final List<RemoteTheme> themes = new ArrayList<>();


    public RemoteTheme[] getThemes() {
        return themes.toArray(new RemoteTheme[0]);
    }

    public void fetch() {
        themes.clear();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
