package io.github.defective4.javajam.tuisweeper.core.replay;

import io.github.defective4.javajam.tuisweeper.components.replay.Replay;
import io.github.defective4.javajam.tuisweeper.core.MineBoard;
import io.github.defective4.javajam.tuisweeper.core.TUIMines;

/**
 * Records a game session to {@link Replay} instance
 *
 * @author Defective
 */
public class ReplayRecorder {
    private final MineBoard board;
    private final TUIMines game;
    private Replay replay;
    private boolean enabled;

    public ReplayRecorder(MineBoard board, TUIMines game) {
        this.board = board;
        this.game = game;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void init(MineBoard board) {
        if (isStarted()) {
            byte[][] matrix = board.getMatrix();
            for (int x = 0; x < matrix.length; x++)
                for (int y = 0; y < matrix[x].length; y++)
                    if (matrix[x][y] == 11)
                        replay.getBombs().add(new Replay.CoordPair(x, y));
            replay.setSeed(board.getSeed());
            replay.setWidth(matrix.length);
            replay.setHeight(matrix.length > 0 ? matrix[0].length : 0);
            replay.getMetadata().setDifficulty(game.getLocalDifficulty());
        }
    }

    public boolean isStarted() {
        return replay != null;
    }

    public void action(Replay.ActionType type, int x, int y) {
        if (isStarted()) {
            replay.getActions().add(new Replay.Action(type, x, y, System.currentTimeMillis() - replay.getStartTime()));
        }
    }

    public void start() {
        if (isEnabled() && replay == null) {
            replay = new Replay();
            init(board);
        }
    }

    public Replay getReplay() {
        return replay;
    }

    public void stop() {
        replay = null;
    }
}
