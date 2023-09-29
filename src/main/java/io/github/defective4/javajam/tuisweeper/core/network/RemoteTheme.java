package io.github.defective4.javajam.tuisweeper.core.network;

import java.text.SimpleDateFormat;

public class RemoteTheme {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm");

    private final String url, name, description, version, author;
    private final long addedDate;

    public RemoteTheme(String url, String name, String description, String version, String author, long addedDate) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.version = version;
        this.author = author;
        this.addedDate = addedDate;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) by %s", name, version, author);
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public long getAddedDate() {
        return addedDate;
    }
}
