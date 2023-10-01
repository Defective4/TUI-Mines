package io.github.defective4.javajam.tuisweeper.core.network;

import io.github.defective4.javajam.tuisweeper.core.Difficulty;
import io.github.defective4.javajam.tuisweeper.core.replay.Replay;
import io.github.defective4.javajam.tuisweeper.core.replay.ReplayIO;

import java.io.InputStream;
import java.net.URL;

public class RemoteReplay {

    public enum Sorting {
        DATE("Date added"), TIME("Replay time"), CREATED("Date created");
        private final String name;

        Sorting(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final int width, height, bombs;
    private final long addedTime, createdTime, playTime;
    private final Difficulty difficulty;
    private final String url, identifier, author;

    public RemoteReplay(int width, int height, int bombs, long addedTime, long createdTime, long playTime, Difficulty difficulty, String url, String identifier, String author) {
        this.width = width;
        this.height = height;
        this.bombs = bombs;
        this.addedTime = addedTime;
        this.createdTime = createdTime;
        this.playTime = playTime;
        this.difficulty = difficulty;
        this.url = url;
        this.identifier = identifier;
        this.author = author;
    }

    public Replay fetch() {
        try (InputStream is = new URL(url).openStream()) {
            return ReplayIO.read(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUrl() {
        return url;
    }

    public long getPlayTime() {
        return playTime;
    }

    public String getAuthor() {
        return author;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBombs() {
        return bombs;
    }

    public long getAddedTime() {
        return addedTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getIdentifier() {
        return identifier;
    }
}
