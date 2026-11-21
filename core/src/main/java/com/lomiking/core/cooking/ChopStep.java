package com.lomiking.core.cooking;

import java.util.Map;

/** Swipe across the chopping board. Even, medium-length swipes give the neatest cuts. */
public class ChopStep extends CookingStep {
    public static final double IDEAL_SWIPE = 0.6, MIN_SWIPE = 0.15;
    private final String ingredientId;
    private final int cutsNeeded;
    private int cuts;
    private double accuracySum;

    public ChopStep(String ingredientName, String ingredientId, int cutsNeeded) {
        super("Chop the " + ingredientName);
        this.ingredientId = ingredientId;
        this.cutsNeeded = Math.max(1, cutsNeeded);
    }
    @Override public InputType getGesture() { return InputType.SWIPE; }
    @Override public String getHint() { return "Swipe across the board " + cutsNeeded + " times"; }
    @Override public double getProgress() { return (double) cuts / cutsNeeded; }
    @Override public double getScore() { return cuts == 0 ? 0 : accuracySum / cuts; }

    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() != InputType.SWIPE) return;
        if (in.getValue() < MIN_SWIPE) { addMistake(fb, "Too short, that didn't cut"); return; }
        cuts++;
        accuracySum += clamp01(1 - Math.abs(in.getValue() - IDEAL_SWIPE) / IDEAL_SWIPE);
        if (cuts >= cutsNeeded) markComplete();
    }
    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("ingredient", ingredientId); m.put("cuts", cuts); m.put("cutsNeeded", cutsNeeded);
    }
}
