package io.github.defective4.javajam.tuisweeper.core.storage;

import io.github.defective4.javajam.tuisweeper.core.Difficulty;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Leaderboards {

    public static class Entry {
        private final long date, time;

        public Entry(long date, long time) {
            this.date = date;
            this.time = time;
        }

        public long getDate() {
            return date;
        }

        public long getTime() {
            return time;
        }
    }

    private Connection con;

    private boolean isAvailable = false;

    public Leaderboards() {
        try (Statement stmt = mkStatement()) {
            try (ResultSet set = stmt.executeQuery(
                    "select * from sqlite_master where type = \"table\" AND name = \"times\"")) {
                if (!set.next()) {
                    stmt.execute(
                            "create table times (id integer not null primary key autoincrement, difficulty integer, time integer, date integer)");
                    stmt.execute("create index diff on times (difficulty)");
                }
            }
            isAvailable = true;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    private void connect() throws SQLException {
        File dbFile = Preferences.getDatabaseFile();
        dbFile.getParentFile().mkdirs();
        con = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }

    private Statement mkStatement() throws SQLException {
        try {
            if (!con.isValid(1000)) connect();
            return con.createStatement();
        } catch (Exception e) {
            connect();
            return con.createStatement();
        }
    }

    public void addEntry(Difficulty diff, long time) {
        try (Statement stmt = mkStatement()) {
            stmt.execute(String.format("insert into times (difficulty, time, date) values (%s, %s, %s)",
                                       diff.getId(),
                                       time,
                                       System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Entry[] getEntries(Difficulty diff, int max) {
        try (Statement stmt = mkStatement()) {
            try (ResultSet set = stmt.executeQuery(String.format(
                    "select * from times where difficulty = %s order by time asc limit 10",
                    diff.getId()))) {
                List<Entry> entries = new ArrayList<>();
                while (set.next()) {
                    entries.add(new Entry(set.getLong(4), set.getLong(3)));
                }
                return entries.toArray(new Entry[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Entry[0];
        }
    }
}
