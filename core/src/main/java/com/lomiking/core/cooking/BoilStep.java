package com.lomiking.core.cooking;

/** Noodles boil on their own; tap when they're just right. */
public class BoilStep extends TimedStep {
    public BoilStep(String what, double targetSeconds) { super("Boil the " + what, targetSeconds); }
    @Override public String getHint() { return "Tap when the bubbles turn golden"; }
    @Override public double getScore() { return isBurnt() ? 0.1 : timingScore(getFinishedAt(), targetSeconds); }
    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() == InputType.TAP || in.getType() == InputType.CONFIRM) finish(elapsed);
    }
}
