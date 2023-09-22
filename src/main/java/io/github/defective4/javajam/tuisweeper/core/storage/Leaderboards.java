package io.github.defective4.javajam.tuisweeper.core.storage;

import io.github.defective4.javajam.tuisweeper.core.Difficulty;

import java.util.*;

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

    private final Map<Difficulty, List<Entry>> entries = new HashMap<>();

    public Entry[] getEntries(Difficulty diff, int max) {
        if (!entries.containsKey(diff)) entries.put(diff, new ArrayList<>());
        List<Entry> entries = new ArrayList<>(this.entries.get(diff));

        entries.sort((o1, o2) -> (int) (o1.getTime() - o2.getTime()));

        return Arrays.copyOf(entries.toArray(new Entry[0]), Math.min(max, entries.size()));
    }
}
