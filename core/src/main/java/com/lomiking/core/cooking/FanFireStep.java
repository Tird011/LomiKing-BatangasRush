package com.lomiking.core.cooking;

import com.lomiking.core.character.Emote;
import java.util.Map;

/**
 * The stove mini-game: blow into the mic (or swipe the pamaypay) to raise the heat and keep it in the
 * green zone long enough. Too hot scorches the pot.
 */
public class FanFireStep extends CookingStep {
    public static final double ZONE_LOW = 0.55, ZONE_HIGH = 0.85, SCORCH = 0.97, BLOW_THRESHOLD = 0.3;
    private final double secondsInZoneNeeded;
    private double heat = 0.2;
    private double blowPower;      // decays when the player stops blowing
    private double timeInZone, timeScorching;
    private boolean blowing;

    private final double gain;

    public FanFireStep(String title, double secondsInZoneNeeded) { this(title, secondsInZoneNeeded, 1.0); }
    public FanFireStep(String title, double secondsInZoneNeeded, double heatGain) {
        super(title); this.secondsInZoneNeeded = secondsInZoneNeeded; this.gain = heatGain;
    }
    @Override public InputType getGesture() { return InputType.MIC_LEVEL; }
    @Override public String getHint() { return "Blow into the mic or fan the fire, keep the flame in the green zone"; }
    @Override public double getProgress() { return clamp01(timeInZone / secondsInZoneNeeded); }
    @Override public double getScore() {
        if (elapsed <= 0) return 0;
        double efficiency = clamp01(secondsInZoneNeeded / Math.max(elapsed, secondsInZoneNeeded) * 1.6);
        return clamp01(efficiency - 0.15 * getMistakes());
    }

    @Override protected void onInput(PlayerInput in, CookingFeedback fb) {
        if (in.getType() == InputType.MIC_LEVEL) blowPower = clamp01(in.getValue());
        else if (in.getType() == InputType.SWIPE) blowPower = clamp01(blowPower + 0.45 * Math.max(in.getValue(), 0.4));
        boolean nowBlowing = blowPower > BLOW_THRESHOLD;
        if (nowBlowing && !blowing) fb.emote(Emote.BLOW, 0.6);
        blowing = nowBlowing;
    }

    @Override protected void onUpdate(double dt, CookingFeedback fb) {
        heat = clamp01(heat + (blowPower * 1.4 * gain - 0.15) * dt);
        blowPower = Math.max(0, blowPower - 0.6 * dt);
        if (blowing) fb.emote(Emote.BLOW, 0.3);
        if (heat >= ZONE_LOW && heat <= ZONE_HIGH) timeInZone += dt;
        if (heat >= SCORCH) {
            timeScorching += dt;
            if (timeScorching >= 1.0) {
                timeScorching = 0;
                addMistake(fb, "Too hot, it's scorching!");
                fb.emote(Emote.SHOCKED, 1.0);
                heat = 0.7;
            }
        }
        if (timeInZone >= secondsInZoneNeeded) markComplete();
    }
    public double getHeat() { return heat; }

    @Override protected void fillViewState(Map<String, Object> m) {
        m.put("heat", round(heat)); m.put("zoneLow", ZONE_LOW); m.put("zoneHigh", ZONE_HIGH);
        m.put("timeInZone", round(timeInZone)); m.put("needed", secondsInZoneNeeded); m.put("blowing", blowing);
    }
}
