package com.lomiking.core.cooking;

import java.util.Map;

/** Draw circles to stir. A steady pace (about 1 turn per second) scores best; dawdling lets the soup clump. */
public class StirStep extends CookingStep {
    private final double rotationsNeeded;
    private double rotations;

    public StirStep(String title, double rotationsNeeded) { super(title); this.rotationsNeeded = rotationsNeeded; }
    @Override public InputType getGesture() { return InputType.CIRCLE; }
    @Override public String getHint() { return "Stir in circles"; }
    @Override public double getProgress() { return clamp01(rotations / rotationsNeeded); }
    @Override public double getScore() {
        double idealTime = rotationsNeeded * 1.5;
        return elapsed <= idealTime ? 1 : clamp01(1 - (elapsed - idealTime) / (idealTime * 2) * 0.5);
    }
    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() != InputType.CIRCLE) return;
        rotations += Math.abs(in.getValue());
        if (rotations >= rotationsNeeded) markComplete();
    }
    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("rotations", round(rotations)); m.put("rotationsNeeded", rotationsNeeded);
    }
}
