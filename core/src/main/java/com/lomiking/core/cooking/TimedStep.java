package com.lomiking.core.cooking;

import com.lomiking.core.character.Emote;
import java.util.Map;

/**
 * Base for steps where the food cooks by itself and the player must tap at the right moment.
 * Leaving it too long (2x the target) burns it.
 */
public abstract class TimedStep extends CookingStep {
    protected final double targetSeconds;
    private boolean burnt;
    private double finishedAt = -1;

    protected TimedStep(String title, double targetSeconds) {
        super(title);
        this.targetSeconds = targetSeconds;
    }
    @Override public InputType getGesture() { return InputType.TAP; }
    @Override public double getProgress() { return clamp01(elapsed / targetSeconds); }
    @Override public boolean isBurnt() { return burnt; }

    /** Score for one timed tap: 1.0 exactly on time, 0 at +-100% off. */
    protected double timingScore(double at, double target) { return clamp01(1 - Math.abs(at - target) / target); }

    protected void finish(double at) { finishedAt = at; markComplete(); }
    public double getFinishedAt() { return finishedAt; }

    @Override protected void onUpdate(double dt, CookingFeedback fb) {
        if (elapsed > targetSeconds * 0.85 && elapsed - dt <= targetSeconds * 0.85) fb.emote(Emote.NERVOUS, 1.2);
        if (elapsed >= targetSeconds * 2) {
            burnt = true;
            fb.burnt(getTitle());
            fb.emote(Emote.SHOCKED, 2.0);
            finish(elapsed);
        }
    }
    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("elapsed", round(elapsed)); m.put("target", targetSeconds); m.put("burnt", burnt);
    }
}
