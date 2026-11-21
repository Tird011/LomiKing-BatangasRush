package com.lomiking.core.task;

import com.lomiking.core.cooking.PlayerInput;
import com.lomiking.core.engine.GameContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A note hanging on the board. Interactive tasks are played with gestures (wipe, scrub, sweep);
 * tracking tasks finish on their own when the game state says so (order served, pantry restocked).
 */
public abstract class Task {
    private final String id;
    private final String title;
    private final Sector sector;
    private final double rewardMoney;
    private final double timeLimit;   // <= 0 means no deadline
    private double timeLeft;
    private TaskStatus status = TaskStatus.POSTED;

    protected Task(String id, String title, Sector sector, double rewardMoney, double timeLimit) {
        this.id = id; this.title = title; this.sector = sector;
        this.rewardMoney = rewardMoney; this.timeLimit = timeLimit; this.timeLeft = timeLimit;
    }

    public abstract boolean isInteractive();
    /** 0..1 */
    public abstract double getProgress();
    /** Tracking tasks: is the goal met? Interactive tasks usually just return getProgress() >= 1. */
    protected abstract boolean isGoalMet(GameContext ctx);
    /** Gesture handling for interactive tasks. */
    protected void onInput(PlayerInput input, GameContext ctx) { }
    protected void fillViewState(Map<String, Object> out) { }

    public final void handleInput(PlayerInput input, GameContext ctx) {
        if (status == TaskStatus.ACTIVE && isInteractive()) onInput(input, ctx);
    }

    /** @return true if the status changed to DONE or EXPIRED during this update. */
    public final boolean update(double dt, GameContext ctx) {
        if (isFinished()) return false;
        if (isGoalMet(ctx)) { status = TaskStatus.DONE; return true; }
        if (timeLimit > 0) {
            timeLeft -= dt;
            if (timeLeft <= 0) { status = TaskStatus.EXPIRED; return true; }
        }
        return false;
    }

    public void activate() {
        if (status == TaskStatus.POSTED) status = TaskStatus.ACTIVE;
    }
    public void deactivate() {
        if (status == TaskStatus.ACTIVE) status = TaskStatus.POSTED;
    }
    /** For tasks whose subject disappears (e.g. a cancelled order). */
    public void expire() { if (!isFinished()) status = TaskStatus.EXPIRED; }

    public boolean isFinished() { return status == TaskStatus.DONE || status == TaskStatus.EXPIRED; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public Sector getSector() { return sector; }
    public double getRewardMoney() { return rewardMoney; }
    public double getTimeLeft() { return timeLeft; }
    public TaskStatus getStatus() { return status; }

    public final Map<String, Object> viewState() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("type", getClass().getSimpleName()); m.put("title", title);
        m.put("sector", sector.name()); m.put("status", status.name()); m.put("interactive", isInteractive());
        m.put("progress", Math.round(getProgress() * 1000) / 1000.0);
        m.put("timeLeft", timeLimit > 0 ? Math.round(Math.max(0, timeLeft) * 10) / 10.0 : -1);
        m.put("reward", rewardMoney);
        fillViewState(m);
        return m;
    }
}
