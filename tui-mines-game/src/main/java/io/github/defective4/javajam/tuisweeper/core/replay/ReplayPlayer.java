package io.github.defective4.javajam.tuisweeper.core.replay;

import com.googlecode.lanterna.gui2.TextBox;
import io.github.defective4.javajam.tuisweeper.components.replay.Replay;
import io.github.defective4.javajam.tuisweeper.core.TUIMines;
import io.github.defective4.javajam.tuisweeper.components.sfx.SFXEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Used to play replays.
 *
 * @author Defective
 */
public class ReplayPlayer {

    private final TUIMines game;
    private final TextBox box;
    private final SFXEngine sfx;

    private final ScheduledThreadPoolExecutor svc = new ScheduledThreadPoolExecutor(1);
    private final List<ScheduledFuture<?>> tasks = new ArrayList<>();
    private boolean stopped = true;

    public ReplayPlayer(TUIMines game, TextBox box, SFXEngine sfx) {
        this.game = game;
        this.box = box;
        this.sfx = sfx;
        svc.setRemoveOnCancelPolicy(true);
    }

    public void play(Replay replay) {
        List<Replay.Action> actions = replay.getActions();
        stopped = false;
        for (Replay.Action act : actions) {
            tasks.add(svc.schedule(() -> {
                switch (act.getAction()) {
                    case CARET: {
                        box.setCaretPosition(act.getY(), act.getX());
                        break;
                    }
                    case FLAG: {
                        game.flag(act.getX(), act.getY());
                        break;
                    }
                    case REVEAL: {
                        int cnt = game.reveal(act.getX(), act.getY());
                        if (cnt > 0) {
                            sfx.play(cnt > 10 ? "mass_reveal" : "reveal");
                        }
                        break;
                    }
                    default:
                        break;
                }
            }, act.getTimestamp() + 1000, TimeUnit.MILLISECONDS));
        }
    }

    public void stop() {
        for (ScheduledFuture<?> task : tasks)
            task.cancel(true);
        tasks.clear();
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }
}
