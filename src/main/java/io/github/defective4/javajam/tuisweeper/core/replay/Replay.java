package io.github.defective4.javajam.tuisweeper.core.replay;

import io.github.defective4.javajam.tuisweeper.core.Difficulty;

import java.util.ArrayList;
import java.util.List;

public class Replay {

    public enum ActionType {
        CARET(0), REVEAL(1), FLAG(2);
        private final int id;

        ActionType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

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
    }

    // Board info
    private final List<CoordPair> bombs = new ArrayList<>();
    private int width, height;

    // Replay data
    private final List<Action> actions = new ArrayList<>();
    private final long startTime;
    private final Metadata metadata = new Metadata();

    public Metadata getMetadata() {
        return metadata;
    }

    public Replay(long startTime) {
        this.startTime = startTime;
    }

    public Replay() {
        startTime = System.currentTimeMillis();
    }

    public List<CoordPair> getBombs() {
        return bombs;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
               '}';
    }
}
