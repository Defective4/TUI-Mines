package io.github.defective4.javajam.tuisweeper.core.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A remote repository.
 * This can be used to retrieve information about user submitted themes and replays.
 *
 * @author Defective
 */
public class Repository {

    /**
     *
     */
    public interface RemoteFuture {
        void completed(Exception ex);
    }

    private static final String REPO = "https://raw.githubusercontent.com/Defective4/TUI-Mines-Repo/master/repo.json";

    private final List<RemoteTheme> themes = new ArrayList<>();

    private final ExecutorService service = Executors.newFixedThreadPool(1);
    private boolean stopped = true;

    public RemoteTheme[] getThemes() {
        return themes.toArray(new RemoteTheme[0]);
    }

    public void cancel(Future<?> future) {
        future.cancel(true);
        stopped = true;
    }

    public Future<?> fetch(RemoteFuture callback) {
        themes.clear();
        stopped = false;
        return service.submit(() -> {
            try (InputStreamReader reader = new InputStreamReader(new URL(REPO).openStream())) {
                if (stopped)
                    return;
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
                callback.completed(null);
            } catch (Exception e) {
                callback.completed(e);
            }
        });
    }
}
