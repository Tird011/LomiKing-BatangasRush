package com.lomiking.core.cooking;

import java.util.Map;

/** Tap to shake in a spice, then press Done. Each shake off from the target costs points; way too many ruins it. */
public class SeasonStep extends CookingStep {
    private final String spiceId;
    private final int targetShakes;
    private int shakes;

    public SeasonStep(String spiceName, String spiceId, int targetShakes) {
        super("Season with " + spiceName);
        this.spiceId = spiceId; this.targetShakes = targetShakes;
    }
    @Override public InputType getGesture() { return InputType.TAP; }
    @Override public String getHint() { return "Tap to shake " + targetShakes + "x, then press Done"; }
    @Override public double getProgress() { return clamp01((double) shakes / targetShakes); }
    @Override public double getScore() { return clamp01(1 - 0.25 * Math.abs(shakes - targetShakes)); }
    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() == InputType.TAP) {
            shakes++;
            if (shakes == targetShakes + 1) addMistake(fb, "Careful, that's a lot of " + spiceId);
            if (shakes >= targetShakes + 4) markComplete();
        } else if (in.getType() == InputType.CONFIRM) {
            markComplete();
        }
    }
    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("spice", spiceId); m.put("shakes", shakes); m.put("target", targetShakes);
    }
}
