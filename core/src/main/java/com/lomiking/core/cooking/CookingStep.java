package com.lomiking.core.cooking;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One mini-game inside a recipe (chop, stir, fan the fire...). Template: the session feeds inputs and time;
 * each subclass decides what counts as progress and how well it was done (score 0..1).
 */
public abstract class CookingStep {
    private final String title;
    private boolean complete;
    private int mistakes;
    protected double elapsed;

    protected CookingStep(String title) { this.title = title; }

    /** Main gesture this step listens to; the view uses it to show the right hint/animation. */
    public abstract InputType getGesture();
    public abstract String getHint();
    /** 0..1 how far the step is. */
    public abstract double getProgress();
    /** 0..1 how well the step was done (valid once complete). */
    public abstract double getScore();

    protected abstract void onInput(PlayerInput input, CookingFeedback fb);
    protected void onUpdate(double dt, CookingFeedback fb) { }
    /** Extra values for the renderer (heat bar, timer...). */
    protected void fillViewState(Map<String, Object> out) { }

    public final void handleInput(PlayerInput input, CookingFeedback fb) {
        if (!complete) onInput(input, fb);
    }
    public final void update(double dt, CookingFeedback fb) {
        if (complete) return;
        elapsed += dt;
        onUpdate(dt, fb);
    }

    protected final void markComplete() { complete = true; }
    protected final void addMistake(CookingFeedback fb, String reason) { mistakes++; fb.mistake(reason); }

    public final boolean isComplete() { return complete; }
    public final int getMistakes() { return mistakes; }
    public final String getTitle() { return title; }
    public final double getElapsed() { return elapsed; }
    public boolean isBurnt() { return false; }

    public final Map<String, Object> viewState() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("step", getClass().getSimpleName());
        m.put("title", title);
        m.put("hint", getHint());
        m.put("gesture", getGesture().name());
        m.put("progress", round(getProgress()));
        m.put("mistakes", mistakes);
        fillViewState(m);
        return m;
    }

    protected static double clamp01(double v) { return Math.max(0, Math.min(1, v)); }
    protected static double round(double v) { return Math.round(v * 1000) / 1000.0; }
}
