package io.github.defective4.javajam.tuisweeper.core.integr;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.defective4.javajam.tuisweeper.Main;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Provides basic integration with GitHub.
 * It is mainly used for update checking.
 *
 * @author Defective
 */
public final class GithubIntegr {
    private GithubIntegr() {
    }

    public static String checkUpdates() throws IOException {
        String remote = checkLatestVersion();
        if (remote == null) return null;
        return !("v" + Main.VERSION).equals(remote) ? remote : null;
    }

    public static String checkLatestVersion() throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(
                "https://api.github.com/repos/Defective4/TUI-Mines/releases").openConnection();
        con.setRequestProperty("Accept", "application/vnd.github+json");
        con.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        try (InputStreamReader reader = new InputStreamReader(con.getInputStream())) {
            JsonArray response = JsonParser.parseReader(reader).getAsJsonArray();
            if (!response.isEmpty()) {
                JsonObject obj = response.get(0).getAsJsonObject();
                return obj.get("tag_name").getAsString();
            } else return "v" + Main.VERSION;
        }
    }
}
