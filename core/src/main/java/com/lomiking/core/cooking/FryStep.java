package com.lomiking.core.cooking;

import java.util.Map;

/** Pan-fry: optionally tap once halfway to flip, then tap to lift it out. */
public class FryStep extends TimedStep {
    private final boolean needsFlip;
    private double flippedAt = -1;

    public FryStep(String what, double targetSeconds, boolean needsFlip) {
        super("Fry the " + what, targetSeconds);
        this.needsFlip = needsFlip;
    }
    @Override public String getHint() {
        return needsFlip && flippedAt < 0 ? "Tap to flip halfway" : "Tap to lift it out when golden";
    }
    @Override public double getScore() {
        if (isBurnt()) return 0.1;
        double lift = timingScore(getFinishedAt(), targetSeconds);
        if (!needsFlip) return lift;
        double flip = flippedAt < 0 ? 0 : timingScore(flippedAt, targetSeconds / 2);
        return (lift + flip) / 2;
    }
    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() != InputType.TAP && in.getType() != InputType.CONFIRM) return;
        if (needsFlip && flippedAt < 0) flippedAt = elapsed;
        else finish(elapsed);
    }
    @Override protected void fillViewState(Map<String, Object> m) {
        super.fillViewState(m); m.put("needsFlip", needsFlip); m.put("flipped", flippedAt >= 0);
    }
}
