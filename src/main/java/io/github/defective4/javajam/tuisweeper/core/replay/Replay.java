package io.github.defective4.javajam.tuisweeper.core.replay;

import io.github.defective4.javajam.tuisweeper.core.Difficulty;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Replay {
    public static class Action {
        private final ActionType action;
        private final int x, y;
        private final long timestamp;

        public Action(ActionType action, int x, int y, long timestamp) {
            this.action = action;
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }

        public ActionType getAction() {
            return action;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "Action{" +
                   "action=" + action +
                   ", x=" + x +
                   ", y=" + y +
                   ", timestamp=" + timestamp +
                   '}';
        }
    }

    public static class CoordPair {
        private final int x, y;

        public CoordPair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "CoordPair{" +
                   "x=" + x +
                   ", y=" + y +
                   '}';
        }
    }

    public static class Metadata {
        private Difficulty difficulty = Difficulty.CUSTOM;
        private String identifier = "";
        private long createdDate = System.currentTimeMillis();
        private File origin;

        public File getOrigin() {
            return origin;
        }

        public void setOrigin(File origin) {
            this.origin = origin;
        }

        public long getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return "Metadata{" +
                   "difficulty=" + difficulty +
                   ", identifier='" + identifier + '\'' +
                   ", createdDate=" + createdDate +
                   '}';
        }
    }

    public enum ActionType {
        CARET(0), REVEAL(1), FLAG(2);
        private final int id;

        ActionType(int id) {
            this.id = id;
        }

        public static ActionType getByID(int id) {
            for (ActionType type : values())
                if (type.getId() == id)
                    return type;
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("mm:ss");
    public static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("yyyy_MM_dd kk_ss");
    private final List<CoordPair> bombs = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private final long startTime;
    private final Metadata metadata = new Metadata();
    private int width, height;
    private long time = -1;
    private long seed = System.currentTimeMillis();

    public Replay(long startTime) {
        this.startTime = startTime;
    }

    public Replay() {
        startTime = System.currentTimeMillis();
    }

    private void calculateTime() {
        for (Action act : actions)
            time = Math.max(time, act.getTimestamp());
    }

    public long getTime() {
        if (time < 0)
            calculateTime();
        return time;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<CoordPair> getBombs() {
        return bombs;
    }

    public int getWidth() {
        return width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public List<Action> getActions() {
        return actions;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "Replay{" +
               "bombs=" + bombs +
               ", width=" + width +
               ", height=" + height +
               ", actions=" + actions +
               ", startTime=" + startTime +
               ", metadata=" + metadata +
               ", time=" + time +
               '}';
    }
}
